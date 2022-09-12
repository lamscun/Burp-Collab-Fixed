package payint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import autopayload.ExtensionState;


public class PayIntConnector {
	private String folderPath = "‪";
	private String payint_main = "payint.py";
	private String payint_File = "burp_payint_template.py";
	private String payint_PayloadFile = "burp_payint_payloads.txt";
	private String payint_XSS_HUNTER = "";
	private String payint_BURP_COLLAB_DOMAIN = "";
	private String payint_PROJECT_NAME = "";
	private String payint_SLEEP_TIME = "";
	private String payloadsPath = "";
	private String payloadsPayIntPath = "";
	private String payIntPath = "";
	private String payint_template = "import sys\r\n"
			+ "import json\r\n"
			+ "import os\r\n"
			+ "payintPath = \"___payint_system_folder_path___\"\r\n"
			+ "sys.path.append(payintPath)\r\n"
			+ "import payint\r\n"
			+ "import payint_settings\r\n"
			+ "\r\n"
			+ "payint_settings.XSS_HUNTER = \"payintxsshunter\"\r\n"
			+ "payint_settings.BURP_COLLAB_DOMAIN = \"payintburpcollabdomain\"\r\n"
			+ "payint_settings.SLEEP_TIME = payintsleeptime\r\n"
			+ "payint_settings.PROJECT_NAME = \"payintprojectname\".lower()"
			+ "\r\n"
			+ "\r\n"
			+ "\r\n"
			+ "sample_json = {\"value\":\"ZZPOLLUTIONZZ\"}\r\n"
			+ "payint_scan_options = {\r\n"
			+ "    \"scan_header\": False,\r\n"
			+ "    \"parameters\": [],\r\n"
			+ "    \"collection_attacks\": [],\r\n"
			+ "    \"url_encode\": False,\r\n"
			+ "    \"remove_param\": False,\r\n"
			+ "    \"use_sub_object\": False,\r\n"
			+ "    \"use_list\": False,\r\n"
			+ "    \"battering_ram\": False,\r\n"
			+ "    \"cast_payload_value_to_str\": False,\r\n"
			+ "    \"url_encode_new_lines\": False,\r\n"
			+ "    \"force_ret_str\": False,\r\n"
			+ "    \"exclude_parameters\": False,\r\n"
			+ "    \"use_sqlmap\": True,\r\n"
			+ "    \"sqlmap_settings\": {}\r\n"
			+ "}\r\n"
			+ "\r\n"
			+ "injected_body = payint.invoke_payint(sample_json, payint_scan_options)\r\n"
			+ "\r\n"
			+ "f = open(\"./payintpayloadsoutputfile\", \"w\")\r\n"
			+ "f.write(\"\")\r\n"
			+ "f.close()\r\n"
			+ "\r\n"
			+ "f = open(\"./payintpayloadsoutputfile\", \"a\")\r\n"
			+ "\r\n"
			+ "\r\n"
			+ "# print(injected_body)\r\n"
			+ "for i in injected_body:\r\n"
			+ "    # print(i)\r\n"
			+ "    if isinstance(i[\"payload\"], dict):\r\n"
			+ "        # print(1)\r\n"
			+ "        # print(i[\"payload\"][\"value\"])\r\n"
			+ "        f.write(i[\"payload_name\"] + \" ||| \" +json.dumps(i[\"payload\"][\"value\"], separators=(',', ':')).replace(\"ZZPOLLUTIONZZ\", \"\")[1:-1]+\"\\n\")\r\n"
			+ "        # print(i[\"payload_name\"] + \" ||| \" +json.dumps(i[\"payload\"][\"value\"], separators=(',', ':')).replace(\"ZZPOLLUTIONZZ\", \"\")[1:-1])\r\n"
			+ "    else:\r\n"
			+ "        f.write(i[\"payload_name\"] + \" ||| \" + i[\"payload\"].replace(\"ZZPOLLUTIONZZ\", \"\") + \"\\n\")\r\n"
			+ "        # print(i[\"payload_name\"] + \" ||| \" + i[\"payload\"].replace(\"ZZPOLLUTIONZZ\", \"\"))\r\n"
			+ "\r\n"
			+ "f.close()\r\n"
			+ "print(\"Outputpaylaodfile:\"+ os.getcwd() +\"\\\\\" + \"payintpayloadsoutputfile\")\r\n"
			+ "\r\n"
			+ "\r\n"
			+ "";
	
