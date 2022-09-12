package autopayload;


import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ActionEvent;


/**
 * This class handles the autocomplete. it keeps a reference to the JTextArea is autocompletes for and generates a list
 * of possible candidates, updated after every letter typed.
 */
public class AutoCompleter implements DocumentListener, CaretListener{

    //The document we are autocompleting for
    private JTextArea source;
    //Our current offset position in the document
    private int pos;
    //Stateflag to determine if the last action was a backspace
    private boolean backspaceMode;
    //The suggestion frame which holds the current autocomplete candidates
    public JFrame suggestionPane;
    //List model to hold the candidate autocompletions
    public DefaultListModel<String> suggestionsModel = new DefaultListModel<>();
    JPanel pane = new JPanel(new BorderLayout());
    //The content of the source document we will be replacing
    private String content;
    private enum MODE {
        INSERT,
        COMPLETION
    }
    private MODE mode = MODE.INSERT;
    private int iframe_width = 1200;
    private int iframe_height = 250;

    public int chk_pos = 0;
    /**
     * This listener follows the caret and updates where we should draw the suggestions box
     * @param e the carent event
     */
    @Override
    public void caretUpdate(CaretEvent e) {
        pos = e.getDot();
        System.out.println("Caret: "+pos);
        Point p = source.getCaret().getMagicCaretPosition();
        if(p != null) {
            Point np = new Point();
            np.x = p.x + source.getLocationOnScreen().x;
            np.y = p.y + source.getLocationOnScreen().y+25;
            suggestionPane.setLocation(np);
        }
    }
    
