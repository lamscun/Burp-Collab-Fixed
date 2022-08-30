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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
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
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

/* loaded from: collab_fixed_v5.jar:burp/BurpExtender.class */
public class BurpExtender implements IBurpExtender, ITab, IExtensionStateListener, IContextMenuFactory, IHttpListener {
	private String extensionName = "Collab_Fixed_v4";
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

	private String config_biid = "";
	private String config_cname = "";
	private String config_collab_id = "";

	public static JTextField filepath = new JTextField();

	public static JTextPane l_cname = new JTextPane();
	public static JButton btn_domain_id = new JButton();

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
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(BurpExtender.btn_domain_id.getText()), (ClipboardOwner) null);
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
				topPanel.add(btn_domain_id, createConstraints(3, 1, 1, GridBagConstraints.NONE));
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
						int answer = JOptionPane.showConfirmDialog((Component) null, "This will remove your current biid, it can not retake, are you sure?");
                        if (answer == 0) {
                            Random r = new Random();
                            String[] list_random_biids = {"g8zhf3fhiA04IZyUksUl8IM5yQDtUGZMFhWQ0Zwba7k%3d -- l2oayratq3wrna9hjnqvoge3quwkk9.oastify.com", "dapbxfDesRu8uCRM3YKjiujbXEtT5H5QWJRYRHWfk3g%3d -- qicjsvj17uupxp822mz46wuyppvfj4.oastify.com", "Lv5%2bUD2sn9%2bcgWr7OmI2catzqq6Aj2WvbS7Zk0NIqzQ%3d -- woo4v23fmnc93q5fjab3m7mz0q6gu5.oastify.com", "0rqBpwTsnjmZY6OnR3Wk3aJnK8KAF2m3lECbaUZ6%2f38%3d -- 9s2a0fj03zhar54y1o2nhat3cuik69.oastify.com", "tvPrEqG3356D6LPInQ54DlRzB9Kp50rsSp2BvY66y8c%3d -- zux7p7f517wh7y7tpfzr3snnkeq4et.oastify.com", "SG93d%2by%2fj1pcIpLlII2OpRm8VvZ9tsRCbL5peqmTHy4%3d -- 2dg3l5lulggzky8c73jn3q3cs3ytmi.oastify.com", "zIP%2fFwXoEhRlQZjLP1IyCXMetbdBnzbLd0I4c8JiKNg%3d -- i4nbf4ezyem3dgwxxl9v6tdtikoacz.oastify.com", "o06hqWFy52lk6u%2fYWRqOl0a4ZlCKJQgelyDq3E97FX8%3d -- uki2ba9lx5gc46utvescuijublhb50.oastify.com", "92mgjZMHNFne62DWesaFxFx9E2UhZUgtGzvVUljPAX0%3d -- eq8o0w632vld6kcqv4jfk8v7zy5otd.oastify.com", "gf%2bi3UoCU%2bqzjWo3iQ%2fLhrkd8KYvCX%2fTA%2fob0HpEqRk%3d -- x1z8ijtd6f5zf7s7betx8w8i79dz1o.oastify.com", "pnRZkS1hYcC5vHRj2l3Ig6JqtoUIRfqzWJCMI5f87wA%3d -- rc7ytytm59lmnqu3lbeui61s9jf93y.oastify.com", "iR0E%2bhPpYsOhv2iEB%2fSuE3JYFTD9SDCAKR1Z8VyaVHQ%3d -- rc7bhuodk21vkzl7887jdw1zdqjg75.oastify.com", "14vvhRJS7MW7cZswVcLzyXtSorpSCEClGNF8fWE9%2bdw%3d -- ar5ikpc3nb0quy2wi3vnbjdtjkpadz.oastify.com", "NvO7jCZSfkIeTSY%2bJJ9677OkknC%2fj5VcUKVxx4F%2f6Oc%3d -- 48aczit8p4dtg9fytp9yqwsublhb50.oastify.com", "HLbL3ig8BeD7Sh1ZKKJ3zFfReKGU9xwQoxvdrBFwHoc%3d -- msirnonq5rkvbciflrncipinsey4mt.oastify.com", "1tCKa8dfFAO4AhGbBxLCBmpI%2bBvFF2eYwXc7QPEGYc8%3d -- n8wagnz2rai8w66a1bmihgauyl4bs0.oastify.com", "pP1OB%2bEoQW6hYhqV0pt1QIuuItz3J1lIMl5zsvxbO10%3d -- hsdksmffwvjcq0432s91t453fulk99.oastify.com", "nVszODzYN6WoCs9yjLriALPnJUBaVqV8kihygU%2b7ZZA%3d -- ao2qepex3fg9pyqhdvu7ngikpbv1jq.oastify.com", "bW9bXgGebEIbKeoQr78SIGHljOUVJ7T%2fSC0mlx60l74%3d -- q1srxttrjw7rishrrn3gfg2iy94zso.oastify.com", "iozsuABZP97DHQ7Vm9uix3Q6gGmXzxLKmgK3qIlm9rg%3d -- y98110ga1batryi95n49wvtk3b91xq.oastify.com", "WXow6u9y%2btKOZJDm5OrWWleWyegR3Bgonx2nFc0rUX4%3d -- ua81l2rcgqpcas74c4k9emhrfil89x.oastify.com", "oDYufqvnZemdyEY%2bcH29uUteg7ydyBPh9I7fK7%2b7q88%3d -- zdgn1wz9473buqaip1rjpxo6nxtnhc.oastify.com", "wdjte9kOwnYhZapl5Ucm2QVU%2buGdzQw%2fPloWi8zCnO8%3d -- ymob5kh3p43v89ud4vrmgddywp2fq4.oastify.com", "5BA0OZ18M7hv3c8q3DUhFwqrQUwtR2DRODs1o4dndhM%3d -- 7dlmdaovrg9v781mha3yam0u7ldb10.oastify.com", "lTKqBFOSEdIFEzRbnz0AkClv%2fSS1Fi8UnXoA9iAcDLY%3d -- kocanh6gy0abxxfswxd9r0c8uz0poe.oastify.com", "z4Rer0Tvh55k4WA50Uflbp%2fR%2fobjAiTCmQjbLTMNisI%3d -- cyeta5y0qabjzj82bm14r5ikrbx1lq.oastify.com", "u0dm3zYNpn1cD50%2fL95goCqAEDaJ%2bAelxMX7nOJ5Ec8%3d -- 6dkqeyklsxe8lgdw6me7rkpb72ds1h.oastify.com", "pFL1UNU5j8mRgkxFz%2bOK2QcTz59GJGdkmsTHL7DyHXc%3d -- 8irprgm90zs2herfem1py1vqdhj77w.oastify.com", "VVyBpzhivexigzllsF%2fy8NYrsVNA2v%2fnXZ9X%2fqGY9P0%3d -- the7qfueaw6ww3b929075kfl5cb2zr.oastify.com", "qM2zcqeMFEZXSSD0%2fEH%2bJhzLfsCIZwA4xip7vpgZWe8%3d -- v73e4d90f3fuq1wj8rgi9fky0p6fu4.oastify.com", "ErWLa2U%2fh4Tdx0vZ4nbDXZWDOox6nn2BwRCclOiPY7U%3d -- lf4t1avkh44fyk2dzfmhquslcci26r.oastify.com", "gcGNPh3XTj%2bZzmdAF%2bfm1pwS3jUC%2blKvUFa0bae1TIs%3d -- zjm18otv4w57vmr8b23ulqu5vw1mpb.oastify.com", "FOWy5hrz%2fuQuwEor2bKRcMudcWo39U9qF1biEdAJvwQ%3d -- 6v28xhg3zh3402th5rbomil1rsxil7.oastify.com", "xiB27M5f3s51l0Nfvevwo4BcPYKLLDaJgJ16TFHwQZQ%3d -- pngevy0i59baloyfc7yqycpvpmvcj1.oastify.com", "pZKaqhqqHnvET3IK90B5Xd7qLD7tlav9BPkJsT5OIx8%3d -- bhwubpdqskqqcmv3zfo5p66cl3rtfi.oastify.com", "VuY6cGRUMlabjMAAD7AvxSvLWIMLwpcFZW0%2f7O6ICiA%3d -- 9lv1dq1wxkhswbejlp8uaen86zcp0e.oastify.com", "KpYxHWZ5VqGWC8tEwC2x%2b3J2y4G1sntcXWay%2f4Hr7VM%3d -- 2qtvkynn2w07kv0lheedaiuaz15rtg.oastify.com", "fJHYxaumvak7Jids51P6Ttoql5bh81nJgSzuSWmKvA8%3d -- d4ix94njamp0jxeh0e8wmwa950bqzf.oastify.com", "IhKjVO4N5dLlLw8vea3v4ZEpnfTuFJMdMpau0UyQux4%3d -- 6krlfctc9jbmdndwltemnxjpagg64v.oastify.com", "XYeFAyfCMoUP5M5jx4iojZD%2bG84JSj7eiOeeAJj3ZtA%3d -- nh87mt01tnhz5uzcyqlrbwnmjdp3ds.oastify.com", "T9bjKv%2bskL8fCtmhkATam0uvRJ6aC4W1aNxRZqidi1M%3d -- sb77tc9jzuhyxlvhl4arf8yhk8qyen.oastify.com", "5pJNSWni3NqX6fKHcQYmIhdZQ8464kdY7sPEdt35jYg%3d -- c2f1zk9o2v5m3jpx5kk9kuol6cc20r.oastify.com", "8vGGFn3j79HT4Q2lVxk3AeoQ9na1cqYb8T%2fMy18ZUVY%3d -- 288o1ucq8ktt1zqfydcd7i0xmoseg3.oastify.com", "dbLaJroPqLKrlyua4tEAA3KP9FGrCpnvI%2bX35K5%2bdZc%3d -- rlg3f0vsmghl1e2iefdwkyf04rahy6.oastify.com", "9SXevm7yc7PbqfXhdPIciXk6V19OzYje%2fRNbyFL63zU%3d -- o9y0i6h59lhcv8mgg6c3hza9309qxf.oastify.com", "UYUwEi0UJAlDL9C7xYwKQ9UrrvAotWNs%2fOkGqYnVhtw%3d -- qgat9o2s6lh6umfyh0vyai70qrwhk6.oastify.com", "NsPhCqbY2%2bBr9hf35SkSv8iY%2bEJ%2bN4WNFoAsjR30sOQ%3d -- 8irfifs86ipm1eh89f1y2ne9b0hq5f.oastify.com", "JDfNs%2fwaU0CgYsrkMaP2Sx21PVmMN%2b38Yd5ApU7J6XE%3d -- egy3mxbqyohwdx780nb8caf4dvjl7a.oastify.com", "qFEW6gsnYC3Io2ntp06g%2f1XdLTpfq1yLErOtnlnP72U%3d -- gwg0l6lshiuy4gbxlsgd7g9fz65wtl.oastify.com", "OW49nkUz1%2foIZt4NZv5c6sqbIn4BcVyiKvbyphgf5%2bI%3d -- sfsfdsf.moen4jgf0clyiqyp4qsf2ulsijo9cy.oastify.com", "fX7cuPaz7V6W%2bFsp1OU9litdCVcm3gTBAde9g21KGA4%3d -- fk35nsogl6gup0plqkdv9biwzn5dt2.oastify.com", "RRpM1FmGbKiby5UOBwjWLX5kzcCQ5QuL96hgm8X7Oyc%3d -- 3lp9c8frv701knfz08czqi925tbjz8.oastify.com", "Lj%2fR20dD2IBifoo7%2bndgEPqEMGbCf%2fUUIPph0365N14%3d -- hf0nh6d07sr88e7f9yl7vkmdo4uuij.oastify.com", "OyhMQZrRDjMfUL9kNt8jiq5SIbod9R7%2bs0i9M11B7bY%3d -- uca3yd7z061mc9setewvgshem5svgk.oastify.com", "2v6t2%2bE2RKxepAB8pQEtgJyM1DD9dG%2fhxQfniowzxq0%3d -- 9u4h52fdjqx3omuj3jtl03fv4macy1.oastify.com", "p1s%2bG7oNkQ4Ssrd%2b%2fYEoKjQ%2bdVXP2V8eUs3MYYdxKio%3d -- xhim80uhs1jcrqut47bze98f66cw0l.oastify.com", "CA1Vba3UC1FjYuD9AN6PlCoWuleVtYMcKLz7485BEDI%3d -- 7ai0pfniyieybfrqqumpmxzlico2cr.oastify.com", "e2vZxDm770pS4zv8yBDmsH%2fAjV8aM1K8snk284EMGSQ%3d -- o5usx37ki72z52ekr1002jhsejk98y.oastify.com", "xFwosAa2YBvFOeC8SSMu%2b%2f09TnDWvILgueHczpTVb8Q%3d -- 3z3t59dy1vc927kmdxc00wzmud03os.oastify.com", "uP2aS6nJPDkwAw%2fYmNLR7zauVsqFEQl5w5drxEqBEm0%3d -- pha65s2srll2i2sjh3b56zt3uu0ko9.oastify.com", "YiLAJDJn%2fzgPOSw34l4uI3Jqv0uv9WeTvN3zd33XkRA%3d -- 75ai13lvlmf6dw2prrr6um7fc6iw6l.oastify.com", "4OuiZVN4gvvGUo64xOJbScdkjJSgOrBDkbdAFCcGuIE%3d -- ssssssssssssss.626liircfzpf1fdiunv48usokfq5eu.oastify.com", "tBrNry0fMELkSE7mnh34Gnpn3sY8r2RRGnEzPl9i2UI%3d -- hpa14mqtaq6wkyw5jax1apqpigo6cv.oastify.com", "BXjB%2b%2b37S%2fzWCSdk%2fEe4Zk87cEdIETINPhBYz10wmPY%3d -- bn2nkr237htuuf6p0zyclzbwvn1dp2.oastify.com", "rhvjLAHu253uaZeM8%2bH0KDHldFXHXb5%2b76XREqPYD3M%3d -- aguer6td5weh36b1tluojp6ojfp5du.oastify.com", "I%2b93dVfbyzh8%2bvmitC04DvhzNTnx%2f6bMiYePxJ3%2ffS0%3d -- wtt65xr474lsoekc3c9m2tj8xz3pre.oastify.com", "PEFClwEt9fqGVw0obcxiFFlXQQcgKVpyCJUGgx%2bnAqw%3d -- rxsfedwptodug52vzc0x93oxdoje73.oastify.com", "TFdLpaEUZXhzRGIJ0Crt6D6Xfd6PHx%2brO2AK8KkhPOg%3d -- bcrm6l0odpqdh63rrsgtvr0fn6twhl.oastify.com", "FufIw7X4CgebdPObYX%2b66zhKU%2bVQX7itX7JmQ7oaduA%3d -- ybdobu30ovpcfgareuaqo5fufllb90.oastify.com", "hUsB9spQu7%2bIeEp44bZ8VvJDeuavwNODjLLk4JF1ozw%3d -- 4x2drs9ozvbtnut4lxpqqnaujlpbd0.oastify.com", "J%2bIXs7BO0vyeDeueXDC8X2V8X3j5pR5Kr9a04WxoC9g%3d -- f8ous137m2bofzppk19z8z0tvk1apz.oastify.com", "dKZUaO1sNLzURSVROB6bgWB9h9CT5w3x049j4sLkEjo%3d -- pzs21x6wgorgne0o3b97ofwsejk98y.oastify.com", "Zu7qe1u1tnHqLijlas4HgUaAQMQI13yUwx0NTV9wXKs%3d -- usqfclvxz74ozktghg5zzx3dm4sugj.oastify.com", "QJNPHr19xvnDj6FvqybWl8C0sRTjbV%2bGp0O71BP%2bwKg%3d -- qztgzr1wgwfezuhy49my0dnydpjf74.oastify.com", "QPazBq4DWhO%2bR1dHWBH6pCa9BuhYgvZ8wNbo5%2fHNAHg%3d -- a6hjjbdjl7zyfkojrf9w5hyp8ge62v.oastify.com", "%2bHBKYr5rOACL1z5ln4cTFVGLGRhTfQDG0Zb8yy%2bYlHo%3d -- fratyn534jlquw3s499c74s0grmha6.oastify.com", "8wPKII2dqy06TW%2bLA7xTlyTi4KyiMfMlvOxNa6OwDGI%3d -- aaorl7ewwe2zpyodk963h6utgkmaaz.oastify.com", "5D0n%2fnwXMDvS4glVY9NymgTlTtWPLqCgSWDSiQbwfWE%3d -- 5kqbxvtze1n3am91ptzzgow0oruhi6.oastify.com", "U01eT%2f%2fgkXlDvZYln7hB%2bf9gpMVoaFG5YUE6DtGOG0c%3d -- eq86ycn7g1u3zn4r6k2lurieh5nvbk.oastify.com", "%2byBygZtvQmHSzT31Rn8ru4eUQd9wVNrxgVnoIwxgt%2fw%3d -- 4v0s6geid9khzqyquiix8gbh086yun.oastify.com", "k2u2898KUfNsrrb82cJri7Om%2fK%2farlJeWb%2ft20vA4ks%3d -- oiaz6ghag8kp30l5iq02kxuan1trhg.oastify.com", "s4J7%2bBnbxq6syfPpK5Icfv%2bvUdMRzS0Z87z2cRORF3M%3d -- 3osoyvw81u30wxpelyiug4665xbnzc.oastify.com", "fxxDquTy6WE1QYv5QwSktRROoGs8DsiEV6wkbr9EsnY%3d -- 378so38pk73xet4lix7muah7nytohd.oastify.com", "zIxmi1kpwmrPNEnWz6fstgeHFxyclgKnF1U4eh7yNUg%3d -- fcvqqy0kmqxzmrti707myixpsgy6mv.oastify.com", "%2bfGiq87gbcK1847YvlraGNIoVurhy7q5NY4AZH6HCLM%3d -- b4gtxztv3o0nlwhjuflfiug64xanyc.oastify.com", "nJoGJRUMIWhlWxVP85ASR%2fHo%2bKF2jEed5KGINO7EhQo%3d -- 3vz6s6ziq0fg8vrfpt3vftcpwg26qv.oastify.com", "1G3kFphnV5vVTWhHuGz2FFlDWBKTSzEskc0AFW6adCM%3d -- n4see12n1vf2276lo0pon2z2mtsjg8.oastify.com", "Tv0DvC2oe4qIm5YHKUtL78YZua7vTXrakb300y0N0dc%3d -- 8eny6s0ehvqknoxt56b70u1rdij87x.oastify.com", "YzcISQjwgNNvHboVIcL%2fQKuzXo7u36zKDZkCL%2f3zLeU%3d -- owouaa2oikcqqbmsn1nerl479yfo3d.oastify.com", "OhRIX5PLmzhBGkXgcNjuP5M4JCXoY558caT5gsm%2fyHA%3d -- aiwb9giw0e0ljsr5h085dftmmds3gs.oastify.com", "OwdLjqYbhP3a%2bilpyQL%2fGhiCAEOykhe7DP8BAWgs%2fpA%3d -- bp4g0j0ezkwofg3bao6hyx4mldr3fs.oastify.com", "wAIPsL7cKvUg3A2pbQgov6AKv82sQUUvL0mUqjYrnB4%3d -- gau35zzk9gdnx371gkmgiq5hd8jy7n.oastify.com", "OIDUwM5eI49oqimXcD9WiuwOGttuw8QHxTEWhPTMC8E%3d -- y43cmuv8vic85bryvy3zeqmg076xum.oastify.com", "SDUsxFWEB7BVBR6xucnLA0ofhNaA8040tqXtc3MjhHM%3d -- q5wxd42y00xxj45fbed4zpuvlmrcf1.oastify.com", "tb5JIjO4UHDiLzG6vxHCnO6Bwn%2ftY78htrEwdTvzES4%3d -- 5kq712sfjowfmau1kzg3nmu5jwpmdb.oastify.com", "F9RLAt9PGin0AslQDLZS6wKErlsPaeXH1xbwcMK25rU%3d -- 378f71ilju5j2pa7dylrcqtfw62wql.oastify.com", "nw3I9C9ZW%2bbmdym9uIPAQ8MvSHKadHvUyjhpVd5eUxA%3d -- lf467ytck0qi6vtf2dz58p40trzhn6.oastify.com", "TIXjqBDYPNOb42uyDEfMcJTnd8CerLJj1%2bloczH%2bGv8%3d -- 2ord9oj94zp6lz7wc8clvpk4jvplda.oastify.com", "lFgoOPzGA1cUcWDAlfEn6MStjj8e3nTKzvSGR%2bEa18s%3d -- hxissl6hyx4jlv4vxgmn20jdu40uoj.oastify.com", "7nQpiz1zVEBIq1SjkP1jrVUU%2fkzmWmGgdl2m0HvdaiM%3d -- 0rs2ovt2cop6ybx0is4qz29vnmtch1.oastify.com", "N9tGl1J4JG5rV7S14FLa0x5gl%2b9mRWkbG%2fLTnV2ON6w%3d -- caq3y2hgyy5gegc4px7tetoosfy5mu.oastify.com", "pjf%2fTIscr7vICbEwYyxLvHYA3LbC3YAcw1Z2NkSrl3g%3d -- 86cdnyxeaed67x8j6fxn1ofds4yumj.oastify.com", "WWo%2fXjXzuKPrzCd%2fSngAFUR7lCIBVlsYeqoyij6Ok%2fw%3d -- 0hi5csfhn2li4f3y7hb3lke12s8iw7.oastify.com", "x3DHuamoDxMop4LHsvXlkq1M2r2kfo7WvAYAYTPubU8%3d -- tqnd65zsg0ecpjjawcc9mofej5pvdk.oastify.com", "aMqwMh12kE%2bXMiTF8Pe%2bcrSoeUVPFLzoL1aWCO6QI3s%3d -- ub9hlvm5un63polzm05pnwweo5uvik.oastify.com", "D%2b1YyvkkChy5GCFX%2fiXsJKCB9E99i%2f7FlvJZ27KmJm8%3d -- s4x4rjkhfg9nfca93n36hbxkcbi16q.oastify.com", "YLb1Sr9xb%2fOMRh6K9D%2fwW%2b0CdCdXKIDvqiQywJDeV6s%3d -- 5lrd2ye6ecz1x4pcxema436ee5kv8k.oastify.com", "hyn%2bfuXiPg3knTLUky%2bhkx1DCWh%2bGmEhSI0ANWfjfZA%3d -- d7lgwtqrb1nuzs8xjxqynu09a0gq4f.oastify.com", "op2T%2fiCV3GNCepi%2fQWT5eeCY1u1KVc6fXelNiQxEfUM%3d -- 020k3pifltekneo7d74oof30ur0ho6.oastify.com"};
                            String random_biid = list_random_biids[r.nextInt(list_random_biids.length)];
                            BurpExtender.biidText.setText(random_biid.split(" -- ")[0]);
                            BurpExtender.collabIdText.setText(random_biid.split(" -- ")[1].replace(".oastify.com", ""));
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
							saveLogs();

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

				config_biid = (String) map.get("biid");
				config_cname = (String) map.get("cname");
				config_collab_id = (String) map.get("collab_id");
				
				
				
				/*
				 * StyledDocument doc = l_domain_id.getStyledDocument();
				 * 
				 * Style style = l_domain_id.addStyle("Red", null);
				 * StyleConstants.setForeground(style, Color.red);
				 */
			    
				btn_domain_id.setText(config_collab_id + ".oastify.com");
				
				l_cname.setText("CNAME: " + config_cname);
				reader.close();

				if (config_biid != "" && config_collab_id != "") {
					biidText.setText(config_biid);
					collabIdText.setText(config_collab_id);
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
					stdout.println("_biid_:" + config_biid);
					if (config_biid != "") {

						JsonObject body = Maintest.getNewRecord(config_biid);
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

	@Override
	public void extensionUnloaded() {
		shutdown = true;
		running = false;
		stdout.println(extensionName + " unloaded");
		pollThread.interrupt();
		saveSettings();
	}
}
