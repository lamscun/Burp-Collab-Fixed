package autopayload;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import payint.PayIntConnector;

import java.awt.*;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;

public class AutoCompleterTab extends JPanel {

    private enum MODE {
        DELETE,
        ADD
    }

    private DefaultListModel<String> listerModel;
    private JButton addNewKeyword;
    private JButton deleteKeyword;
    public JTextField newKeywordField;
    public JTextField payint_pathField;
    public JLabel payint_payloads_path_Label;
    private MODE currentMode = MODE.ADD;
    public JTextField textFileName;
    
    public JTextField text_XSS_HUNTER;
    public JTextField text_SLEEP_TIME;
    public JTextField text_PROJECT_NAME;
    public JTextField text_BURP_COLLAB_DOMAIN;
    
    public JTextField text_Filter;
    DefaultTableModel model;
    AutoCompleterTab() {
        this.initTab();
    }
    
    void addKeywordToModelTable(DefaultTableModel model, int rowID, String keyword, String cmt) {
        // listerModel.addElement(keyword);
        if ( keyword.trim().contains(" ||| ")) {
        	model.addRow(new Object[] { rowID, keyword.trim().split("\\|\\|\\|")[0], keyword.trim().split("\\|\\|\\|")[1], cmt});
            rowID++;
        } else {
        	model.addRow(new Object[] { rowID, "no name", keyword.trim(), cmt});
            rowID++;
        }
    }

    
    private void initTab(){
        GridBagLayout gbl_mainPane = new GridBagLayout();
        gbl_mainPane.columnWeights = new double[]{1, 0};
        JPanel mainPane = new JPanel(gbl_mainPane);
        
       
        listerModel = new DefaultListModel<>();

        setLayout(new BorderLayout());         
        GridBagConstraints gbcScroller = new GridBagConstraints();
        gbcScroller.insets = new Insets(5, 5, 5, 5);
        gbcScroller.weighty = 1;
        gbcScroller.anchor = GridBagConstraints.NORTH;
        gbcScroller.fill = GridBagConstraints.BOTH;
        gbcScroller.gridx = 0;
        gbcScroller.gridy = 0;
        gbcScroller.gridwidth = 2;

        final Class[] classes = new Class[] { Integer.class, String.class, String.class, String.class};
        
        
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
		
		
        JTable collaboratorTable = new JTable(model);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
		collaboratorTable.setRowSorter(sorter);
		model.addColumn("#");
		model.addColumn("Tab Name");
		model.addColumn("Payload");
		model.addColumn("Cmt");
		collaboratorTable.getColumnModel().getColumn(0).setPreferredWidth(30);
		collaboratorTable.getColumnModel().getColumn(0).setMaxWidth(150);
		collaboratorTable.getColumnModel().getColumn(1).setPreferredWidth(150);
		collaboratorTable.getColumnModel().getColumn(1).setMaxWidth(1000);
		collaboratorTable.getColumnModel().getColumn(3).setPreferredWidth(100);
		collaboratorTable.getColumnModel().getColumn(3).setMaxWidth(500);
		JScrollPane collaboratorScroll = new JScrollPane(collaboratorTable);
		
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem deleteSelectedMenu = new JMenuItem("Delete Selected");
		
		JMenuItem deleteSelectedMenuInFile = new JMenuItem("Delete Selected In File");
		
		popupMenu.add(deleteSelectedMenu);
		popupMenu.add(deleteSelectedMenuInFile);
		collaboratorTable.setComponentPopupMenu(popupMenu);
		
		mainPane.add(collaboratorScroll,gbcScroller);
        ///
        
        /// mainPane.add(scroller,gbcScroller);
        
        JLabel lblNewLabel = new JLabel("Ctrl + Shift + N --> Show/Hide Suggestion");
        // lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 10));
        lblNewLabel.setForeground(new Color(255, 191, 128));
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
        gbc_lblNewLabel.insets = new Insets(5, 5, 5, 5);
        gbc_lblNewLabel.gridx = 0;
        gbc_lblNewLabel.gridy = 1;
        mainPane.add(lblNewLabel, gbc_lblNewLabel);
        
        JLabel lblCtrlShift = new JLabel("Ctrl + Shift + M --> Show/Hide Suggestion with all payload");
        lblCtrlShift.setForeground(new Color(255, 191, 128));
        // lblCtrlShift.setFont(new Font("Tahoma", Font.PLAIN, 10));
        GridBagConstraints gbc_lblCtrlShift = new GridBagConstraints();
        gbc_lblCtrlShift.anchor = GridBagConstraints.WEST;
        gbc_lblCtrlShift.insets = new Insets(5, (int) lblNewLabel.getPreferredSize().getWidth()+20, 5, 5);
        gbc_lblCtrlShift.gridx = 0;
        gbc_lblCtrlShift.gridy = 1;
        mainPane.add(lblCtrlShift, gbc_lblCtrlShift);
        
        JLabel lb_html = new JLabel("Ctrl + Shift + F --> HTML/XML beautifier");
        lb_html.setForeground(new Color(255, 191, 128));
        // lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 10));
        GridBagConstraints gbc_html = new GridBagConstraints();
        gbc_html.anchor = GridBagConstraints.WEST;
        gbc_html.insets = new Insets(5, 5, 5, 5);
        gbc_html.gridx = 0;
        gbc_html.gridy = 2;
        mainPane.add(lb_html, gbc_html);
        
        JLabel lb_Json = new JLabel("Ctrl + Shift + J --> JSON beautifier");
        lb_Json.setForeground(new Color(255, 191, 128));
        // lblCtrlShift.setFont(new Font("Tahoma", Font.PLAIN, 10));
        GridBagConstraints gbc_Json = new GridBagConstraints();
        gbc_Json.anchor = GridBagConstraints.WEST;
        gbc_Json.insets = new Insets(5, (int) lb_html.getPreferredSize().getWidth()+30, 5, 5);
        gbc_Json.gridx = 0;
        gbc_Json.gridy = 2;
        mainPane.add(lb_Json, gbc_Json);
        
        
        JLabel lb_CustomPayload = new JLabel("Custom payloads file: ");
        lb_Json.setForeground(new Color(255, 191, 128));
        // lblCtrlShift.setFont(new Font("Tahoma", Font.PLAIN, 10));
        GridBagConstraints gbc_lb_CustomPayloadn = new GridBagConstraints();
        gbc_lb_CustomPayloadn.anchor = GridBagConstraints.WEST;
        gbc_lb_CustomPayloadn.insets = new Insets(5, 5, 5, 5);
        gbc_lb_CustomPayloadn.gridx = 0;
        gbc_lb_CustomPayloadn.gridy = 4;
        mainPane.add(lb_CustomPayload, gbc_lb_CustomPayloadn);
        
        textFileName = new JTextField();
        textFileName.setText("/payloads.txt");
        GridBagConstraints gbcFileName = new GridBagConstraints();
        gbcFileName.insets = new Insets(5, (int) lb_CustomPayload.getPreferredSize().getWidth()+10, 5, 5);
        gbcFileName.fill = GridBagConstraints.HORIZONTAL;
        gbcFileName.gridx = 0;
        gbcFileName.gridy = 4;
        mainPane.add(textFileName, gbcFileName);
        
     
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(760, 400);      
        frame.setLocationRelativeTo(null);  
        frame.setVisible(false);
           
        JButton btnOpenFile = new JButton("Open File");
        GridBagConstraints gbc_btnLoadFile1 = new GridBagConstraints();
        gbc_btnLoadFile1.insets = new Insets(5, 5, 5, 5);
        gbc_btnLoadFile1.gridx = 1;
        gbc_btnLoadFile1.gridy = 4;
        mainPane.add(btnOpenFile, gbc_btnLoadFile1);

        
        JButton btnLoadFile = new JButton("Load File");
        GridBagConstraints gbc_btnLoadFile = new GridBagConstraints();
        gbc_btnLoadFile.insets = new Insets(5, 5, 5, 5);
        gbc_btnLoadFile.gridx = 2;
        gbc_btnLoadFile.gridy = 4;
        mainPane.add(btnLoadFile, gbc_btnLoadFile);
        
        
        JLabel lbgenPayint = new JLabel("PayInt payloads file: ");
        // lbAddNePayLoad.setFont(new Font("Tahoma", Font.PLAIN, 10));
        GridBagConstraints gbc_genPayint = new GridBagConstraints();
        gbc_genPayint.anchor = GridBagConstraints.WEST;
        gbc_genPayint.insets = new Insets(5, 5, 5, 5);
        gbc_genPayint.gridx = 0;
        gbc_genPayint.gridy = 5;
        mainPane.add(lbgenPayint, gbc_genPayint);
        
        payint_payloads_path_Label = new JLabel("null");
        GridBagConstraints gbc_payint_payloads_pathField = new GridBagConstraints();
        gbc_payint_payloads_pathField.insets = new Insets(5, (int) lbgenPayint.getPreferredSize().getWidth() + 10, 5, 5);
        gbc_payint_payloads_pathField.fill = GridBagConstraints.BOTH;
        gbc_payint_payloads_pathField.gridx = 0;
        gbc_payint_payloads_pathField.gridy = 5;
        mainPane.add(payint_payloads_path_Label, gbc_payint_payloads_pathField);
        
        payint_pathField = new JTextField();
        GridBagConstraints gbc_payint_pathField = new GridBagConstraints();
        gbc_payint_pathField.insets = new Insets(5, 5, 5, 5);
        gbc_payint_pathField.fill = GridBagConstraints.HORIZONTAL;
        gbc_payint_pathField.gridx = 0;
        gbc_payint_pathField.gridy = 6;
        mainPane.add(payint_pathField, gbc_payint_pathField);
        
        
        JButton btnOpenPayInt = new JButton("Open PayInt");
        GridBagConstraints gbc_OpenPayIntPayloads = new GridBagConstraints();
        gbc_OpenPayIntPayloads.insets = new Insets(5, 5, 5, 5);
        gbc_OpenPayIntPayloads.gridx = 1;
        gbc_OpenPayIntPayloads.gridy = 6;
        mainPane.add(btnOpenPayInt, gbc_OpenPayIntPayloads);
        
        JButton btnLoadPayInt = new JButton("Load PayInt");
        GridBagConstraints gbc_genPayIntPayloads = new GridBagConstraints();
        gbc_genPayIntPayloads.insets = new Insets(5, 5, 5, 5);
        gbc_genPayIntPayloads.gridx = 2;
        gbc_genPayIntPayloads.gridy = 6;
        mainPane.add(btnLoadPayInt, gbc_genPayIntPayloads);
        
        
        
        
        JLabel lbAddNePayLoad = new JLabel("Add Payload: <tabname> ||| <payload>");
        // lbAddNePayLoad.setFont(new Font("Tahoma", Font.PLAIN, 10));
        GridBagConstraints gbcLableAddNewPayload = new GridBagConstraints();
        gbcLableAddNewPayload.anchor = GridBagConstraints.WEST;
        gbcLableAddNewPayload.insets = new Insets(5, 5, 5, 5);
        gbcLableAddNewPayload.gridx = 0;
        gbcLableAddNewPayload.gridy = 7;
        mainPane.add(lbAddNePayLoad, gbcLableAddNewPayload);
        
        
        
        newKeywordField = new JTextField();
        GridBagConstraints gbcNewKey = new GridBagConstraints();
        gbcNewKey.insets = new Insets(5, (int) lbAddNePayLoad.getPreferredSize().getWidth() + 10, 5, 5);
        gbcNewKey.fill = GridBagConstraints.HORIZONTAL;
        gbcNewKey.gridx = 0;
        gbcNewKey.gridy = 7;
        mainPane.add(newKeywordField,gbcNewKey);

       
        addNewKeyword = new JButton("Add payload");
        GridBagConstraints gbc_btnAdd = new GridBagConstraints();
        gbc_btnAdd.insets = new Insets(5, 5, 5, 5);
        gbc_btnAdd.gridx = 1;
        gbc_btnAdd.gridy = 7;
        mainPane.add(addNewKeyword,gbc_btnAdd);
        
		/*
		 * deleteKeyword = new JButton("Del payload"); GridBagConstraints gbc_btnDel =
		 * new GridBagConstraints(); gbc_btnDel.insets = new Insets(5, 5, 5, 5);
		 * gbc_btnDel.gridx = 2; gbc_btnDel.gridy = 7;
		 * mainPane.add(deleteKeyword,gbc_btnDel);
		 */
        
        JLabel lb_config_info = new JLabel("Setting config for generate PayInt payloads: ");
        lb_config_info.setForeground(new Color(0, 191, 255));
        // lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 10));
        GridBagConstraints gbc_lb_config_info = new GridBagConstraints();
        gbc_lb_config_info.anchor = GridBagConstraints.WEST;
        gbc_lb_config_info.insets = new Insets(5, 5, 5, 5);
        gbc_lb_config_info.gridx = 0;
        gbc_lb_config_info.gridy = 8;
        mainPane.add(lb_config_info, gbc_lb_config_info);
        
        JLabel lbXSS_Hunter = new JLabel("Config - XSS_Hunter: ");
        // lbAddNePayLoad.setFont(new Font("Tahoma", Font.PLAIN, 10));
        GridBagConstraints gbclbXSS_Hunter = new GridBagConstraints();
        gbclbXSS_Hunter.anchor = GridBagConstraints.WEST;
        gbclbXSS_Hunter.insets = new Insets(5, 5, 5, 5);
        gbclbXSS_Hunter.gridx = 0;
        gbclbXSS_Hunter.gridy = 9;
        mainPane.add(lbXSS_Hunter, gbclbXSS_Hunter);
        
        text_XSS_HUNTER = new JTextField();
        text_XSS_HUNTER.setText("lamscun.xss.ht");
        GridBagConstraints gbcXSS_Hunter = new GridBagConstraints();
        gbcXSS_Hunter.insets = new Insets(5, (int) lbXSS_Hunter.getPreferredSize().getWidth() + 10, 5, 5);
        gbcXSS_Hunter.fill = GridBagConstraints.HORIZONTAL;
        gbcXSS_Hunter.gridx = 0;
        gbcXSS_Hunter.gridy = 9;
        mainPane.add(text_XSS_HUNTER, gbcXSS_Hunter);
        
        
        JLabel lbBURP_COLLAB_DOMAIN = new JLabel("Config - BURP_COLLAB_DOMAIN: ");
        // lbAddNePayLoad.setFont(new Font("Tahoma", Font.PLAIN, 10));
        GridBagConstraints gbclbBURP_COLLAB_DOMAIN = new GridBagConstraints();
        gbclbBURP_COLLAB_DOMAIN.anchor = GridBagConstraints.WEST;
        gbclbBURP_COLLAB_DOMAIN.insets = new Insets(5, 5, 5, 5);
        gbclbBURP_COLLAB_DOMAIN.gridx = 0;
        gbclbBURP_COLLAB_DOMAIN.gridy = 10;
        mainPane.add(lbBURP_COLLAB_DOMAIN, gbclbBURP_COLLAB_DOMAIN);
        
        text_BURP_COLLAB_DOMAIN = new JTextField();
        text_BURP_COLLAB_DOMAIN.setText("wwwz15e554m201wwajfl7m1ey54z1nq.oastify.com");
        GridBagConstraints gbcBURP_COLLAB_DOMAIN = new GridBagConstraints();
        gbcBURP_COLLAB_DOMAIN.insets = new Insets(5,  (int) lbBURP_COLLAB_DOMAIN.getPreferredSize().getWidth() + 10, 5, 5);
        gbcBURP_COLLAB_DOMAIN.fill = GridBagConstraints.HORIZONTAL;
        gbcBURP_COLLAB_DOMAIN.gridx = 0;
        gbcBURP_COLLAB_DOMAIN.gridy = 10;
        mainPane.add(text_BURP_COLLAB_DOMAIN, gbcBURP_COLLAB_DOMAIN);
        
        
        JLabel lbPROJECT_NAME = new JLabel("Config - PROJECT_NAME: ");
        // lbAddNePayLoad.setFont(new Font("Tahoma", Font.PLAIN, 10));
        GridBagConstraints gbclbPROJECT_NAME = new GridBagConstraints();
        gbclbPROJECT_NAME.anchor = GridBagConstraints.WEST;
        gbclbPROJECT_NAME.insets = new Insets(5, 5, 5, 5);
        gbclbPROJECT_NAME.gridx = 0;
        gbclbPROJECT_NAME.gridy = 11;
        mainPane.add(lbPROJECT_NAME, gbclbPROJECT_NAME);
        
        text_PROJECT_NAME = new JTextField();
        text_PROJECT_NAME.setText("collabfix");
        GridBagConstraints gbcPROJECT_NAME = new GridBagConstraints();
        gbcPROJECT_NAME.insets = new Insets(5,  (int) lbPROJECT_NAME.getPreferredSize().getWidth() + 10, 5, 5);
        gbcPROJECT_NAME.fill = GridBagConstraints.HORIZONTAL;
        gbcPROJECT_NAME.gridx = 0;
        gbcPROJECT_NAME.gridy = 11;
        mainPane.add(text_PROJECT_NAME, gbcPROJECT_NAME);
        
        
        JLabel lbSLEEP_TIME = new JLabel("Config - SLEEP_TIME: ");
        // lbAddNePayLoad.setFont(new Font("Tahoma", Font.PLAIN, 10));
        GridBagConstraints gbclbSLEEP_TIME = new GridBagConstraints();
        gbclbSLEEP_TIME.anchor = GridBagConstraints.WEST;
        gbclbSLEEP_TIME.insets = new Insets(5, 5, 5, 5);
        gbclbSLEEP_TIME.gridx = 0;
        gbclbSLEEP_TIME.gridy = 12;
        mainPane.add(lbSLEEP_TIME, gbclbSLEEP_TIME);
        
        text_SLEEP_TIME = new JTextField();
        text_SLEEP_TIME.setText("15");
        GridBagConstraints gbcSLEEP_TIME = new GridBagConstraints();
        gbcSLEEP_TIME.insets = new Insets(5,  (int) lbSLEEP_TIME.getPreferredSize().getWidth() + 10, 5, 5);
        gbcSLEEP_TIME.fill = GridBagConstraints.HORIZONTAL;
        gbcSLEEP_TIME.gridx = 0;
        gbcSLEEP_TIME.gridy = 12;
        mainPane.add(text_SLEEP_TIME, gbcSLEEP_TIME);
        
        
        JButton btnLoadPayIntConfig = new JButton("Load PayInt Config");
        GridBagConstraints gbcbtnLoadPayIntConfig = new GridBagConstraints();
        gbcbtnLoadPayIntConfig.insets = new Insets(5, 5, 5, 5);
        gbcbtnLoadPayIntConfig.gridx = 1;
        gbcbtnLoadPayIntConfig.gridy = 12;
        mainPane.add(btnLoadPayIntConfig, gbcbtnLoadPayIntConfig);
        
        add(mainPane,BorderLayout.CENTER);
        
        
        text_Filter = new JTextField();
        GridBagConstraints gbc_Text_Filter = new GridBagConstraints();
        gbc_Text_Filter.insets = new Insets(5, 5, 5, 5);
        gbc_Text_Filter.fill = GridBagConstraints.HORIZONTAL;
        gbc_Text_Filter.gridx = 0;
        gbc_Text_Filter.gridy = 13;
        mainPane.add(text_Filter, gbc_Text_Filter);
        
        text_Filter.addKeyListener(new KeyAdapter() {
	        @Override
	        public void keyPressed(KeyEvent e) {
	            if(e.getKeyCode() == KeyEvent.VK_ENTER){
	            	String text = text_Filter.getText();
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
        
        JButton btn_Filter = new JButton("Filter");
        GridBagConstraints gbcFilter = new GridBagConstraints();
        gbcFilter.insets = new Insets(5, 5, 5, 5);
        gbcFilter.fill = GridBagConstraints.HORIZONTAL;
        gbcFilter.gridx = 1;
        gbcFilter.gridy = 13;
        mainPane.add(btn_Filter, gbcFilter);
        
        
        JButton btn_copyAll = new JButton("Copy Selected"); 
        GridBagConstraints gbc_copyAll = new GridBagConstraints();
        gbc_copyAll.insets = new Insets(5, 5, 5, 5);
        gbc_copyAll.fill = GridBagConstraints.HORIZONTAL;
        gbc_copyAll.gridx = 0;
        gbc_copyAll.gridy = 14;
        mainPane.add(btn_copyAll, gbc_copyAll);
        
        
        btn_copyAll.addActionListener(event->{     	
        	int[] row_ids = collaboratorTable.getSelectedRows();    	
        	String all_current_payloads = "";	
        	for (int rowId : row_ids) {
				int modelRow = collaboratorTable.convertRowIndexToModel(rowId);
				String str_payload = (String) collaboratorTable.getModel().getValueAt(modelRow, 2);
				all_current_payloads = all_current_payloads + str_payload + "\n";
			}

        	Toolkit.getDefaultToolkit().getSystemClipboard()
			.setContents(new StringSelection(all_current_payloads), (ClipboardOwner) null);
        });
        
        
        btn_Filter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               String text = text_Filter.getText();
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
        
        
        
        deleteSelectedMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int answer = JOptionPane.showConfirmDialog(null, "This will delete this items, are you sure?");
				TableModel model = (DefaultTableModel) collaboratorTable.getModel();
				if (answer == 0) {
					int[] row_ids = collaboratorTable.getSelectedRows();

					for (int rowId : row_ids) {
						int modelRow = collaboratorTable.convertRowIndexToModel(rowId);

						// Delete in history
						// interactionHistory.remove(id);
						
						// Delete in keywords search
						String tag_name = (String) collaboratorTable.getModel().getValueAt(modelRow, 1);
						String payload = (String) collaboratorTable.getModel().getValueAt(modelRow, 2);
						// ExtensionState.getInstance().getCallbacks().printOutput(tag_name.trim() + " ||| " + payload.trim());
						ExtensionState.getInstance().getKeywords().remove(tag_name.trim() + " ||| " + payload.trim());
						
						// Delete in table UI
						((DefaultTableModel) model).removeRow(rowId);

						// After delete a items, need to decrease index from 1
						for (int i = 0; i < row_ids.length; i++) {
							row_ids[i]--;
						}

					}

				}
				// saveLogs();
				collaboratorTable.clearSelection();
			}
		});
        
        deleteSelectedMenuInFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int answer = JOptionPane.showConfirmDialog(null, "This will delete this items in file, are you sure?");
				TableModel model = (DefaultTableModel) collaboratorTable.getModel();
				if (answer == 0) {
					int[] row_ids = collaboratorTable.getSelectedRows();

					for (int rowId : row_ids) {
						int modelRow = collaboratorTable.convertRowIndexToModel(rowId);

						// Delete in history
						// interactionHistory.remove(id);
						
						// Delete in keywords search
						String tag_name = (String) collaboratorTable.getModel().getValueAt(modelRow, 1);
						String payload = (String) collaboratorTable.getModel().getValueAt(modelRow, 2);
						// ExtensionState.getInstance().getCallbacks().printOutput(tag_name.trim() + " ||| " + payload.trim());
						ExtensionState.getInstance().getKeywords().remove(tag_name.trim() + " ||| " + payload.trim());
						
						// Delete in table UI
						((DefaultTableModel) model).removeRow(rowId);

						// After delete a items, need to decrease index from 1
						for (int i = 0; i < row_ids.length; i++) {
							row_ids[i]--;
						}

					}

				}
				// saveLogs();
				collaboratorTable.clearSelection();
			}
		});
        
        addNewKeyword.addActionListener(e -> {
            if (currentMode == MODE.ADD) {
                ExtensionState.getInstance().getKeywords().add(newKeywordField.getText().trim());
                listerModel.addElement(newKeywordField.getText().trim());
                int rowID =  model.getRowCount()+1;
                addKeywordToModelTable(model, rowID, newKeywordField.getText(), "custom payload");
            } else if (currentMode == MODE.DELETE) {
                ExtensionState.getInstance().getKeywords().remove(newKeywordField.getText().trim());
                listerModel.removeElement(newKeywordField.getText().trim());
            }
            currentMode = MODE.ADD;
            addNewKeyword.setText("Add");
            newKeywordField.setText("");

        });
        
		/*
		 * deleteKeyword.addActionListener(e -> { if (currentMode == MODE.DELETE) {
		 * ExtensionState.getInstance().getKeywords().remove(newKeywordField.getText().
		 * trim()); listerModel.removeElement(newKeywordField.getText().trim()); }
		 * currentMode = MODE.ADD; addNewKeyword.setText("Add");
		 * newKeywordField.setText("");
		 * 
		 * });
		 */
        
        btnLoadPayIntConfig.addMouseListener(new MouseAdapter() {  
        	@Override
        	public void mouseClicked(MouseEvent e) {
		        ExtensionState.getInstance().setUserOptions_payloadPath();
        	}
        });
        
        btnLoadFile.addMouseListener(new MouseAdapter() {  
        	@Override
        	public void mouseClicked(MouseEvent e) {
        	
        		
        		((DefaultTableModel) model).setRowCount(0);
        		// Clear current table
        		collaboratorTable.removeAll();
        		
        		ExtensionState.getInstance().getKeywords().clear();
				ExtensionState ext_state = new ExtensionState();
				// ArrayList<String> keywords = new ArrayList<>();
				// ext_state.keywords = ext_state.setKeyWordsFromFile(ExtensionState.getInstance().filePath);
				
				//System.out.println("File name: ");
				//System.out.println(getFileName());
				// Set new list key words for search
				ext_state.keywords.addAll(ext_state.setKeyWordsFromFile(getFileName()));
				int rowID = 1;
		        for(String keyword : ext_state.keywords){
		        	ExtensionState.getInstance().getKeywords().add(keyword.trim());
	                addKeywordToModelTable(model, rowID, keyword, "custom payload");
	                rowID++;
		        }
		        ExtensionState.getInstance().setUserOptions_payloadPath();
        	}
        });
        
        btnLoadPayInt.addMouseListener(new MouseAdapter() {  
        	@Override
        	public void mouseClicked(MouseEvent e) {
        	
        		
        		((DefaultTableModel) model).setRowCount(0);
        		// Clear current table
        		collaboratorTable.removeAll();
        		
        		ExtensionState.getInstance().getKeywords().clear();
				ExtensionState ext_state = new ExtensionState();
				// ArrayList<String> keywords = new ArrayList<>();
				// ext_state.keywords = ext_state.setKeyWordsFromFile(ExtensionState.getInstance().filePath);
				
				//System.out.println("File name: ");
				//System.out.println(getFileName());
				// Set new list key words for search
				
				PayIntConnector payint = new PayIntConnector();
				try {
					String outputPayloadPayintfileName = payint.genPayIntPayload(payint_pathField.getText().replace("\\", "\\\\"));
					payint_payloads_path_Label.setText(outputPayloadPayintfileName);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				ext_state.keywords.addAll(ext_state.setKeyWordsFromFile(getPayintPayloadsPathName()));
				int rowID = 1;
		        for(String keyword : ext_state.keywords){
		        	ExtensionState.getInstance().getKeywords().add(keyword.trim());
	                addKeywordToModelTable(model, rowID, keyword, "payint payload");
	                rowID++;
		        }
		        ExtensionState.getInstance().setUserOptions_payloadPath();
        	}
        });
        
        btnOpenFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	
               JFileChooser fileChooser = new JFileChooser(getFileName());
               fileChooser.setMultiSelectionEnabled(true);

               int option = fileChooser.showOpenDialog(frame);
               if(option == JFileChooser.APPROVE_OPTION){
                  File file = fileChooser.getSelectedFile();
                  textFileName.setText(file.getAbsolutePath());
               }
            }
         });
           
        btnOpenPayInt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	
               JFileChooser fileChooser = new JFileChooser(getFileName());
               //fileChooser.setMultiSelectionEnabled(true);
               fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
               int option = fileChooser.showOpenDialog(frame);
               if(option == JFileChooser.APPROVE_OPTION){
                  File file = fileChooser.getSelectedFile();
                  payint_pathField.setText(file.getAbsolutePath());
               }
            }
         });
        
        
        
        
		/*
		 * lister.addMouseListener(new MouseAdapter() {
		 * 
		 * @Override public void mouseClicked(MouseEvent e) { JList list =
		 * (JList)e.getSource(); if (e.getClickCount() == 1) { currentMode =
		 * MODE.DELETE; // addNewKeyword.setText("Delete"); int index =
		 * list.locationToIndex(e.getPoint()); currentlyEdittingCompletion =
		 * listerModel.elementAt(index);
		 * newKeywordField.setText(currentlyEdittingCompletion);
		 * 
		 * } } });
		 */
        
    }

	public String getFileName() {
		// TODO Auto-generated method stub
		return textFileName.getText();
	}
	
	public String getPayintPayloadsPathName() {
		// TODO Auto-generated method stub
		return payint_payloads_path_Label.getText();
	}
}
