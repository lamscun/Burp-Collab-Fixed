package autopayload;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

class AutoCompleterTab extends JPanel {

    private enum MODE {
        DELETE,
        ADD
    }

    private DefaultListModel<String> listerModel;
    private JButton addNewKeyword;
    private JTextField newKeywordField;
    private MODE currentMode = MODE.ADD;
    private String currentlyEdittingCompletion;
    public JTextField textFileName;
    AutoCompleterTab() {
        this.initTab();
    }
    
    void addKeywordToList(String keyword) {
        listerModel.addElement(keyword);
    }
//    public String getFileName() {
//    	return textFileName.getText();
//    }
    private void initTab(){
        GridBagLayout gbl_mainPane = new GridBagLayout();
        gbl_mainPane.columnWeights = new double[]{1.0, 0.0};
        
        JPanel mainPane = new JPanel(gbl_mainPane);
        
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 5, 0);
        GridBagConstraints c1 = new GridBagConstraints();
        c1.insets = new Insets(0, 0, 0, 5);
        c1.fill = GridBagConstraints.HORIZONTAL;
        GridBagConstraints c2 = new GridBagConstraints();
        c2.anchor = GridBagConstraints.SOUTH;
        listerModel = new DefaultListModel<>();
        JList<String> lister = new JList<>(listerModel);
        lister.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JList list = (JList)e.getSource();
                if (e.getClickCount() == 1) {
                    currentMode = MODE.DELETE;
                    addNewKeyword.setText("Delete");
                    int index = list.locationToIndex(e.getPoint());
                    currentlyEdittingCompletion = listerModel.elementAt(index);
                    newKeywordField.setText(currentlyEdittingCompletion);

                }
            }
        });
        JScrollPane scroller = new JScrollPane(lister);
        setLayout(new BorderLayout());
        c.weighty = 0.9;
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy = 0;
        mainPane.add(scroller,c);
        
        JLabel lblNewLabel = new JLabel("Ctrl + Shift + N --> Show/Hide jFrame Suggestion");
        lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 10));
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
        gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel.gridx = 0;
        gbc_lblNewLabel.gridy = 1;
        mainPane.add(lblNewLabel, gbc_lblNewLabel);
        
        JLabel lblCtrlShift = new JLabel("Ctrl + Shift + M --> Show/Hide jFrame Suggestion with all payload");
        lblCtrlShift.setFont(new Font("Tahoma", Font.PLAIN, 10));
        GridBagConstraints gbc_lblCtrlShift = new GridBagConstraints();
        gbc_lblCtrlShift.anchor = GridBagConstraints.WEST;
        gbc_lblCtrlShift.insets = new Insets(0, 0, 5, 5);
        gbc_lblCtrlShift.gridx = 0;
        gbc_lblCtrlShift.gridy = 2;
        mainPane.add(lblCtrlShift, gbc_lblCtrlShift);
        
        textFileName = new JTextField();
        // create new file if not exist file 
               
    	
//    	System.out.println("filePath:"+  ExtensionState.getInstance().filePath);        
//        String userDirectory = new File("").getAbsolutePath();
        textFileName.setText("/payloads.txt");
        //    	textFileName.setText(ExtensionState.getInstance().filePath);
                GridBagConstraints gbc_textFileName = new GridBagConstraints();
                gbc_textFileName.insets = new Insets(0, 0, 5, 5);
                gbc_textFileName.fill = GridBagConstraints.HORIZONTAL;
                gbc_textFileName.gridx = 0;
                gbc_textFileName.gridy = 3;
                mainPane.add(textFileName, gbc_textFileName);
                textFileName.setColumns(10);
        
        JButton btnLoadFile = new JButton("Load/Reload File");
        btnLoadFile.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		listerModel.clear();
        		ExtensionState.getInstance().getKeywords().clear();
				ExtensionState ext_state = new ExtensionState();
//				ArrayList<String> keywords = new ArrayList<>();
//				ext_state.keywords = ext_state.setKeyWordsFromFile(ExtensionState.getInstance().filePath);
				 System.out.println("File name: ");
				 System.out.println(getFileName());
				ext_state.keywords = ext_state.setKeyWordsFromFile(getFileName());
		        for(String keyword : ext_state.keywords){
//		        	listerModel.addElement(keyword);
//		        	ext_state.getAutoCompleterTab().addKeywordToList(keyword);
		        	ExtensionState.getInstance().getKeywords().add(keyword.trim());
	                listerModel.addElement(keyword.trim());
		        }
		        ExtensionState.getInstance().setUserOptions_payloadPath(getFileName());
        	}
        });
        GridBagConstraints gbc_btnLoadFile = new GridBagConstraints();
        gbc_btnLoadFile.insets = new Insets(0, 0, 5, 0);
        gbc_btnLoadFile.gridx = 1;
        gbc_btnLoadFile.gridy = 3;
        mainPane.add(btnLoadFile, gbc_btnLoadFile);
        c1.weighty = 0.1;
        c1.anchor = GridBagConstraints.SOUTH;
        c1.gridwidth = 1;
        c1.gridx = 0;
        c1.gridy = 4;
        c1.weightx = 0.9;
        newKeywordField = new JTextField(50);
        mainPane.add(newKeywordField,c1);
        c2.gridx = 1;
        c2.gridwidth = 1;
        c2.weightx = 0.1;
        c2.gridy = 4;
        addNewKeyword = new JButton("Add");
        addNewKeyword.addActionListener(e -> {
            if (currentMode == MODE.ADD) {
                ExtensionState.getInstance().getKeywords().add(newKeywordField.getText().trim());
                listerModel.addElement(newKeywordField.getText().trim());
            } else if (currentMode == MODE.DELETE) {
                ExtensionState.getInstance().getKeywords().remove(newKeywordField.getText().trim());
                listerModel.removeElement(newKeywordField.getText().trim());
            }
            currentMode = MODE.ADD;
            addNewKeyword.setText("Add");
            newKeywordField.setText("");
            currentlyEdittingCompletion = "";

        });
        mainPane.add(addNewKeyword,c2);
        add(mainPane,BorderLayout.CENTER);

    }

	public String getFileName() {
		// TODO Auto-generated method stub
		return textFileName.getText();
	}
}
