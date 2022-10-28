package burp;

import burp.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class CustomProxy {
    static public String biid = "";

    static public String current_burp_config;

    public static String addProjectUpstreamProxyAndPollHttp(){
        JSONObject burp_config = new JSONObject(current_burp_config);
        burp_config.getJSONObject("project_options")
                .getJSONObject("connections")
                .getJSONObject("upstream_proxy")
                .getJSONArray("servers").put(new JSONObject("{\n" +
                        "                        \"destination_host\":\"polling.oastify.com\",\n" +
                        "                        \"enabled\":true,\n" +
                        "                        \"proxy_host\":\"127.0.0.1\",\n" +
                        "                        \"proxy_port\":12345\n" +
                        "                    },"));

        burp_config.getJSONObject("project_options")
                .getJSONObject("connections")
                .getJSONObject("upstream_proxy")
                .put("use_user_options",false);

        burp_config.getJSONObject("project_options")
                .getJSONObject("misc")
                .getJSONObject("collaborator_server")
                .put("poll_over_unencrypted_http",true);

        return burp_config.toString();
    }

    public static String getCollabBiid(IBurpExtenderCallbacks callbacks, IBurpCollaboratorClientContext collaborator){
        // Add upstream proxy and poll over unencrypted http
        current_burp_config = callbacks.saveConfigAsJson();
        callbacks.loadConfigFromJson(addProjectUpstreamProxyAndPollHttp());

        Thread proxyThread = new Thread(() -> {
            PrintWriter stdout = new PrintWriter(callbacks.getStdout(), true);
            try {
                final byte[]  Request = new byte[1024];
                ServerSocket serverSocket = new ServerSocket(12345);
                Socket socket = serverSocket.accept();

                InputStream InputStreamClient = socket.getInputStream();

                long starttime = System.currentTimeMillis();

                while (true){
                    if (System.currentTimeMillis() - starttime > 10000)
                        break;
                    int num_byte = InputStreamClient.read(Request);
                    if (num_byte != -1)
                    {
                        String request_str = new String(Request, StandardCharsets.UTF_8);

                        if (request_str.contains("GET http://polling.oastify.com/burpresults?biid=")){
                            int start_index = request_str.indexOf("biid=") + 5;
                            int end_index = request_str.indexOf(" ", start_index);
                            String biid = request_str.substring(start_index, end_index);
                            //CustomProxy.biid = URLDecoder.decode(biid, StandardCharsets.UTF_8.name());
                            CustomProxy.biid = biid;
                            // callbacks.loadConfigFromJson(current_burp_config);
                            
                            break;
                        }
                    }
                }

                serverSocket.close();
            }
            catch (Exception ex){
                stdout.println(ex.toString());
            }

        });
        proxyThread.start();
        
        Thread collaThread = new Thread(collaborator::fetchAllCollaboratorInteractions);
        collaThread.start();
        
        

        // Wait 1 second to capture fetch request
        try{
            Thread.sleep(1000);
        }
        catch (Exception ex){}

        //reverse config
        callbacks.loadConfigFromJson(current_burp_config);

        return CustomProxy.biid;
    }
}
