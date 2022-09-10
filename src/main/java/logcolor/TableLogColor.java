package logcolor;

import java.awt.Color;

import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class TableLogColor {
	
	public JSplitPane request_log_splitpane;
	public final Class[] table_classes;
	public DefaultTableModel table_model;
	public JTable logTable;
	public TableLogColor() {
		
		request_log_splitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		table_classes = new Class[] { Integer.class, Long.class, String.class, String.class, String.class, String.class };
		
		table_model = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex < table_classes.length)
					return table_classes[columnIndex];
				return super.getColumnClass(columnIndex);
			}
		};
		
		
		table_model.addColumn("#");
		table_model.addColumn("Host");
		table_model.addColumn("Method");
		table_model.addColumn("URL");
		table_model.addColumn("Status");
		table_model.addColumn("Length");
		table_model.addColumn("TimeSleep");
		table_model.addColumn("Port");
		table_model.addColumn("TimeSend");
		table_model.addColumn("Comment1");
		table_model.addColumn("Comment2");
		table_model.addColumn("Comment3");
		
		logTable = new JTable(table_model);
		
		logTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		logTable.getColumnModel().getColumn(0).setMaxWidth(50);
		logTable.getColumnModel().getColumn(2).setPreferredWidth(80);
		logTable.getColumnModel().getColumn(2).setMaxWidth(80);
		
		DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
		cellRenderer.setBackground(new Color(255,135,135));
		logTable.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
		
		logTable.setAutoCreateRowSorter(true);
		logTable.setRowSelectionAllowed(true);
		//"#","Host","Method","URL","Status","Length","TimeSleep","Port","TimeSend","Comment1","Comment2","Comment3,
		table_model.addRow(new Object[]{"1", "example.com", "GET", "/", "200", "100", "200", "443", "null", "null", "null"});
		table_model.addRow(new Object[]{"2", "example2.com", "POST", "/", "404", "50", "100", "80", "null", "null", "null"});
		
	}
	public  DefaultTableModel getTableModel() {
		return table_model;
	}
	public  JTable getLogTable() {
		return logTable;
	}
	
	public void setNewRow(Object[] objects) {
		
	
	}
}
