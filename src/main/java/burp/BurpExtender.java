package burp;

import com.coreyd97.BurpExtenderUtilities.DefaultGsonProvider;
import com.coreyd97.BurpExtenderUtilities.ILogProvider;
import com.coreyd97.BurpExtenderUtilities.Maintest;
import com.coreyd97.BurpExtenderUtilities.Preferences;
import com.coreyd97.BurpExtenderUtilities.ProjectSettingStore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import autopayload.AutoCompleter;
import autopayload.ExtensionState;
import logcolor.TableLogColor;


import burp.api.montoya.*;
import burp.api.montoya.collaborator.*;
import burp.api.montoya.logging.Logging;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.collaborator.*;
import burp.api.montoya.logging.Logging;


import java.io.UnsupportedEncodingException;
import java.util.Base64;

/* loaded from: collab_fixed_v5.jar:burp/BurpExtender.class */
public class BurpExtender implements BurpExtension, IBurpExtender, ITab, IExtensionStateListener, IContextMenuFactory, IHttpListener, AWTEventListener   {
	private String extensionName = "Collab_Fixed_v6.7";
	private String extensionVersion = "6.7";
	private IBurpExtenderCallbacks callbacks;
	private IExtensionHelpers helpers;
	private PrintWriter stderr;
	private PrintWriter stdout;
	private JPanel panel;
	private volatile boolean running;
	private int unread = 0;
	private ArrayList<Integer> readRows = new ArrayList<>();
	private IBurpCollaboratorClientContext collaborator = null;
	private Collaborator collaboratorNew = null;
	private HashMap<Integer, HashMap<String, String>> interactionHistory = new HashMap<>();
	private HashMap<String, HashMap<String, String>> originalRequests = new HashMap<>();
	private HashMap<String, String> originalResponses = new HashMap<>();
	private JTabbedPane interactionsTab;
	private Integer selectedRow = -1;
	private HashMap<Integer, Color> colours = new HashMap<>();
	private HashMap<Integer, Color> textColours = new HashMap<>();
	private HashMap<Integer, String> comments = new HashMap<>();
	private static final String COLLABORATOR_PLACEHOLDER = "$collabplz";
	private Thread pollThread;
	private long POLL_EVERY_MS = 5000;
	private boolean pollNow = true;
	private boolean createdCollaboratorPayload = false;
	private int pollCounter = 0;
	private boolean shutdown = false;
	private boolean isSleeping = false;
	private Preferences prefs;
	private Integer rowNumber = 0;
	private DefaultTableModel model;
	private JTable collaboratorTable;
	private TableRowSorter<TableModel> sorter = null;
	private Color defaultTabColour;

	private String config_biids = "";
	private String config_cnames = "";
	private String config_collab_ids = "";
	String subDomain = "";
	private String choosed = "";

	public static JTextField filepath = new JTextField();
	public static JTabbedPane mainTab = new JTabbedPane();
	public static JTextPane l_cname = new JTextPane();
	public static JButton btn_domain_id = new JButton();
	public static JComboBox multipleBiid = new JComboBox();
	
	public static JTabbedPane jtabC = new JTabbedPane();
	public static JSplitPane collaboratorClientSplit = null;

	public static JTextField biidText = new JTextField();
	public static JTextField collabIdText = new JTextField();
	public static JTextField logspath = null;
	public static JTextField customText = null;

	public static String collab_fixed_config_file_name = "collab_fixed_config.json";
	public static String collab_fixed_logs_file_name = "collab_fixed_logs.json";

	
	private Collaborator collab;
	