	public void createPayIntTemplateFile() {
		getConfilePayIntFormFile();
    	try {
	      FileWriter myWriter = new FileWriter("./" + this.payint_File);
	      payint_template = payint_template.replace("___payint_system_folder_path___", this.folderPath);
	      payint_template = payint_template.replace("payintpayloadsoutputfile", this.payint_PayloadFile);
	      
	      if (payint_XSS_HUNTER!="") {
	    	  payint_template = payint_template.replace("payintxsshunter", this.payint_XSS_HUNTER);
	      } else {
	    	  payint_template = payint_template.replace("payintxsshunter", "lamscun.xss.ht");
	      }
	      if (payint_BURP_COLLAB_DOMAIN!="") {
	    	  payint_template = payint_template.replace("payintburpcollabdomain", this.payint_BURP_COLLAB_DOMAIN);
	      } else {
	    	  payint_template = payint_template.replace("payintburpcollabdomain", "wwwz15e554m201wwajfl7m1ey54z1nq.oastify.com");
	      }
	      if (payint_PROJECT_NAME!="") {
	    	  payint_template = payint_template.replace("payintprojectname", this.payint_PROJECT_NAME);
	      } else {
	    	  payint_template = payint_template.replace("payintprojectname", "collabfix");
	      }
	      if (payint_SLEEP_TIME!="") {
	    	  payint_template = payint_template.replace("payintsleeptime", this.payint_SLEEP_TIME);
	      } else {
	    	  payint_template = payint_template.replace("payintsleeptime", "15");
	      }

	      // ExtensionState.getInstance().getCallbacks().printOutput(payint_template);
	      
	      myWriter.write(payint_template);
	      myWriter.close();
	      System.out.println("Successfully wrote Payloads_Path to the file" + "./burp_payint_template.py");
	    } catch (IOException e) {
	    // ExtensionState.getInstance().getCallbacks().printOutput("An error occurred. createPayIntTemplateFile");
	      e.printStackTrace();
	    }

    }
    public String getPayIntPayloads() {
    	String payloads = "";
    	try {
	      File myObj = new File("./"+ payint_PayloadFile);
	      Scanner myReader = new Scanner(myObj);
	      while (myReader.hasNextLine()) {
	        //String data = myReader.nextLine();
	        // System.out.println(data);
	        payloads += myReader.nextLine() +"\n";
	        
	      }
	      myReader.close();
	    } catch (FileNotFoundException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
	    }
    	
    	return payloads;
    }
	public String genPayIntPayload(String folderPath) throws IOException {
		this.folderPath = folderPath;
		this.createPayIntTemplateFile();
		
		String payint_file = folderPath + payint_main;
		
		Runtime rt = Runtime.getRuntime();
		String[] commands = { "python", "./" + payint_File };
		Process proc = rt.exec(commands);
	
		BufferedReader stdInput = new BufferedReader(new 
		     InputStreamReader(proc.getInputStream()));
	
		String s = "";
		String payloads = "";
		while ((s = stdInput.readLine()) != null) {
			payloads+=s;
		}
		
		if (payloads.contains("Outputpaylaodfile:")){
			// System.out.println(payloads);
			return payloads.split("Outputpaylaodfile:")[1];
		} else {
			return "";
		}
		
	}
	
	public void getConfilePayIntFormFile() {
		
		try {
	      File myObj = new File(new File("").getAbsolutePath()+"/auto_payload_extenstion_user_options.txt");
	      Scanner myReader = new Scanner(myObj);
	      while (myReader.hasNextLine()) {
	        String data = myReader.nextLine();
	        System.out.println(data);
	        if (data.contains("Payloads_Path: ")) {
	        	this.payloadsPath = data.replace("Payloads_Path: ", "");
	        }
	        if (data.contains("Payloads_PayInt_Path: ")) {
	        	this.payloadsPayIntPath = data.replace("Payloads_PayInt_Path: ", "");
	        }
	        
	        if (data.contains("PayInt_Folder_Path: ")) {
	        	this.payIntPath = data.replace("PayInt_Folder_Path: ", "");
	        }
	        
	        if (data.contains("PayInt_SLEEP_TIME: ")) {
	        	this.payint_SLEEP_TIME = data.replace("PayInt_SLEEP_TIME: ", "");
	        }
	        
	        if (data.contains("PayInt_XSS_HUNTER: ")) {
	        	this.payint_XSS_HUNTER = data.replace("PayInt_XSS_HUNTER: ", "");
	        }
	        
	        if (data.contains("PayInt_PROJECT_NAME: ")) {
	        	this.payint_PROJECT_NAME = data.replace("PayInt_PROJECT_NAME: ", "");
	        }
	        
	        if (data.contains("PayInt_BURP_COLLAB_DOMAIN: ")) {
	        	this.payint_BURP_COLLAB_DOMAIN = data.replace("PayInt_BURP_COLLAB_DOMAIN: ", "");
	        }
	        
	      }
	      myReader.close();
	    } catch (FileNotFoundException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
	    }
	}
}