    /**
     * Initializes the suggestion pane and attaches our listeners
     * @param s the source to provide autocompletions for
     */
    public AutoCompleter(JTextArea s) {
        this.source = s;
        this.pos = this.source.getCaret().getDot();
        this.source.addCaretListener(this);
        
        suggestionPane = new JFrame();
        
        
        suggestionPane.setSize(iframe_width,iframe_height);
//        suggestionPane.setTitle("List Suggestion");
        suggestionPane.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        suggestionPane.setUndecorated(true);
        suggestionPane.setAutoRequestFocus(false);
//        
        suggestionPane.setLocationRelativeTo(null);
        suggestionPane.setLocation(700,700);
        
        
        
        JList<String> suggestions = new JList<>(suggestionsModel);
        JScrollPane scroller = new JScrollPane(suggestions);
        pane.add(scroller, BorderLayout.CENTER);
        suggestionPane.getContentPane().add(pane);
        //Double clicks will pick the autocompletion to commit to
        suggestions.addMouseListener(new MouseAdapter() {
        	
        	
        	
            @Override
            public void mouseClicked(MouseEvent e) {
                JList list = (JList)e.getSource();
                if (e.getClickCount() == 2) {

                    // Double-click detected
                    int start = getTextReplacementStart();
                    int index = list.locationToIndex(e.getPoint());
                    String selectedCompletion = suggestionsModel.elementAt(index).replaceAll("^(.* \\|\\|\\|) ", "");
                    System.out.println(start+1 + " : " + pos+1);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            source.select(start+1,pos);
//                            source.replaceSelection(selectedCompletion+": ");
                            source.replaceSelection(selectedCompletion+"");
                            source.setCaretPosition(source.getSelectionEnd());
                            suggestionPane.setVisible(false);
                        }
                    });

                }
            }
        });

    }

    
    /**
     * Get's the start of the users text we are replacing
     * @return starting index of the users input
     */
    private int getTextReplacementStart() {
    	
    	if(chk_pos==1) {
    		// chk_pos is full payload show
    		chk_pos = 0;
    		return pos-1;
    	}
    	
        int start;
        if(backspaceMode) {
            for (start = pos-2; start >= 0; start--) {
            	if(start<content.length()  && start>=0) {
            		System.out.println(content.getBytes().toString());
                	System.out.println(content.charAt(start));
                    if (Character.isWhitespace(content.charAt(start))) {
                        break;
                    }
            	} else {
            		System.out.println("Out of range!!!!");
            	}
            	
            }
        } else {
            for (start = pos-1; start >= 0; start--) {
            	if(start<content.length() && start>=0) {

                	System.out.println(content.length());
                    if (Character.isWhitespace(content.charAt(start))) {
                        break;
                    }
            	} else {
            		System.out.println("Out of range!!!!");
            	}
            	
            	
            }
        }
//        start = pos;
        return start;
    }

    public JTextArea getSource() {
        return this.source;
    }

    public void detachFromSource(){
        this.suggestionPane.dispose();
        this.source.removeCaretListener(this);
        this.source.getDocument().removeDocumentListener(this); 
    }

    /**
     * Searches the autocompletions for candidates. Exact matches are ignored.
     * @param search What to search for
     * @return the results that match, if any
     */
    private static ArrayList<String> prefixSearcher(String search) {
        ArrayList<String> results = new ArrayList<>();
        for(String in : ExtensionState.getInstance().getKeywords()) {
        	if( !in.toLowerCase().equals(search.trim()) && in.toLowerCase().contains(search.trim()) ) {
//            if( !in.toLowerCase().equals(search.trim()) && in.toLowerCase().startsWith(search.trim()) ) {
                results.add(in);
            }
        }
        return results;
    }



    @Override
    public void insertUpdate(DocumentEvent e) {
        if (mode == MODE.COMPLETION) {
            mode = MODE.INSERT;
        } else {
            backspaceMode = false;
            if (Character.isWhitespace(this.source.getText().charAt(pos))) {
                suggestionPane.setVisible(false);

            } else {
                checkForCompletions();
            }
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        if (mode == MODE.COMPLETION) {
            mode = MODE.INSERT;
        } else {
            backspaceMode = true;
//            if (Character.isWhitespace(source.getText().charAt(pos))) {
//                suggestionPane.setVisible(false);
//            } else {
//                checkForCompletions();
//            }
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e) {

    }


    /**
     * Handles changes to the document by getting the recent word entered by the user and searching for completion candidates.
     */
    private void checkForCompletions() {
        //pos = e.getOffset();
        content = null;
       
        try {
        	System.out.println(this.source.getText());
            content = this.source.getText(0, pos + 1);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
        System.out.println(content.charAt(pos));
        System.out.println(content);
//        if (e.getLength() != 1) {
//            return;
//        }
//		
//
        int start = getTextReplacementStart();
//


//
        if (pos - start < 1 && !backspaceMode) {
            return;
        }
//
//
        String prefix = content.substring(start + 1);
        ExtensionState.getInstance().getCallbacks().printOutput("Searching for " + prefix);
        if (prefix.trim().length() == 0 || prefix.contains(":") || prefix.trim().length() == 1) {

            suggestionPane.setVisible(false);
        } else {
            ArrayList<String> matches = prefixSearcher(prefix.toLowerCase());
            ExtensionState.getInstance().getCallbacks().printOutput(Arrays.toString(matches.toArray()));
            if (matches.size() != 0) {
            	System.out.println("MAtche Sting!!!!!!!!11");
            	Point p = MouseInfo.getPointerInfo().getLocation();
            	int pointer_x = p.x;
            	int pointer_y = p.y;
            	System.out.println("X: "+ pointer_x + " Y: "+ pointer_y);
            	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
          	    int screen_height = screenSize.height;
      		    int screen_width = screenSize.width;    	
            	
      		    System.out.println("W: "+ screen_width + " H: "+ screen_height);
      		    
//      		    if(pointer_y > (screen_height-iframe_height) ) {
////            	  
//      		    	System.out.println("Set net location");	
////        		  suggestionPane.setLocation(pointer_x,pointer_y-iframe_height);
//      		    	
//      		    	suggestionPane.setLocationRelativeTo(suggestionPane);
////      		    	suggestionPane.setLocation(700,700);
////      		    	pane.setLocation(700,700);
////      		    	suggestionPane.setSize(700,700);
////						
//            	} else {
//            		suggestionPane.setSize(350,250);
//            	}
            	
                SwingUtilities.invokeLater(
                        new CompletionTask(matches));
            } else {
                suggestionPane.setVisible(false);
            }
        }
    }


    /**
     * Updates the suggestion pane with the new options
     */
    private class CompletionTask
            implements Runnable {

        CompletionTask(ArrayList<String> completions) {
            mode = MODE.COMPLETION;
            suggestionsModel.removeAllElements();
            for(String completion : completions) {
                suggestionsModel.addElement(completion);
            }
        }

        @Override
        public void run() {
            suggestionPane.setVisible(true);
            suggestionPane.toFront();
        }
    }


}