	/**
	 * @wbp.parser.entryPoint
	 */
	
	
	
	
	public void registerExtenderCallbacks(final IBurpExtenderCallbacks callbacks) {
		shutdown = false;
		isSleeping = false;
		helpers = callbacks.getHelpers();
		this.callbacks = callbacks;
		callbacks.registerExtensionStateListener(this);
		callbacks.registerContextMenuFactory(this);
		callbacks.registerHttpListener(this);
		stderr = new PrintWriter(callbacks.getStderr(), true);
		stdout = new PrintWriter(callbacks.getStdout(), true);
		callbacks.setExtensionName(extensionName);
		defaultTabColour = getDefaultTabColour();
		DefaultGsonProvider gsonProvider = new DefaultGsonProvider();

		Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
		
		ExtensionState.setCallbacks(callbacks);
		Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
		callbacks.registerExtensionStateListener(this);
		callbacks.addSuiteTab(this);
		
		prefs = new Preferences("Collab_Fixed", gsonProvider, new ILogProvider() {
			@Override
			public void logOutput(String message) {
				// System.out.println("Output:"+message);
			}

			@Override
			public void logError(String errorMessage) {
				System.err.println("Error Output:" + errorMessage);
			}
		}, callbacks);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				stdout.println(extensionName + " " + extensionVersion);
				stdout.println(
						"\n---------------\nTo use Collab_Fixed right click in the repeater request tab and select \"Collab_Fixed->Insert Collaborator payload\". \nUse \"Collab_Fixed->Insert Collaborator placeholder\" to insert a placeholder that will be replaced by a Collaborator payload in every request. \nThe Collab_Fixed placeholder also works in other Burp tools. You can also use the buttons in the Collab_Fixed tab to create a payload and poll now."
				+ "\n\n- Dev by: @lamscun" + "\n- Update v5.3: Allow delete selected rows\n"
				+ "\n- Update v5.4: Real random Biid by: @chihuynhminh.\n"
				+ "\n- Update v5.5: Hot key Ctrl + Shift + F for HTML, XML Beautifier the selected strings.\n"
				+ "\n- Update v5.5: Hot key Ctrl + Shift + J for Json Beautifier the selected strings."
				+ "\n- Update v5.6: AutoPayload/AutoComple . Hot key Ctrl + Shift + N for show Payload Table. Hot key Ctrl + Shift + M for hide Payload Table"
				+ "\n\n---------------\n");
				running = true;
				try {
					prefs.registerSetting("config", new TypeToken<HashMap<String, Integer>>() {
					}.getType(), new HashMap<>(), Preferences.Visibility.PROJECT);
					prefs.registerSetting("readRows", new TypeToken<ArrayList<Integer>>() {
					}.getType(), new ArrayList<Integer>(), Preferences.Visibility.PROJECT);
					prefs.registerSetting("interactionHistory",
							new TypeToken<HashMap<Integer, HashMap<String, String>>>() {
							}.getType(), new HashMap<>(), Preferences.Visibility.PROJECT);
					prefs.registerSetting("originalRequests",
							new TypeToken<HashMap<String, HashMap<String, String>>>() {
							}.getType(), new HashMap<>(), Preferences.Visibility.PROJECT);
					prefs.registerSetting("originalResponses", new TypeToken<HashMap<String, String>>() {
					}.getType(), new HashMap<>(), Preferences.Visibility.PROJECT);
					prefs.registerSetting("comments", new TypeToken<HashMap<Integer, String>>() {
					}.getType(), new HashMap<>(), Preferences.Visibility.PROJECT);
					prefs.registerSetting("colours", new TypeToken<HashMap<Integer, Color>>() {
					}.getType(), new HashMap<>(), Preferences.Visibility.PROJECT);
					prefs.registerSetting("textColours", new TypeToken<HashMap<Integer, Color>>() {
					}.getType(), new HashMap<>(), Preferences.Visibility.PROJECT);
				} catch (Throwable e) {
					System.err.println("Error registering settings:" + e);
				}

				// Main Panel
				
				panel = new JPanel(new BorderLayout());
				JPanel topPanel = new JPanel();
				
				TableLogColor tableLogColor = new TableLogColor();
				JPanel topPanel_autoPayload = new JPanel();
				
				mainTab.add("Collab_fixed",panel); 
				mainTab.add("Log Color",tableLogColor.getLogTable()); 
				mainTab.add("Auto Payload", ExtensionState.getInstance().getAutoCompleterTab()); 
				
				topPanel.setLayout(new GridBagLayout());
				JButton exportBtn = new JButton("Export");
				exportBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						JFrame frame = new JFrame();
						JFileChooser fileChooser = new JFileChooser();
						fileChooser.setDialogTitle("Please choose where to save interactions");
						int userSelection = fileChooser.showSaveDialog(frame);
						if (userSelection == JFileChooser.APPROVE_OPTION) {
							File fileToSave = fileChooser.getSelectedFile();
							String filePath = fileToSave.getAbsolutePath();
							ProjectSettingStore projectSettingStore = prefs.getProjectSettingsStore();
							saveSettings();
							String jsonStr = projectSettingStore.getJSONSettings();
							FileWriter file = null;
							try {
								file = new FileWriter(filePath);
								file.write(jsonStr);
							} catch (IOException ex) {
								ex.printStackTrace();
							} finally {
								try {
									file.flush();
									file.close();
								} catch (IOException ex) {
									ex.printStackTrace();
								}
							}
						}
					}
				});

				JComboBox filter = new JComboBox();
				filter.addItem("All");
				filter.addItem("DNS");
				filter.addItem("HTTP");
				filter.addItem("SMTP");
				
				
				
				multipleBiid.setPreferredSize(new Dimension(200,25));
				
				multipleBiid.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// stdout.println("Debug 1");
						if (multipleBiid.getSelectedItem().toString().contains(" - ")) {
							// stdout.println("Debug 2");
							String selectedBiid = multipleBiid.getSelectedItem().toString().split(" - ")[1];
							String selectedCollabId = multipleBiid.getSelectedItem().toString().split(" - ")[0];
							btn_domain_id.setText(selectedCollabId + ".oastify.com");
							stdout.println(selectedBiid);
							stdout.println(selectedCollabId);
							BurpExtender.biidText.setText(selectedBiid);
							BurpExtender.collabIdText.setText(selectedCollabId);
							
							String text = selectedCollabId;
				               if(text.length() == 0) {
				                  sorter.setRowFilter(null);
				               } else {
				                  try {
				                     sorter.setRowFilter(RowFilter.regexFilter(text));
				                  } catch(PatternSyntaxException pse) {
				                        System.out.println("Bad regex pattern");
				                  }
				               }
							
						}
						
						
					}
				});

				/*
				 * JButton createCollaboratorPayloadWithTaboratorCmd = new
				 * JButton("Collab Fixed commands & copy");
				 * createCollaboratorPayloadWithTaboratorCmd.addActionListener(new
				 * ActionListener() {
				 * 
				 * @Override public void actionPerformed(ActionEvent e) {
				 * createdCollaboratorPayload = true; String payload =
				 * collaborator.generatePayload(true) +
				 * "?Collab_Fixed=comment:Test;bgColour:0x000000;textColour:0xffffff";
				 * Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new
				 * StringSelection(payload), null); } });
				 */
				
				customText = new JTextField();
				customText.setPreferredSize(new Dimension(100, 25));
				JComboBox sub_type = new JComboBox();
				sub_type.addItem("Datetime");
				sub_type.addItem("Timestamp");
				
				
				sub_type.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						choosed = sub_type.getSelectedItem().toString();
						if (choosed=="Datetime") {
							Timestamp timestamp = new Timestamp(System.currentTimeMillis()+3600000*7);
							customText.setText("d" + timestamp.toString().split("\\.")[0].replace("-", "").replace(":", "").replace(" ", "t") );
						}
						if (choosed=="Timestamp") {
							customText.setText(java.lang.System.currentTimeMillis() / 1000 +"");
						}
		
					}
				});

				JButton pollButton = new JButton("Poll");

				JButton randomButton = new JButton("Random ID");
				
				JButton addNewBiidButton = new JButton("Add NewID");
				
				JButton removeCurrentBiid = new JButton("Remove Biid");
				
				JButton filterButton = new JButton("Filter");
				
				JTextField filterText = new JTextField();
				
				filterText.setPreferredSize(new Dimension(350, 25));
				filterText.setMinimumSize(new Dimension(200, 25));
				
				filterText.addKeyListener(new KeyAdapter() {
			        @Override
			        public void keyPressed(KeyEvent e) {
			            if(e.getKeyCode() == KeyEvent.VK_ENTER){
			               
			               String text = filterText.getText();
			               if(text.length() == 0) {
			                  sorter.setRowFilter(null);
			               } else {
			                  try {
			                     sorter.setRowFilter(RowFilter.regexFilter(text));
			                  } catch(PatternSyntaxException pse) {
			                        System.out.println("Bad regex pattern");
			                  }
			               }
			            }
			        }

			    });
				
				JTextField numberOfPayloads = new JTextField("1");
				numberOfPayloads.setMinimumSize(new Dimension(25, 25));

				JTextField timePollAuto = new JTextField("5");
				timePollAuto.setMinimumSize(new Dimension(25, 25));

				String jarPath = "";
				try {
					jarPath = MainClass.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
				} catch (URISyntaxException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

				filepath = new JTextField(jarPath + collab_fixed_config_file_name);
				filepath.setMinimumSize(new Dimension(200, 25));
				logspath = new JTextField(jarPath + collab_fixed_logs_file_name);
				logspath.setMinimumSize(new Dimension(200, 25));

				btn_domain_id = new JButton("null");
				btn_domain_id.setBackground(null);
				btn_domain_id.setBorder(null);
				btn_domain_id.setMaximumSize(new Dimension(450, 30));
				btn_domain_id.setPreferredSize(new Dimension(450, 25));
				btn_domain_id.setMinimumSize(new Dimension(250, 25));

				btn_domain_id.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						subDomain = customText.getText();
						if (subDomain.length() >0) {
							subDomain=subDomain+".";
						} 
						
						Toolkit.getDefaultToolkit().getSystemClipboard()
								.setContents(new StringSelection(
										subDomain + btn_domain_id.getText()),
										(ClipboardOwner) null);
					}
				});

				l_cname = new JTextPane();
				l_cname.setText("CNAME: Null");
				l_cname.setEditable(false);
				l_cname.setBackground(null);
				l_cname.setBorder(null);

				numberOfPayloads.setPreferredSize(new Dimension(50, 25));
				// filepath.setPreferredSize(new Dimension(350, 25));
				// logspath.setPreferredSize(new Dimension(350, 25));

				biidText.setPreferredSize(new Dimension(350, 25));
				biidText.setMinimumSize(new Dimension(200, 25));
				collabIdText.setPreferredSize(new Dimension(350, 25));
				collabIdText.setMinimumSize(new Dimension(200, 25));

				JLabel l_biidText = new JLabel();
				l_biidText.setText("Poll Biid: ");

				JLabel l_collabIdText = new JLabel();
				l_collabIdText.setText("Collab Fixed Id: ");

				JButton b_loadConfig = new JButton("Load Config");
				b_loadConfig.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						try {
							saveConfigToFile();
							loadConfigFromFile2();

						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}

				});

				JButton createCollaboratorPayload = new JButton("NoFixed-Copy");
				createCollaboratorPayload.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						createdCollaboratorPayload = true;
						int amount = 1;
						try {
							amount = Integer.parseInt(numberOfPayloads.getText());
						} catch (NumberFormatException ex) {
							amount = 1;
						}
						StringBuilder payloads = new StringBuilder();
						payloads.append(collaborator.generatePayload(true));
						for (int i = 1; i < amount; i++) {
							payloads.append("\n");
							payloads.append(collaborator.generatePayload(true));
						}
						Toolkit.getDefaultToolkit().getSystemClipboard()
								.setContents(new StringSelection(payloads.toString()), null);
					}
				});
				pollButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						////
						
						////
						
						pollNow = true;
						pollNowWithFixedCollab();
						if (isSleeping) {
							pollThread.interrupt();
						}
					}
				});
				
				filterButton.addActionListener(new ActionListener() {
		            public void actionPerformed(ActionEvent e) {
		               String text = filterText.getText();
		               if(text.length() == 0) {
		                  sorter.setRowFilter(null);
		               } else {
		                  try {
		                     sorter.setRowFilter(RowFilter.regexFilter(text));
		                  } catch(PatternSyntaxException pse) {
		                        System.out.println("Bad regex pattern");
		                  }
		                }
		            }
		         });

				pollButton.setPreferredSize(new Dimension(80, 30));
				pollButton.setMaximumSize(new Dimension(80, 30));

				createCollaboratorPayload.setPreferredSize(new Dimension(180, 30));
				createCollaboratorPayload.setMaximumSize(new Dimension(180, 30));

				topPanel.add(l_collabIdText, createConstraints(1, 1, 1, GridBagConstraints.NONE));
				topPanel.add(collabIdText, createConstraints(2, 1, 1, GridBagConstraints.NONE));
				topPanel.add(sub_type, createConstraints(3, 1, 1, GridBagConstraints.NONE));
				topPanel.add(customText, createConstraints(4, 1, 1, GridBagConstraints.NONE));
				topPanel.add(btn_domain_id, createConstraints(5, 1, 1, GridBagConstraints.NONE));
				topPanel.add(pollButton, createConstraints(6, 1, 1, GridBagConstraints.NONE));
				
				topPanel.add(l_biidText, createConstraints(1, 2, 1, GridBagConstraints.NONE));
				topPanel.add(biidText, createConstraints(2, 2, 1, GridBagConstraints.NONE));
				topPanel.add(l_cname, createConstraints(3, 2, 1, GridBagConstraints.NONE));
				topPanel.add(timePollAuto, createConstraints(4, 2, 1, GridBagConstraints.NONE));

				topPanel.add(b_loadConfig, createConstraints(1, 3, 1, GridBagConstraints.NONE));
				topPanel.add(createCollaboratorPayload, createConstraints(2, 3, 1, GridBagConstraints.NONE));
				topPanel.add(filter, createConstraints(3, 3, 1, GridBagConstraints.NONE));
				topPanel.add(exportBtn, createConstraints(4, 3, 1, GridBagConstraints.NONE));
				topPanel.add(randomButton, createConstraints(5, 3, 1, GridBagConstraints.NONE));
				topPanel.add(addNewBiidButton, createConstraints(6, 3, 1, GridBagConstraints.NONE));
				
				topPanel.add(filterButton, createConstraints(1, 4, 1, GridBagConstraints.NONE));
				topPanel.add(filterText, createConstraints(2, 4, 1, GridBagConstraints.NONE));
				
				topPanel.add(multipleBiid, createConstraints(5, 4, 1, GridBagConstraints.NONE));
				topPanel.add(removeCurrentBiid, createConstraints(6, 4, 1, GridBagConstraints.NONE));

				addNewBiidButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int answer = JOptionPane.showConfirmDialog((Component) null,
								"This will add new biid, are you sure?");
						if (answer == 0) {
							
							CollaboratorClient collab_client = collab.createClient();
					        CollaboratorPayload collab_client_payload = collab_client.generatePayload("lamscun", PayloadOption.WITHOUT_SERVER_LOCATION);
					        //stdout.println(collab_client_payload.toString());
					        //stdout.println(collab_client.getSecretKey().toString());
					        
					
							// Get from custom proxy
							// String ran_collab = collaborator.generatePayload(false);
							// String ran_biid = CustomProxy.getCollabBiid(callbacks, collaborator);
							// https://github.com/PortSwigger/burp-extensions-montoya-api/blob/73369af49eac0079199b5a3a5273835036d91adf/api/src/test/java/burp/api/montoya/TestExtension.java
							String ran_collab = collab_client_payload.toString();
							String ran_biid = helpers.urlEncode(collab_client.getSecretKey().toString());
									
							
							stdout.println("Random collab - biid: " + ran_collab + " - " + ran_biid);
							
							multipleBiid.addItem(ran_collab + " - " + ran_biid);
							
							try {
								saveConfigToFile();
								loadConfigFromFile2();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
							// BurpExtender.biidText.setText(ran_biid);
							// BurpExtender.collabIdText.setText(ran_collab);
							// Get from custom proxy

							/*
							 * Random r = new Random(); String[] list_random_biids =
							 * {"g8zhf3fhiA04IZyUksUl8IM5yQDtUGZMFhWQ0Zwba7k%3d -- l2oayratq3wrna9hjnqvoge3quwkk9.oastify.com"
							 * ,
							 * "op2T%2fiCV3GNCepi%2fQWT5eeCY1u1KVc6fXelNiQxEfUM%3d -- 020k3pifltekneo7d74oof30ur0ho6.oastify.com"
							 * }; String random_biid =
							 * list_random_biids[r.nextInt(list_random_biids.length)];
							 * BurpExtender.biidText.setText(random_biid.split(" -- ")[0]);
							 * BurpExtender.collabIdText.setText(random_biid.split(" -- ")[1].replace(
							 * ".oastify.com", ""));
							 */
						}
					}
				});
				
				removeCurrentBiid.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int answer = JOptionPane.showConfirmDialog((Component) null,
								"This will remove current biid, are you sure?");
						if (answer == 0) {

							// Get from custom proxy
							
							String currrentBiidCollab = multipleBiid.getSelectedItem().toString();
							multipleBiid.removeItem(multipleBiid.getSelectedItem());
							multipleBiid.getSelectedItem();
							
							biidText.setText("");
							collabIdText.setText("");
							btn_domain_id.setText(".oastify.com");
							
							// ashMap<Integer, HashMap<String, String>> interactionHistory = new HashMap<>();						
							try {
								saveConfigToFile();
								loadConfigFromFile2();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
						
							
							
						}
					}
				});
				
				randomButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int answer = JOptionPane.showConfirmDialog((Component) null,
								"This will remove your current biid, it can not retake, are you sure?");
						if (answer == 0) {

							// Get from custom proxy
							String ran_collab = collaborator.generatePayload(false);
							String ran_biid = CustomProxy.getCollabBiid(callbacks, collaborator);
							stdout.println("Random collab - biid: " + ran_collab + " - " + ran_biid);

							BurpExtender.biidText.setText(ran_biid);
							BurpExtender.collabIdText.setText(ran_collab);
							// Get from custom proxy

							/*
							 * Random r = new Random(); String[] list_random_biids =
							 * {"g8zhf3fhiA04IZyUksUl8IM5yQDtUGZMFhWQ0Zwba7k%3d -- l2oayratq3wrna9hjnqvoge3quwkk9.oastify.com"
							 * ,
							 * "op2T%2fiCV3GNCepi%2fQWT5eeCY1u1KVc6fXelNiQxEfUM%3d -- 020k3pifltekneo7d74oof30ur0ho6.oastify.com"
							 * }; String random_biid =
							 * list_random_biids[r.nextInt(list_random_biids.length)];
							 * BurpExtender.biidText.setText(random_biid.split(" -- ")[0]);
							 * BurpExtender.collabIdText.setText(random_biid.split(" -- ")[1].replace(
							 * ".oastify.com", ""));
							 */
						}
					}
				});

				panel.add(topPanel, BorderLayout.NORTH);
				panel.addComponentListener(new ComponentAdapter() {
					@Override
					public void componentShown(ComponentEvent e) {
						pollNow = true;
					}
				});
				interactionsTab = new JTabbedPane();
				collaboratorClientSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
				collaboratorClientSplit.setResizeWeight(.5d);
				final Class[] classes = new Class[] { Integer.class, Long.class, String.class, String.class,
						String.class, String.class };
				model = new DefaultTableModel() {
					@Override
					public boolean isCellEditable(int row, int column) {
						return false;
					}

					@Override
					public Class<?> getColumnClass(int columnIndex) {
						if (columnIndex < classes.length)
							return classes[columnIndex];
						return super.getColumnClass(columnIndex);
					}
				};
				collaboratorTable = new JTable(model);
				sorter = new TableRowSorter<>(model);
				collaboratorTable.setRowSorter(sorter);
				model.addColumn("#");
				model.addColumn("Time");
				model.addColumn("Type");
				model.addColumn("IP");
				model.addColumn("Payload");
				model.addColumn("Comment");
				collaboratorTable.getColumnModel().getColumn(0).setPreferredWidth(50);
				collaboratorTable.getColumnModel().getColumn(0).setMaxWidth(50);
				collaboratorTable.getColumnModel().getColumn(2).setPreferredWidth(80);
				collaboratorTable.getColumnModel().getColumn(2).setMaxWidth(80);
				JPopupMenu popupMenu = new JPopupMenu();
				JMenuItem commentMenuItem = new JMenuItem("Add comment");
				commentMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int rowNum = collaboratorTable.getSelectedRow();
						if (rowNum > -1) {
							int realRowNum = collaboratorTable.convertRowIndexToModel(rowNum);
							String comment = JOptionPane.showInputDialog("Please enter a comment");
							collaboratorTable.getModel().setValueAt(comment, realRowNum, 5);
							if (comment.length() == 0) {
								if (comments.containsKey(realRowNum)) {
									comments.remove(realRowNum);
								}
							} else {
								comments.put(realRowNum, comment);
							}
						}
					}
				});
				popupMenu.add(commentMenuItem);
				JMenu highlightMenu = new JMenu("Highlight");
				highlightMenu.add(generateMenuItem(collaboratorTable, null, "HTTP", null));
				highlightMenu.add(generateMenuItem(collaboratorTable, Color.decode("0xfa6364"), "HTTP", Color.white));
				highlightMenu.add(generateMenuItem(collaboratorTable, Color.decode("0xfac564"), "HTTP", Color.black));
				highlightMenu.add(generateMenuItem(collaboratorTable, Color.decode("0xfafa64"), "HTTP", Color.black));
				highlightMenu.add(generateMenuItem(collaboratorTable, Color.decode("0x63fa64"), "HTTP", Color.black));
				highlightMenu.add(generateMenuItem(collaboratorTable, Color.decode("0x63fafa"), "HTTP", Color.black));
				highlightMenu.add(generateMenuItem(collaboratorTable, Color.decode("0x6363fa"), "HTTP", Color.white));
				highlightMenu.add(generateMenuItem(collaboratorTable, Color.decode("0xfac5c5"), "HTTP", Color.black));
				highlightMenu.add(generateMenuItem(collaboratorTable, Color.decode("0xfa63fa"), "HTTP", Color.black));
				highlightMenu.add(generateMenuItem(collaboratorTable, Color.decode("0xb1b1b1"), "HTTP", Color.black));
				popupMenu.add(highlightMenu);
				JMenuItem clearMenuItem = new JMenuItem("Clear All");
				clearMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int answer = JOptionPane.showConfirmDialog(null,
								"This will clear all interactions, are you sure?");
						TableModel model = (DefaultTableModel) collaboratorTable.getModel();
						if (answer == 0) {
							interactionHistory = new HashMap<>();
							originalRequests = new HashMap<>();
							originalResponses = new HashMap<>();
							readRows = new ArrayList<>();
							unread = 0;
							rowNumber = 0;
							colours = new HashMap<>();
							textColours = new HashMap<>();
							comments = new HashMap<>();
							((DefaultTableModel) model).setRowCount(0);
							interactionsTab.removeAll();
							updateTab(false);
							saveLogs();

						}
						collaboratorTable.clearSelection();
					}
				});
				popupMenu.add(clearMenuItem);

				JMenuItem deleteSelectedMenu = new JMenuItem("Delete Selected");
				deleteSelectedMenu.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int answer = JOptionPane.showConfirmDialog(null, "This will delete this items, are you sure?");
						TableModel model = (DefaultTableModel) collaboratorTable.getModel();
						if (answer == 0) {
							int[] row_ids = collaboratorTable.getSelectedRows();
							// stdout.print("RemoveId 1:");
							for (int rowId : row_ids) {
								int modelRow = collaboratorTable.convertRowIndexToModel(rowId);
								int id = (int) collaboratorTable.getModel().getValueAt(modelRow, 0);

								if (!readRows.contains(id)) {
									unread--;
								}
								// stdout.print("RemoveId:");
								// stdout.print(id);
								// Delete in history
								interactionHistory.remove(id);
								// Delete in table UI
								rowId = collaboratorTable.convertRowIndexToModel(rowId);
								((DefaultTableModel) model).removeRow(rowId);
								
								

								// After delete a items, need to decrease index from 1
								for (int i = 0; i < row_ids.length; i++) {
									row_ids[i]--;
								}

							}

						}
						saveLogs();
						collaboratorTable.clearSelection();
					}
				});
				popupMenu.add(deleteSelectedMenu);
				collaboratorTable.setComponentPopupMenu(popupMenu);

				JScrollPane collaboratorScroll = new JScrollPane(collaboratorTable);
				collaboratorTable.setFillsViewportHeight(true);
				collaboratorClientSplit.setTopComponent(collaboratorScroll);
				collaboratorClientSplit.setBottomComponent(new JPanel());
				
				
			    panel.add(collaboratorClientSplit, BorderLayout.CENTER);
				callbacks.addSuiteTab(BurpExtender.this);
				collaborator = callbacks.createBurpCollaboratorClientContext();
				DefaultTableCellRenderer tableCellRender = new DefaultTableCellRenderer() {
					@Override
					public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
							boolean hasFocus, int row, int column) {
						final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
								column);
						int modelRow = table.convertRowIndexToModel(row);
						int id = (int) table.getModel().getValueAt(modelRow, 0);
						putClientProperty("html.disable", Boolean.TRUE);
						if (isSelected) {
							if (!readRows.contains(id)) {
								c.setFont(c.getFont().deriveFont(Font.PLAIN));
								readRows.add(id);
								unread--;
							}
							if (selectedRow != row && collaboratorTable.getSelectedRowCount() == 1) {
								JPanel descriptionPanel = new JPanel(new BorderLayout());
								HashMap<String, String> interaction = interactionHistory.get(id);
								JTextArea description = new JTextArea();
								description.setEditable(false);
								description.setBorder(null);
								interactionsTab.removeAll();
								interactionsTab.addTab("Description", descriptionPanel);
								if (interaction.get("type").equals("DNS")) {
									TaboratorMessageEditorController taboratorMessageEditorController = new TaboratorMessageEditorController();
									description.setText("DNS lookup of type " + interaction.get("query_type")
											+ " for the domain name: " + interaction.get("interaction_id")
											+ collaborator.getCollaboratorServerLocation() + ".\n\n"
											+ "From IP address: " + interaction.get("client_ip") + " at "
											+ interaction.get("time_stamp") + "\n\n" + interaction.get("sub_domain"));
									IMessageEditor messageEditor = callbacks
											.createMessageEditor(taboratorMessageEditorController, false);
									messageEditor.setMessage(helpers.base64Decode(interaction.get("raw_query")), false);
									if (originalRequests.containsKey(interaction.get("interaction_id"))) {
										HashMap<String, String> requestInfo = originalRequests
												.get(interaction.get("interaction_id"));
										IHttpService httpService = helpers.buildHttpService(requestInfo.get("host"),
												Integer.decode(requestInfo.get("port")), requestInfo.get("protocol"));
										taboratorMessageEditorController.setHttpService(httpService);
										IMessageEditor requestMessageEditor = callbacks
												.createMessageEditor(taboratorMessageEditorController, false);
										if (requestInfo.get("request") != null) {
											requestMessageEditor.setMessage(
													helpers.stringToBytes(requestInfo.get("request")), true);
											interactionsTab.addTab("Original request",
													requestMessageEditor.getComponent());
										}
										if (originalResponses.containsKey(interaction.get("interaction_id"))) {
											taboratorMessageEditorController.setHttpService(httpService);
											IMessageEditor responseMessageEditor = callbacks
													.createMessageEditor(taboratorMessageEditorController, false);
											if (requestInfo.get("request") != null && originalResponses
													.get(interaction.get("interaction_id")) != null) {
												responseMessageEditor.setMessage(helpers.stringToBytes(
														originalResponses.get(interaction.get("interaction_id"))),
														true);
												interactionsTab.addTab("Original response",
														responseMessageEditor.getComponent());
											}
										}
									}
									interactionsTab.addTab("DNS query", messageEditor.getComponent());
								} else if (interaction.get("type").equals("HTTP")) {

									stdout.println(interaction);
									TaboratorMessageEditorController taboratorMessageEditorController = new TaboratorMessageEditorController();
									URL collaboratorURL = null;
									try {
										collaboratorURL = new URL(interaction.get("protocol").toLowerCase() + "://"
												+ collaborator.getCollaboratorServerLocation());
									} catch (MalformedURLException e) {
										stderr.println("Failed parsing Collaborator URL:" + e.toString());
									}
									if (collaboratorURL != null) {
										IHttpService httpService = helpers.buildHttpService(collaboratorURL.getHost(),
												collaboratorURL.getPort() == -1 ? collaboratorURL.getDefaultPort()
														: collaboratorURL.getPort(),
												interaction.get("protocol").equals("HTTPS"));
										taboratorMessageEditorController.setHttpService(httpService);
									}
									byte[] collaboratorResponse = helpers.base64Decode(interaction.get("response"));
									stdout.println(helpers.bytesToString(collaboratorResponse));
									byte[] collaboratorRequest = helpers.base64Decode(interaction.get("request"));
									stdout.println(collaboratorRequest.toString());

									taboratorMessageEditorController.setRequest(collaboratorRequest);
									taboratorMessageEditorController.setResponse(collaboratorResponse);
									description.setText("The Collaborator server received an "
											+ interaction.get("protocol")
											+ " request.\n\nThe request was received from IP address "
											+ interaction.get("client_ip") + " at " + interaction.get("time_stamp"));
									if (originalRequests.containsKey(interaction.get("interaction_id"))) {
										HashMap<String, String> requestInfo = originalRequests
												.get(interaction.get("interaction_id"));
										IHttpService httpService = helpers.buildHttpService(requestInfo.get("host"),
												Integer.decode(requestInfo.get("port")), requestInfo.get("protocol"));
										taboratorMessageEditorController.setHttpService(httpService);
										IMessageEditor requestMessageEditor = callbacks
												.createMessageEditor(taboratorMessageEditorController, false);
										if (requestInfo.get("request") != null) {
											requestMessageEditor.setMessage(
													helpers.stringToBytes(requestInfo.get("request")), true);
											interactionsTab.addTab("Original request",
													requestMessageEditor.getComponent());
										}
										if (originalResponses.containsKey(interaction.get("interaction_id"))) {
											taboratorMessageEditorController.setHttpService(httpService);
											IMessageEditor responseMessageEditor = callbacks
													.createMessageEditor(taboratorMessageEditorController, false);
											if (requestInfo.get("request") != null && originalResponses
													.get(interaction.get("interaction_id")) != null) {
												responseMessageEditor.setMessage(helpers.stringToBytes(
														originalResponses.get(interaction.get("interaction_id"))),
														true);
												interactionsTab.addTab("Original response",
														responseMessageEditor.getComponent());
											}
										}
									}
									IMessageEditor requestMessageEditor = callbacks
											.createMessageEditor(taboratorMessageEditorController, false);
									requestMessageEditor.setMessage(collaboratorRequest, true);
									interactionsTab.addTab("Request to Collaborator",
											requestMessageEditor.getComponent());

									IMessageEditor responseMessageEditor = callbacks
											.createMessageEditor(taboratorMessageEditorController, false);
									responseMessageEditor.setMessage(collaboratorResponse, false);
									interactionsTab.addTab("Response from Collaborator",
											responseMessageEditor.getComponent());

									IMessageEditor re_res_burp_logs = callbacks
											.createMessageEditor(taboratorMessageEditorController, false);
									re_res_burp_logs.setMessage("Feature is still developing........".getBytes(), true);
									interactionsTab.addTab("Request & Response from burp logs",
											re_res_burp_logs.getComponent());

									interactionsTab.setSelectedIndex(1);

								}
								else if (interaction.get("type").equals("SMTP")) {
									Pattern pattern =  null;
									Matcher matcher = null;
									// stdout.println("Sender: ");
									// stdout.println(interaction.toString());
									byte[] conversation = helpers.base64Decode(interaction.get("conversation"));
									String conversationString = helpers.bytesToString(conversation);
									String boundary = "";
									String to = "";
									String from = "";
									String subject= "";
									
									
									if (interaction.containsKey("sender")) {
										from = helpers.bytesToString(helpers.base64Decode(interaction.get("sender")));
									} else {
										from ="Error: The old record don't match with this CollabFixed version!!!, please view in SMTP conversation";
									}
									
									if (interaction.containsKey("recipients")) {
										String[] recipients = interaction.get("recipients").replace("[", "").replace("]", "").split(",");
										for (String b64_recipients: recipients) {
											to += helpers.bytesToString(helpers.base64Decode(b64_recipients)) + "; ";
										}
									} else {
										to = "Error: The old record don't match with this CollabFixed version!!!, please view in SMTP conversation";
									}
									// stdout.println("Debug 1");
									pattern = Pattern.compile("Subject: (.*)");
									matcher = pattern.matcher(conversationString);
									if (matcher.find())
									{
										subject = matcher.group(1);
									}
									// stdout.println("Debug 2");
									pattern = Pattern.compile("boundary=\"(.*)\"");
									matcher = pattern.matcher(conversationString);
									if (matcher.find())
									{
										boundary = matcher.group(1);
									    // System.out.println("boundary: " + boundary);
									}
									// stdout.println("Debug 3");
									String body_html = "";
									
									
									
									if (conversationString.contains("Content-Type: text/html;")) {
										body_html = conversationString.split("Content-Type: text/html;")[1].split("--"+boundary)[0];
										 body_html = body_html.replace("=\r\n", "").replace("3D'", "").replace("3D\"", "");
									}
									
									System.out.println("body html: "+ body_html);
									
									String body_plain = "";
									
									if (conversationString.contains("Content-Type: text/plain;")) {
										body_plain = conversationString.split("Content-Type: text/plain;")[1].split("--"+boundary)[0];
									}
									
									
									
									System.out.println("body plain: "+ body_plain);
									
									// stdout.println("Debug 4");
									TaboratorMessageEditorController taboratorMessageEditorController= new TaboratorMessageEditorController();
									description.setText(
											"The Collaborator server received a SMTP connection from IP address "
													+ interaction.get("client_ip").toString() + " at "
													+ interaction.get("time_stamp").toString() + ".\n\n"
													+ "The email details were:\n\n" + "From: " + from + "\n\n" + "To: "
													//+ to + "\n\n" + "Message: \n" +message);
													+ to + "\n\n" + "Subject: "+ subject + "\n\n" );
									// stdout.println("Debug 5");
									IMessageEditor messageEditor = callbacks
											.createMessageEditor(taboratorMessageEditorController, false);
									messageEditor.setMessage(conversation, false);
//									stdout.println("Debug 6");
									String res = "HTTP/1.1 200 OK\r\n"
											+ "Server: Burp Collaborator https://burpcollaborator.net/\r\n"
											+ "X-Collaborator-Version: 4\r\n"
											+ "Content-Type: text/html\r\n"
											+ "Content-Length: 5400";
									// stdout.println("Debug 7");
									TaboratorMessageEditorController taboratorMessageEditorControllerEmail= new TaboratorMessageEditorController();
									IMessageEditor messageEditorEmailRender = callbacks
											.createMessageEditor(taboratorMessageEditorControllerEmail, false);
									// stdout.println("Debug 8");
									//Must set buildHttpService to able to use the render
									IHttpService httpServiceEmail = helpers.buildHttpService("www.google.com", 443, "HTTPS");
									taboratorMessageEditorControllerEmail.setHttpService(httpServiceEmail);
									//Must set setRequest to able to use the render
									taboratorMessageEditorControllerEmail.setRequest(helpers.stringToBytes("GET / HTTP/1.1"));
									// stdout.println("Debug 9");
									taboratorMessageEditorControllerEmail.setResponse(helpers.stringToBytes(res +body_html));
									messageEditorEmailRender.setMessage(helpers.stringToBytes(res + body_html), false);
									
									if (originalRequests.containsKey(interaction.get("interaction_id"))) {
										HashMap<String, String> requestInfo = originalRequests
												.get(interaction.get("interaction_id"));
										IHttpService httpService = helpers.buildHttpService(requestInfo.get("host"),
												Integer.decode(requestInfo.get("port")), requestInfo.get("protocol"));
										taboratorMessageEditorController.setHttpService(httpService);
										IMessageEditor requestMessageEditor = callbacks
												.createMessageEditor(taboratorMessageEditorController, false);
										if (requestInfo.get("request") != null) {
											requestMessageEditor.setMessage(
													helpers.stringToBytes(requestInfo.get("request")), true);
											interactionsTab.addTab("Original request",
													requestMessageEditor.getComponent());
										}
										if (originalResponses.containsKey(interaction.get("interaction_id"))) {
											taboratorMessageEditorController.setHttpService(httpService);
											IMessageEditor responseMessageEditor = callbacks
													.createMessageEditor(taboratorMessageEditorController, false);
											if (requestInfo.get("request") != null && originalResponses
													.get(interaction.get("interaction_id")) != null) {
												responseMessageEditor.setMessage(helpers.stringToBytes(
														originalResponses.get(interaction.get("interaction_id"))),
														true);
												interactionsTab.addTab("Original response",
														responseMessageEditor.getComponent());
											}
										}
									}
									 interactionsTab.addTab("SMTP Conversation", messageEditor.getComponent());
									interactionsTab.addTab("Email Render", messageEditorEmailRender.getComponent());
									interactionsTab.setSelectedIndex(1);
								} 
								description.setBorder(BorderFactory.createCompoundBorder(description.getBorder(),
										BorderFactory.createEmptyBorder(10, 10, 10, 10)));
								descriptionPanel.add(description);
								collaboratorClientSplit.setBottomComponent(interactionsTab);
								selectedRow = row;
								updateTab(false);
								setDividerLocation(collaboratorClientSplit, 0.5);
							}
						} else {
							if (!readRows.contains(id)) {
								c.setFont(c.getFont().deriveFont(Font.BOLD));
							}
						}
						if (colours.containsKey(id) && isSelected) {
							if (colours.get(id) == null) {
								setBackground(colours.get(id));
								colours.remove(id);
								textColours.remove(id);
							} else {
								setBackground(colours.get(id).darker());
							}
							setForeground(textColours.get(id));
							table.repaint();
							table.validate();
						} else if (colours.containsKey(id)) {
							setBackground(colours.get(id));
							setForeground(textColours.get(id));
						} else if (isSelected) {
							if (UIManager.getLookAndFeel().getID().equals("Darcula")) {
								setBackground(Color.decode("0x0d293e"));
								setForeground(Color.white);
							} else {
								setBackground(Color.decode("0xffc599"));
								setForeground(Color.black);
							}
						} else {
							setBackground(null);
							setForeground(null);
						}
						return c;
					}
				};
				collaboratorTable.setDefaultRenderer(Object.class, tableCellRender);
				collaboratorTable.setDefaultRenderer(Number.class, tableCellRender);

