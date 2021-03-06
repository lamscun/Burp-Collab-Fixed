package burp;

import com.coreyd97.BurpExtenderUtilities.DefaultGsonProvider;
import com.coreyd97.BurpExtenderUtilities.ILogProvider;
import com.coreyd97.BurpExtenderUtilities.Maintest;
import com.coreyd97.BurpExtenderUtilities.Preferences;
import com.coreyd97.BurpExtenderUtilities.ProjectSettingStore;
import com.google.gson.reflect.TypeToken;
import com.google.gson.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class BurpExtender implements IBurpExtender, ITab, IExtensionStateListener, IContextMenuFactory, IHttpListener {
	private String extensionName = "Collab_Fixed_v2";
	private String extensionVersion = "2.0";
	private IBurpExtenderCallbacks callbacks;
	private IExtensionHelpers helpers;
	private PrintWriter stderr;
	private PrintWriter stdout;
	private JPanel panel;
	private volatile boolean running;
	private int unread = 0;
	private ArrayList<Integer> readRows = new ArrayList<>();
	private IBurpCollaboratorClientContext collaborator = null;
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
	private boolean pollNow = false;
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

	private String _biid_ = "";
	private String _cname_ = "";
	private String _collab_id_ = "";

	public static JTextField filepath = new JTextField();

	public static JTextPane l_cname = new JTextPane();
	public static JTextPane l_domain_id = new JTextPane();

	public static JTextField biidText = new JTextField();
	public static JTextField collabIdText = new JTextField();
	public static JTextField logspath = null;

	public static String collab_fixed_config_file_name = "collab_fixed_config.json";
	public static String collab_fixed_logs_file_name = "collab_fixed_logs.json";
	 
	
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
						"To use Collab_Fixed right click in the repeater request tab and select \"Collab_Fixed->Insert Collaborator payload\". Use \"Collab_Fixed->Insert Collaborator placeholder\" to insert a placeholder that will be replaced by a Collaborator payload in every request. The Collab_Fixed placeholder also works in other Burp tools. You can also use the buttons in the Collab_Fixed tab to create a payload and poll now.");
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
				filter.addItem("All interactions");
				filter.addItem("DNS");
				filter.addItem("HTTP");
				filter.addItem("SMTP");
				filter.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (sorter == null) {
							return;
						}
						selectedRow = -1;
						if (filter.getSelectedIndex() == 0) {
							sorter.setRowFilter(null);
						} else {
							sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
								@Override
								public boolean include(RowFilter.Entry<? extends TableModel, ? extends Integer> row) {
									return row.getValue(2).equals(filter.getSelectedItem().toString());
								}
							});
						}
					}
				});
				
				/*
				JButton createCollaboratorPayloadWithTaboratorCmd = new JButton("Collab Fixed commands & copy");
				createCollaboratorPayloadWithTaboratorCmd.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						createdCollaboratorPayload = true;
						String payload = collaborator.generatePayload(true)
								+ "?Collab_Fixed=comment:Test;bgColour:0x000000;textColour:0xffffff";
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(payload),
								null);
					}
				});
				*/

				JButton pollButton = new JButton("Poll");
				
				JButton randomButton = new JButton("Random ID");
				
				JTextField numberOfPayloads = new JTextField("1");
				numberOfPayloads.setMinimumSize(new Dimension(25, 25));

				JTextField timePollAuto = new JTextField("5");
				timePollAuto.setMinimumSize(new Dimension(25, 25));

				String jarPath = "";
				try {
					jarPath = MainCalss.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
				} catch (URISyntaxException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

				filepath = new JTextField(jarPath + collab_fixed_config_file_name);
				filepath.setMinimumSize(new Dimension(200, 25));
				logspath = new JTextField(jarPath + collab_fixed_logs_file_name);
				logspath.setMinimumSize(new Dimension(200, 25));



				l_domain_id = new JTextPane();
				l_domain_id.setText("Fixed Domain:  Null");
				l_domain_id.setEditable(false);
				l_domain_id.setBackground(null);
				l_domain_id.setBorder(null);
				l_domain_id.setMaximumSize(new Dimension(450, 30));
				l_domain_id.setPreferredSize(new Dimension(450, 25));
				l_domain_id.setMinimumSize(new Dimension(250, 25));
				
				
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
						pollNow = true;
                        // pollNowWithFixedCollab();
						if (isSleeping) {
							pollThread.interrupt();
						}
					}
				});
				
				
				pollButton.setPreferredSize(new Dimension(80, 30));
				pollButton.setMaximumSize(new Dimension(80, 30));
				
				createCollaboratorPayload.setPreferredSize(new Dimension(180, 30));
				createCollaboratorPayload.setMaximumSize(new Dimension(180, 30));
				
				topPanel.add(l_collabIdText, createConstraints(1, 1, 1, GridBagConstraints.NONE));
				topPanel.add(collabIdText, createConstraints(2, 1, 1, GridBagConstraints.NONE));
				topPanel.add(l_domain_id, createConstraints(3, 1, 1, GridBagConstraints.NONE));
				topPanel.add(pollButton, createConstraints(4, 1, 1, GridBagConstraints.NONE));
				
				topPanel.add(l_biidText, createConstraints(1, 2, 1, GridBagConstraints.NONE));
				topPanel.add(biidText, createConstraints(2, 2, 1, GridBagConstraints.NONE));
				topPanel.add(l_cname, createConstraints(3, 2, 1, GridBagConstraints.NONE));
				topPanel.add(timePollAuto, createConstraints(4, 2, 1, GridBagConstraints.NONE));
				
				
				topPanel.add(b_loadConfig, createConstraints(1, 3, 1, GridBagConstraints.NONE));
				topPanel.add(createCollaboratorPayload, createConstraints(2, 3, 1, GridBagConstraints.NONE));
				topPanel.add(filter, createConstraints(3, 3, 1, GridBagConstraints.NONE));
				topPanel.add(exportBtn, createConstraints(4, 3, 1, GridBagConstraints.NONE));
				topPanel.add(randomButton, createConstraints(5, 3, 1, GridBagConstraints.NONE));
				
				
				
				randomButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Random r = new Random();
						String[] list_random_biids = {"xxxx -- yyy", "xxxx -- yy", "xxxx -- yy", "xxxx -- yy", "xxxx -- yy"};
						String random_biid = list_random_biids[r.nextInt(list_random_biids.length)];
						biidText.setText(random_biid.split(" -- ")[0]);
						collabIdText.setText(random_biid.split(" -- ")[1].replace(".oastify.com", ""));
						
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
				JSplitPane collaboratorClientSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
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
				JMenuItem clearMenuItem = new JMenuItem("Clear");
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
						}
						collaboratorTable.clearSelection();
					}
				});
				popupMenu.add(clearMenuItem);
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
									description.setText("DNS lookup of type "
											+ interaction.get("query_type") + " for the domain name: "
											+ interaction.get("interaction_id")
											+ collaborator.getCollaboratorServerLocation() + ".\n\n"
											+ "From IP address: " + interaction.get("client_ip")
											+ " at " + interaction.get("time_stamp") + "\n\n"
											+ interaction.get("sub_domain"));
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
								} else if (interaction.get("type").equals("SMTP")) {
									byte[] conversation = helpers.base64Decode(interaction.get("conversation"));
									String conversationString = helpers.bytesToString(conversation);
									String to = "";
									String from = "";
									String message = "";
									Matcher m = Pattern
											.compile("^RCPT TO:(.+?)$", Pattern.CASE_INSENSITIVE + Pattern.MULTILINE)
											.matcher(conversationString);
									if (m.find()) {
										to = m.group(1).trim();
									}
									m = Pattern
											.compile("^MAIL From:(.+)?$", Pattern.CASE_INSENSITIVE + Pattern.MULTILINE)
											.matcher(conversationString);
									if (m.find()) {
										from = m.group(1).trim();
									}
									m = Pattern
											.compile("^DATA[\\r\\n]+([\\d\\D]+)?[\\r\\n]+[.][\\r\\n]+",
													Pattern.CASE_INSENSITIVE + Pattern.MULTILINE)
											.matcher(conversationString);
									if (m.find()) {
										message = m.group(1).trim();
									}
									TaboratorMessageEditorController taboratorMessageEditorController = new TaboratorMessageEditorController();
									description.setText(
											"The Collaborator server received a SMTP connection from IP address "
													+ interaction.get("client_ip") + " at "
													+ interaction.get("time_stamp") + ".\n\n"
													+ "The email details were:\n\n" + "From: " + from + "\n\n" + "To: "
													+ to + "\n\n" + "Message: \n" + message);
									IMessageEditor messageEditor = callbacks
											.createMessageEditor(taboratorMessageEditorController, false);
									messageEditor.setMessage(conversation, false);
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
									interactionsTab.setSelectedIndex(1);
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
									stdout.println(collaboratorResponse.toString());
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
									responseMessageEditor.setMessage(collaboratorResponse, true);
									interactionsTab.addTab("Response from Collaborator",
											responseMessageEditor.getComponent());
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
								pollNow = false;
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
				String biid_ = biidText.getText();
				String collab_id_ = collabIdText.getText();
				
				stdout.println("biid_: " + biid_);
				stdout.println("collab_id_: " + collab_id_);
				
				Map<String, Object> map = new HashMap<>();
				map.put("biid", biid_);
				map.put("collab_id", collab_id_);
				map.put("cname", "");
				try (Writer writer = new FileWriter(collab_fixed_config_file_name)) {
					Gson gson = new GsonBuilder().create();

					gson.toJson(map, writer);

				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

			}

			private void loadConfigFromFile2() throws IOException {
				stdout.println("LoadConfigFromFile2... ");
				Gson gson = new Gson();
				// create a reader
				Reader reader = Files.newBufferedReader(Paths.get(collab_fixed_config_file_name));
				// convert JSON file to map
				Map<?, ?> map = gson.fromJson(reader, Map.class);
				System.out.println();
				// print map entries

				_biid_ = (String) map.get("biid");
				_cname_ = (String) map.get("cname");
				_collab_id_ = (String) map.get("collab_id");
				
				
				
			    StyledDocument doc = l_domain_id.getStyledDocument();

			    Style style = l_domain_id.addStyle("Red", null);
			    StyleConstants.setForeground(style, Color.red);

			    
				l_domain_id.setText("Fixed Domain:  " + _collab_id_ + ".oastify.com");
				
				l_cname.setText("CNAME: " + _cname_);
				reader.close();

				if (_biid_ != "" && _collab_id_ != "") {
					biidText.setText(_biid_);
					collabIdText.setText(_collab_id_);
				}

			}

			private void saveLogs() {
				try {
					// FileOutputStream fileOut = new FileOutputStream(logspath.getText());
					FileOutputStream fileOut = new FileOutputStream(collab_fixed_logs_file_name);
					ObjectOutputStream out = new ObjectOutputStream(fileOut);
					out.writeObject(interactionHistory);
					out.close();
					fileOut.close();
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
					stdout.println("_biid_:" + _biid_);
					if (_biid_ != "") {

						JsonObject body = Maintest.getNewRecord(_biid_);
						if (body.has("responses")) {

							stdout.println("Response: " + body.get("responses"));
							JsonArray jArray = (JsonArray) body.get("responses");
							if (jArray.size() > 0) {
								insertInteractions(jArray);
								saveLogs();
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
	    if (interaction.get("type").equals("HTTP")){
			cmt = "...view host in Request...";
		} else {
			cmt = interaction.get("sub_domain");
		}
		model.addRow(new Object[] { rowID, interaction.get("time_stamp"), interaction.get("type"),
				interaction.get("client_ip"), interaction.get("interaction_id"), cmt });
		
		/*
		if (comments.size() > 0) {
			int actualID = getRealRowID(rowID);
			if (actualID > -1 && comments.containsKey(actualID)) {
				String comment = comments.get(actualID);
				model.setValueAt(comment, actualID, 5);
			}
		}
		
		
		if (interaction.get("type").equals("HTTP")) {

			byte[] collaboratorRequest = helpers.base64Decode(interaction.get("request"));

			if (helpers.indexOf(collaboratorRequest, helpers.stringToBytes("TaboratorCmd="), true, 0,
					collaboratorRequest.length) > -1) {
				IRequestInfo analyzedRequest = helpers.analyzeRequest(collaboratorRequest);
				List<IParameter> params = analyzedRequest.getParameters();
				for (int i = 0; i < params.size(); i++) {
					if (params.get(i).getName().equals("TaboratorCmd")) {
						String[] commands = params.get(i).getValue().split(";");
						for (int j = 0; j < commands.length; j++) {
							String[] command = commands[j].split(":");
							if (command[0].equals("bgColour")) {
								try {
									Color colour = Color.decode(helpers.urlDecode(command[1]));
									colours.put(rowID, colour);
								} catch (NumberFormatException e) {

								}
							} else if (command[0].equals("textColour")) {
								try {
									Color colour = Color.decode(helpers.urlDecode(command[1]));
									textColours.put(rowID, colour);
								} catch (NumberFormatException e) {

								}
							} else if (command[0].equals("comment")) {
								String comment = helpers.urlDecode(command[1]);
								int actualID = getRealRowID(rowID);
								if (actualID > -1) {
									model.setValueAt(comment, actualID, 5);
								}
							}
						}
						break;
					}
				}
			}
		}
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
		for (int i = 0; i < interactions.size(); i++) {
			IBurpCollaboratorInteraction interaction = interactions.get(i);
			HashMap<String, String> interactionHistoryItem = new HashMap<>();
			rowNumber++;
			int rowID = rowNumber;
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
		for (int i = 0; i < interactions.size(); i++) {
			HashMap<String, String> interactionHistoryItem = new HashMap<>();
			rowNumber++;
			int rowID = rowNumber;

			JsonObject jsonObject = new JsonParser().parse(interactions.get(i).toString()).getAsJsonObject();
			jsonObject.keySet().forEach(keyStr -> {
				Object keyvalue = jsonObject.get(keyStr);

//		        stdout.println("key: "+ keyStr + " value: " + keyvalue);

				if (keyStr.contains("client")) {
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
		return panel;
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
	private GridBagConstraints createConstraints(int x, int y, int gridWidth, int fill) {
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

	@Override
	public void extensionUnloaded() {
		shutdown = true;
		running = false;
		stdout.println(extensionName + " unloaded");
		pollThread.interrupt();
		saveSettings();
	}