//                loadConfigFromFile();

				Runnable collaboratorRunnable = new Runnable() {
					public void run() {
						stdout.println("Collab_Fixed running...");

						// String payload1 = collaborator.generatePayload(true);
						// stdout.println("payload1: " + payload1);
						// stdout.println(collaborator.fetchCollaboratorInteractionsFor(payload1).toString());

						loadSettings();
						try {
							loadConfigFromFile2();

						} catch (IOException e1) {
							e1.printStackTrace();
						}

						interactionHistory = getLogs();

						for (Map.Entry<Integer, HashMap<String, String>> data : interactionHistory.entrySet()) {
							int id = data.getKey();
							HashMap<String, String> interaction = data.getValue();
							insertInteraction(interaction, id);
						}
						if (unread > 0) {
							updateTab(true);
						}

						while (running) {
							if (pollNow) {
								List<IBurpCollaboratorInteraction> interactions = collaborator
										.fetchAllCollaboratorInteractions();

								pollNowWithFixedCollab();

								stdout.println(interactions);

								if (interactions.size() > 0) {
									insertInteractions(interactions);
									saveLogs();
								}
								// pollNow = false;
							}
							try {
								isSleeping = true;
								pollThread.sleep(Long.parseLong(timePollAuto.getText()) * 1000);
								isSleeping = false;
								pollCounter++;
								if (pollCounter > 5) {
									if (createdCollaboratorPayload) {
										pollNow = true;
									}
									pollCounter = 0;
								}
							} catch (InterruptedException e) {
								if (shutdown) {
									stdout.println("Collab_Fixed shutdown.");
									return;
								} else {
									continue;
								}

							}
						}
						stdout.println("Collab_Fixed shutdown.");
					}

				};
				pollThread = new Thread(collaboratorRunnable);
				pollThread.start();
			}

			private void saveConfigToFile() throws IOException {
				stdout.println("SaveConfigToFile... ");
				//String biid_ = biidText.getText();
				//String collab_id_ = collabIdText.getText();
//				stdout.println("biid_: " + biid_);
//				stdout.println("collab_id_: " + collab_id_);
//				Map<String, Object> map = new HashMap<>();
//				map.put("biid", biid_);
//				map.put("collab_id", collab_id_);
//				map.put("cname", "");
//				try (Writer writer = new FileWriter(collab_fixed_config_file_name)) {
//					Gson gson = new GsonBuilder().create();
//
//					gson.toJson(map, writer);
//
//				} catch (IOException e2) {
//					// TODO Auto-generated catch block
//					e2.printStackTrace();
//				}
				
				// new
				String biids = "";
				String collab_ids = "";
			
				int numberItem = multipleBiid.getItemCount();
				for (int i=0; i< numberItem; i=i+1) {
					biids+=multipleBiid.getItemAt(i).toString().split(" - ")[1] + ", ";
					collab_ids+=multipleBiid.getItemAt(i).toString().split(" - ")[0] + ", ";
				}
				
				if (biids.contains(biidText.getText())==false  && collab_ids.contains(collabIdText.getText())==false) {
					biids+=biidText.getText();
					collab_ids+=collabIdText.getText();
				}
				
				stdout.println("biids: " + biids);
				stdout.println("collab_ids: " + collab_ids);
				
				Map<String, Object> map = new HashMap<>();
				map.put("biid", biids);
				map.put("collab_id", collab_ids);
				map.put("cname", "");
				try (Writer writer = new FileWriter(collab_fixed_config_file_name)) {
					Gson gson = new GsonBuilder().create();

					gson.toJson(map, writer);

				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				
				// new
				

			}

			private void loadConfigFromFile2() throws IOException {
				/*
				stdout.println("LoadConfigFromFile2... ");
				Gson gson = new Gson();
				// create a reader
				Reader reader = Files.newBufferedReader(Paths.get(collab_fixed_config_file_name));
				// convert JSON file to map
				Map<?, ?> map = gson.fromJson(reader, Map.class);
				System.out.println();
				// print map entries

				config_biid = (String) map.get("biid");
				config_cname = (String) map.get("cname");
				config_collab_id = (String) map.get("collab_id");

				btn_domain_id.setText(config_collab_id + ".oastify.com");

				l_cname.setText("CNAME: " + config_cname);
				reader.close();

				if (config_biid != "" && config_collab_id != "") {
					biidText.setText(config_biid);
					collabIdText.setText(config_collab_id);
				}
				*/
				
				// new 
				stdout.println("LoadConfigFromFile2... ");
				
				
				Gson gson = new Gson();
				// create a reader
				Reader reader = Files.newBufferedReader(Paths.get(collab_fixed_config_file_name));
				// convert JSON file to map
				Map<?, ?> map = gson.fromJson(reader, Map.class);
				System.out.println();
				// print map entries
				
				
				config_biids = (String) map.get("biid");
				config_cnames = (String) map.get("cname");
				config_collab_ids = (String) map.get("collab_id");
				stdout.println(config_biids);
				stdout.println(config_collab_ids);
				//multipleBiid.removeAllItems();
				
				
				String strItems = "";
				int numberItem = multipleBiid.getItemCount();
				for (int i=numberItem-1 ; i>=0; i--) {
					strItems+=multipleBiid.getItemAt(i).toString()+",";
				}
				
				
				if (config_biids != "" && config_collab_ids!="" && config_biids.length()>0  && config_collab_ids.length()>0 ) {
					// stdout.println("Debug 3");
					
					int numberBiids = config_biids.split(", ").length;
					for (int i = numberBiids-1; i >=0; i--) {
						// stdout.println("Debug 4");
						if (!strItems.contains(config_collab_ids.split(", ")[i])) {
							multipleBiid.addItem(config_collab_ids.split(", ")[i] + " - "+ config_biids.split(", ")[i]);
							
						}
						
						
					}
					
					String firstBiid = config_biids.split(", ")[0];
					String firstCallabId = config_collab_ids.split(", ")[0];
					
					sorter.setRowFilter(null);
					
					btn_domain_id.setText(firstCallabId + ".oastify.com");
	
					l_cname.setText("CNAME: " + config_cnames);
					
	
					if (firstBiid != "" && firstCallabId != "") {
						biidText.setText(firstBiid);
						collabIdText.setText(firstCallabId);
					}
					
					
				}
				reader.close();
				

			}

			private void saveLogs() {
				try {
					// FileOutputStream fileOut = new FileOutputStream(logspath.getText());
					FileOutputStream fileOut = new FileOutputStream(collab_fixed_logs_file_name);
					ObjectOutputStream out = new ObjectOutputStream(fileOut);
					out.writeObject(interactionHistory);
					out.close();
					fileOut.close();

					/// Save prefs (colour, readRow, comments,...)
					FileOutputStream fileOut2 = new FileOutputStream("collab_fixed_prefs.json");
					ObjectOutputStream out2 = new ObjectOutputStream(fileOut2);
					out2.writeObject(prefs);
					out2.close();
					fileOut2.close();
					///
					stdout.printf("Serialized data is saved in " + logspath.getText());
				} catch (IOException i) {
					i.printStackTrace();
				}

			}

			private HashMap<Integer, HashMap<String, String>> getLogs() {
				HashMap<Integer, HashMap<String, String>> interactionHistory_save = new HashMap<>();
				try {
					// FileInputStream fileIn = new FileInputStream(logspath.getText());
					FileInputStream fileIn = new FileInputStream(collab_fixed_logs_file_name);
					ObjectInputStream in = new ObjectInputStream(fileIn);
					interactionHistory_save = (HashMap<Integer, HashMap<String, String>>) in.readObject();
					in.close();
					fileIn.close();

					FileInputStream fileIn2 = new FileInputStream("collab_fixed_prefs.json");
					ObjectInputStream in2 = new ObjectInputStream(fileIn2);
					prefs = (Preferences) in2.readObject();
					in2.close();
					fileIn2.close();

					return interactionHistory_save;
				} catch (IOException i) {
					i.printStackTrace();
					return interactionHistory_save;
				} catch (ClassNotFoundException c) {
					stdout.println("Employee class not found");
					c.printStackTrace();
					return interactionHistory_save;
				}

			}

			private void pollNowWithFixedCollab() {
				try {
					stdout.println("PollNowWithFixedCollab...");
					stdout.println("_biid_:" + config_biids);
					if (config_biids != "") {
						
						int numberBiids = config_biids.split(", ").length;
						for (int i=0; i<numberBiids; i++) {
							JsonObject body = Maintest.getNewRecord(config_biids.split(", ")[i]);
							if (body.has("responses")) {

								stdout.println("Response: " + body.get("responses"));
								JsonArray jArray = (JsonArray) body.get("responses");
								if (jArray.size() > 0) {
									insertInteractions(jArray);
									saveLogs();
									// TimeUnit.SECONDS.sleep(1);
								}
							}
						}
						
						
					} else {
						stdout.println("Else 1");
					}

				} catch (IOException e1) {
					stdout.println("pollNowWithFixedCollab error:");
					e1.printStackTrace();
				}

			}
		});
	}

	private void insertInteraction(HashMap<String, String> interaction, int rowID) {

		String cmt = "";
		if (interaction.get("type").equals("HTTP")) {
			cmt = "...view host in Request...";
		} else {
			cmt = interaction.get("sub_domain");
		}
		model.addRow(new Object[] { rowID, interaction.get("time_stamp"), interaction.get("type"),
				interaction.get("client_ip"), interaction.get("interaction_id"), cmt });

		/*
		 * if (comments.size() > 0) { int actualID = getRealRowID(rowID); if (actualID >
		 * -1 && comments.containsKey(actualID)) { String comment =
		 * comments.get(actualID); model.setValueAt(comment, actualID, 5); } }
		 * 
		 * 
		 * if (interaction.get("type").equals("HTTP")) { byte[] collaboratorRequest =
		 * helpers.base64Decode(interaction.get("request")); if
		 * (helpers.indexOf(collaboratorRequest, helpers.stringToBytes("TaboratorCmd="),
		 * true, 0, collaboratorRequest.length) > -1) { IRequestInfo analyzedRequest =
		 * helpers.analyzeRequest(collaboratorRequest); List<IParameter> params =
		 * analyzedRequest.getParameters(); for (int i = 0; i < params.size(); i++) { if
		 * (params.get(i).getName().equals("TaboratorCmd")) { String[] commands =
		 * params.get(i).getValue().split(";"); for (int j = 0; j < commands.length;
		 * j++) { String[] command = commands[j].split(":"); if
		 * (command[0].equals("bgColour")) { try { Color colour =
		 * Color.decode(helpers.urlDecode(command[1])); colours.put(rowID, colour); }
		 * catch (NumberFormatException e) { } } else if
		 * (command[0].equals("textColour")) { try { Color colour =
		 * Color.decode(helpers.urlDecode(command[1])); textColours.put(rowID, colour);
		 * } catch (NumberFormatException e) { } } else if
		 * (command[0].equals("comment")) { String comment =
		 * helpers.urlDecode(command[1]); int actualID = getRealRowID(rowID); if
		 * (actualID > -1) { model.setValueAt(comment, actualID, 5); } } } break; } } }
		 * }
		 */
	}

	private int getRealRowID(int rowID) {
		int rowCount = collaboratorTable.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			int id = (int) collaboratorTable.getValueAt(i, 0);
			if (rowID == id) {
				return collaboratorTable.convertRowIndexToView(i);
			}
		}
		return -1;
	}

	private void loadSettings() {
		try {

			// stdout.println(" running...");
			HashMap<String, Integer> config = prefs.getSetting("config");

			if (config.size() > 0) {
				unread = config.get("unread");
				rowNumber = config.get("rowNumber");
			}
			interactionHistory = prefs.getSetting("interactionHistory");
			originalRequests = prefs.getSetting("originalRequests");
			originalResponses = prefs.getSetting("originalResponses");
			comments = prefs.getSetting("comments");
			colours = prefs.getSetting("colours");
			textColours = prefs.getSetting("textColours");
			readRows = prefs.getSetting("readRows");
		} catch (Throwable e) {
			System.err.println("Error reading settings:" + e);
		}
	}

	private void saveSettings() {
		try {
			HashMap<String, Integer> config = new HashMap<>();
			config.put("unread", unread);
			config.put("rowNumber", rowNumber);
			prefs.setSetting("config", config);
			prefs.setSetting("interactionHistory", interactionHistory);
			prefs.setSetting("originalRequests", originalRequests);
			prefs.setSetting("originalResponses", originalResponses);
			prefs.setSetting("readRows", readRows);
			prefs.setSetting("comments", comments);
			prefs.setSetting("colours", colours);
			prefs.setSetting("textColours", textColours);
		} catch (Throwable e) {
			System.err.println("Error saving settings:" + e);
		}
	}

	private void insertInteractions(List<IBurpCollaboratorInteraction> interactions) {
		boolean hasInteractions = false;
		int rowID = getMaxKeyInteractHistory(interactionHistory);
		for (int i = 0; i < interactions.size(); i++) {
			IBurpCollaboratorInteraction interaction = interactions.get(i);
			HashMap<String, String> interactionHistoryItem = new HashMap<>();
			rowID++;
			for (Map.Entry<String, String> interactionData : interaction.getProperties().entrySet()) {
				stdout.println("Key:");
				stdout.println(interactionData.getKey());

				stdout.println("Value:");
				stdout.println(interactionData.getValue());
				interactionHistoryItem.put(interactionData.getKey(), interactionData.getValue());
			}
			insertInteraction(interactionHistoryItem, rowID);
			unread++;
			interactionHistory.put(rowID, interactionHistoryItem);
			hasInteractions = true;
		}
		updateTab(hasInteractions);
	}

	private void insertInteractions(JsonArray interactions) {
		boolean hasInteractions = false;
		int rowID = getMaxKeyInteractHistory(interactionHistory);
		for (int i = 0; i < interactions.size(); i++) {
			HashMap<String, String> interactionHistoryItem = new HashMap<>();
			rowID++;

			JsonObject jsonObject = new JsonParser().parse(interactions.get(i).toString()).getAsJsonObject();
			jsonObject.keySet().forEach(keyStr -> {
				Object keyvalue = jsonObject.get(keyStr);

//		        stdout.println("key: "+ keyStr + " value: " + keyvalue);

				if (keyStr.contains("client") && keyStr.length()==6) {
					interactionHistoryItem.put("client_ip", keyvalue.toString().replace("\"", ""));
				}
				if (keyStr.contains("interactionString")) {
					interactionHistoryItem.put("interaction_id", keyvalue.toString().replace("\"", ""));
				}
				if (keyStr.contains("time")) {
					interactionHistoryItem.put("time_stamp",
							Instant.ofEpochMilli(Long.parseLong(keyvalue.toString().replace("\"", ""))).toString());
				}
				if (keyStr.contains("protocol")) {
					if (keyvalue.toString().contains("https")) {
						interactionHistoryItem.put("type", "HTTP");
						interactionHistoryItem.put("protocol", "HTTPS");
					} else {
						interactionHistoryItem.put("type", keyvalue.toString().toUpperCase().replace("\"", ""));
						interactionHistoryItem.put("protocol", keyvalue.toString().toUpperCase().replace("\"", ""));
					}

				}

				if (keyStr.contains("data")) {
					JsonObject jsonData = new JsonParser().parse(keyvalue.toString()).getAsJsonObject();

					if (jsonData.has("request")) {
						interactionHistoryItem.put("request", jsonData.get("request").toString().replace("\"", ""));
						interactionHistoryItem.put("response", jsonData.get("response").toString().replace("\"", ""));
					}

					if (jsonData.has("rawRequest")) {
						interactionHistoryItem.put("raw_query",
								jsonData.get("rawRequest").toString().replace("\"", ""));
						interactionHistoryItem.put("sub_domain",
								jsonData.get("subDomain").toString().replace("\"", ""));
					}

					if (jsonData.has("conversation")) {
						interactionHistoryItem.put("conversation",
								jsonData.get("conversation").toString().replace("\"", ""));
						interactionHistoryItem.put("sender",
								jsonData.get("sender").toString().replace("\"", ""));
						interactionHistoryItem.put("recipients",
								jsonData.get("recipients").toString().replace("\"", ""));
					}

				}

			});

//            stdout.println(interactionHistoryItem.get("type"));
			insertInteraction(interactionHistoryItem, rowID);
			unread++;
			interactionHistory.put(rowID, interactionHistoryItem);

			hasInteractions = true;
//            stdout.println("CCCCCCCC");
		}
		updateTab(hasInteractions);
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public Component getUiComponent() {
		return mainTab;
	}

	@Override
	public String getTabCaption() {
		return unread > 0 ? extensionName + " (" + unread + ")" : extensionName;
	}

	private void changeTabColour(JTabbedPane tabbedPane, final int tabIndex, boolean hasInteractions) {
		if (hasInteractions) {
			tabbedPane.setBackgroundAt(tabIndex, new Color(0xff6633));
		} else {
			tabbedPane.setBackgroundAt(tabIndex, defaultTabColour);
		}
	}

	private Color getDefaultTabColour() {
		if (running) {
			JTabbedPane tp = (JTabbedPane) BurpExtender.this.getUiComponent().getParent();
			int tIndex = getTabIndex(BurpExtender.this);
			if (tIndex > -1) {
				return tp.getBackgroundAt(tIndex);
			}
			return new Color(0x000000);
		}
		return null;
	}

	private void updateTab(boolean hasInteractions) {
		if (running) {
			JTabbedPane tp = (JTabbedPane) BurpExtender.this.getUiComponent().getParent();
			int tIndex = getTabIndex(BurpExtender.this);
			if (tIndex > -1) {
				tp.setTitleAt(tIndex, getTabCaption());
				changeTabColour(tp, tIndex, hasInteractions);
			}
		}
	}

	private int getTabIndex(ITab your_itab) {
		if (running) {
			JTabbedPane parent = (JTabbedPane) your_itab.getUiComponent().getParent();
			for (int i = 0; i < parent.getTabCount(); ++i) {
				if (parent.getTitleAt(i).contains(extensionName)) {
					return i;
				}
			}
		}
		return -1;
	}

	private JMenuItem generateMenuItem(JTable collaboratorTable, Color colour, String text, Color textColour) {
		JMenuItem item = new JMenuItem(text);
		item.setBackground(colour);
		item.setForeground(textColour);
		item.setOpaque(true);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] rows = collaboratorTable.getSelectedRows();
				for (int i = 0; i < rows.length; i++) {
					int realRow = collaboratorTable.convertRowIndexToModel(rows[i]);
					if (realRow > -1) {
						int id = (int) collaboratorTable.getModel().getValueAt(realRow, 0);
						colours.put(id, colour);
						textColours.put(id, textColour);
					}
				}
			}
		});
		return item;
	}

	public static JSplitPane setDividerLocation(final JSplitPane splitter, final double proportion) {
		if (splitter.isShowing()) {
			
			if ((splitter.getWidth() > 0) && (splitter.getHeight() > 0)) {
				splitter.setDividerLocation(proportion);
			} else {
				splitter.addComponentListener(new ComponentAdapter() {
					@Override
					public void componentResized(ComponentEvent ce) {
						splitter.removeComponentListener(this);
						setDividerLocation(splitter, proportion);
					}
				});
			}
		} else {
			splitter.addHierarchyListener(new HierarchyListener() {
				@Override
				public void hierarchyChanged(HierarchyEvent e) {
					if (((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) && splitter.isShowing()) {
						splitter.removeHierarchyListener(this);
						setDividerLocation(splitter, proportion);
					}
				}
			});
		}
		return splitter;
	}

	public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
		if (messageIsRequest) {
			byte[] request = messageInfo.getRequest();
			if (helpers.indexOf(request, helpers.stringToBytes(COLLABORATOR_PLACEHOLDER), true, 0,
					request.length) > -1) {
				String requestStr = helpers.bytesToString(request);
				Matcher m = Pattern.compile(COLLABORATOR_PLACEHOLDER.replace("$", "\\$")).matcher(requestStr);
				ArrayList<String> collaboratorPayloads = new ArrayList<>();
				while (m.find()) {
					String collaboratorPayloadID = collaborator.generatePayload(false);
					collaboratorPayloads.add(collaboratorPayloadID);
					requestStr = requestStr.replaceFirst(COLLABORATOR_PLACEHOLDER.replace("$", "\\$"),
							collaboratorPayloadID + "." + collaborator.getCollaboratorServerLocation());
					pollNow = true;
					createdCollaboratorPayload = true;
				}
				request = helpers.stringToBytes(requestStr);
				request = fixContentLength(request);
				messageInfo.setRequest(request);

				for (int i = 0; i < collaboratorPayloads.size(); i++) {
					HashMap<String, String> originalRequestsInfo = new HashMap<>();
					originalRequestsInfo.put("request", helpers.bytesToString(request));
					originalRequestsInfo.put("host", messageInfo.getHttpService().getHost());
					originalRequestsInfo.put("port", Integer.toString(messageInfo.getHttpService().getPort()));
					originalRequestsInfo.put("protocol", messageInfo.getHttpService().getProtocol());
					originalRequests.put(collaboratorPayloads.get(i), originalRequestsInfo);
				}
			}
		} else {
			byte[] response = messageInfo.getResponse();
			byte[] request = messageInfo.getRequest();
			for (Map.Entry<String, HashMap<String, String>> entry : originalRequests.entrySet()) {
				String payload = entry.getKey();
				if (!originalResponses.containsKey(payload)
						&& helpers.indexOf(request, helpers.stringToBytes(payload), true, 0, request.length) > -1) {
					originalResponses.put(payload, helpers.bytesToString(response));
				}
			}
		}
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	public static GridBagConstraints createConstraints(int x, int y, int gridWidth, int fill) {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = fill;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = x;
		c.gridy = y;
		c.ipadx = 0;
		c.ipady = 0;
		c.gridwidth = gridWidth;
		c.insets = new Insets(5, 5, 5, 5);
		return c;
	}

	public byte[] fixContentLength(byte[] request) {
		IRequestInfo analyzedRequest = helpers.analyzeRequest(request);
		if (countMatches(request, helpers.stringToBytes("Content-Length: ")) > 0) {
			int start = analyzedRequest.getBodyOffset();
			int contentLength = request.length - start;
			return setHeader(request, "Content-Length", Integer.toString(contentLength));
		} else {
			return request;
		}
	}

	private int getMaxKeyInteractHistory(HashMap<Integer, HashMap<String, String>> interactionHistory) {
		int max = 0;
		Iterator<Map.Entry<Integer, HashMap<String, String>>> it = interactionHistory.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, HashMap<String, String>> data = it.next();
			int id = data.getKey().intValue();
			if (id > max) {
				max = id;
			}
		}
		return max;
	}

	public int[] getHeaderOffsets(byte[] request, String header) {
		int i = 0;
		int end = request.length;
		while (i < end) {
			int line_start = i;
			while (i < end && request[i++] != ' ') {
			}
			byte[] header_name = Arrays.copyOfRange(request, line_start, i - 2);
			int headerValueStart = i;
			while (i < end && request[i++] != '\n') {
			}
			if (i == end) {
				break;
			}

			String header_str = helpers.bytesToString(header_name);

			if (header.equals(header_str)) {
				int[] offsets = { line_start, headerValueStart, i - 2 };
				return offsets;
			}

			if (i + 2 < end && request[i] == '\r' && request[i + 1] == '\n') {
				break;
			}
		}
		return null;
	}

	public byte[] setHeader(byte[] request, String header, String value) {
		int[] offsets = getHeaderOffsets(request, header);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			outputStream.write(Arrays.copyOfRange(request, 0, offsets[1]));
			outputStream.write(helpers.stringToBytes(value));
			outputStream.write(Arrays.copyOfRange(request, offsets[2], request.length));
			return outputStream.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Request creation unexpectedly failed");
		} catch (NullPointerException e) {
			throw new RuntimeException("Can't find the header");
		}
	}

	int countMatches(byte[] response, byte[] match) {
		int matches = 0;
		if (match.length < 4) {
			return matches;
		}

		int start = 0;
		while (start < response.length) {
			start = helpers.indexOf(response, match, true, start, response.length);
			if (start == -1)
				break;
			matches += 1;
			start += match.length;
		}

		return matches;
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
		int[] bounds = invocation.getSelectionBounds();

		switch (invocation.getInvocationContext()) {
		case IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST:
			break;
		default:
			return null;
		}
		List<JMenuItem> menu = new ArrayList<JMenuItem>();
		JMenu submenu = new JMenu(extensionName);
		JMenuItem createPayload = new JMenuItem("Insert Collaborator payload");
		createPayload.addActionListener(e -> {
			if (invocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST
					|| invocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_MESSAGE_VIEWER_REQUEST) {
				byte[] message = invocation.getSelectedMessages()[0].getRequest();
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				try {
					outputStream.write(Arrays.copyOfRange(message, 0, bounds[0]));
					outputStream.write(helpers.stringToBytes(collaborator.generatePayload(true)));
					outputStream.write(Arrays.copyOfRange(message, bounds[1], message.length));
					outputStream.flush();
					invocation.getSelectedMessages()[0].setRequest(outputStream.toByteArray());
					pollNow = true;
					createdCollaboratorPayload = true;
				} catch (IOException e1) {
					System.err.println(e1.toString());
				}
			}
		});
		JMenuItem createPlaceholder = new JMenuItem("Insert Collaborator placeholder");
		createPlaceholder.addActionListener(e -> {
			if (invocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST
					|| invocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_MESSAGE_VIEWER_REQUEST) {
				byte[] message = invocation.getSelectedMessages()[0].getRequest();
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				try {
					outputStream.write(Arrays.copyOfRange(message, 0, bounds[0]));
					outputStream.write(helpers.stringToBytes(COLLABORATOR_PLACEHOLDER));
					outputStream.write(Arrays.copyOfRange(message, bounds[1], message.length));
					outputStream.flush();
					invocation.getSelectedMessages()[0].setRequest(outputStream.toByteArray());
					pollNow = true;
					createdCollaboratorPayload = true;
				} catch (IOException e1) {
					System.err.println(e1.toString());
				}
			}
		});
		submenu.add(createPayload);
		submenu.add(createPlaceholder);
		menu.add(submenu);
		return menu;
	}
	
	public int chk = 0;
	public int chk1 = 0;
	
	@Override
	public void eventDispatched(AWTEvent event) {
		// stdout.println("eventDispatched");
		if (event.getSource() instanceof JTextArea) {

			// stdout.println("JS Beautify 2");
			JTextArea source = ((JTextArea) event.getSource());
			if (source.getClientProperty("hasListener") == null
					|| !((Boolean) source.getClientProperty("hasListener"))) {
				// stdout.println("JS Beautify 1");
				
				stdout.println("Adding AutoPayload Listener");
				
				/*  Disable auto suggestion
					AutoCompleter t = new AutoCompleter(source);
					source.getDocument().addDocumentListener(t);
					source.putClientProperty("hasListener", true);
					ExtensionState.getInstance().addListener(t);
				*/
				source.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {

						
						int keyCode = e.getKeyCode();
						if (e.isControlDown() && e.isShiftDown()) {
							// Check : Ctrl + Shift + F --> Formater HTLM, XML 
							if (keyCode == 70) {
								stdout.println("Formater HTLM, XML ");
								Document doc = Jsoup.parse(source.getSelectedText());
								// doc.outputSettings().prettyPrint(true);
								source.replaceRange(doc.body().html(),source.getSelectionStart(), source.getSelectionEnd());
							}
							// Check : Ctrl + Shift + J --> JSON Beautify
							if (keyCode == 74) {
								stdout.println("JS Beautify");
								// source.getText()
								String beautiful_json = (new JSONObject(source.getSelectedText())).toString(4);
								// source.setText(beautiful_json);
								source.replaceRange(beautiful_json, source.getSelectionStart(),
										source.getSelectionEnd());
								
							}
							
							/*  Disable auto suggestion
								// Check : Ctrl + Shift + N --> Show
								if (keyCode == 78) {
									if(chk1==0) {
										t.suggestionPane.setVisible(true);
										t.suggestionPane.toFront();
										chk1=1;
									} else {
										t.suggestionPane.setVisible(false);
										chk1=0;
									}
								}
								// Check : Ctrl + Shift + M  -> Show all
								if (keyCode == 78) {
									if(chk==0 ) {
										t.suggestionPane.setVisible(true);
										t.suggestionPane.toFront();
										t.suggestionsModel.addAll(ExtensionState.getInstance().keywords);
										
										Point p = MouseInfo.getPointerInfo().getLocation();	
						            	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
						            	t.suggestionPane.setSize(350, (screenSize.height*2)/3);
										t.suggestionPane.setLocation(p.x, screenSize.height/3);
										t.chk_pos = 1;
										chk=1;
									} else {
										t.suggestionPane.setVisible(false);
										t.chk_pos = 0;
										chk=0;
									}
								}
							*/
						}

						/*
						 * if (e.isControlDown() && e.isShiftDown() && keyCode == 74) {
						 * stdout.println("JS Beautify"); // source.getText() String beautiful_json =
						 * (new JSONObject(source.getSelectedText())).toString(4); //
						 * source.setText(beautiful_json); source.replaceRange(beautiful_json,
						 * source.getSelectionStart(), source.getSelectionEnd()); } // Check : Ctrl +
						 * Shift + H --> HTML Beautify else { if (e.isControlDown() && e.isShiftDown()
						 * && keyCode == 72) { // source.getText() stdout.println("HTML Beautify");
						 * //Document doc = Jsoup.parse(source.getSelectedText()); //
						 * source.setText(beautiful_json); //source.replaceRange(doc.toString(),
						 * source.getSelectionStart(), source.getSelectionEnd()); } }
						 */

					}
				});
			}
		}

	}
	
	@Override
	public void extensionUnloaded() {
		//
		// Need to remove eventDispatched JS Beautify after JS Beautify
		//
		shutdown = true;
		running = false;
		stdout.println(extensionName + " unloaded");
		pollThread.interrupt();
		saveSettings();
		
		
		stdout.println("removing listeners");
		stdout.println(Arrays.toString(Toolkit.getDefaultToolkit().getAWTEventListeners()));

		Toolkit.getDefaultToolkit().removeAWTEventListener(this);
		for (AutoCompleter listener : ExtensionState.getInstance().getListeners()) {
			listener.detachFromSource();
			listener.getSource().getDocument().removeDocumentListener(listener);
		}

	}

	@Override
	public void initialize(MontoyaApi api) {
		// TODO Auto-generated method stub
		Logging logging = api.logging();

        // write a message to our output stream
        logging.logToOutput("Hello MontoyaApi.");

        // write a message to our error stream
        // logging.logToError("Hello error.");

        // write a message to the Burp alerts tab
        // logging.raiseInfoEvent("Hello info event.");
        // logging.raiseDebugEvent("Hello debug event.");
        // logging.raiseErrorEvent("Hello error event.");
        // logging.raiseCriticalEvent("Hello critical event.");

        // throw an exception that will appear in our error stream
        // throw new RuntimeException("Hello exception.");
        
        collab = api.collaborator();
        
	}
}
