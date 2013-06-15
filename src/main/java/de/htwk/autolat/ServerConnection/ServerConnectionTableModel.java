package de.htwk.autolat.ServerConnection;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

import de.htwk.autolat.BBautOLAT.EditConnectionController;

/**
 * Description:<br>
 * The table model for the server connections used for viewing and editing the connections.
 * 
 * <P>
 * Initial Date:  20.11.2009 <br>
 * @author Tom
 */
public class ServerConnectionTableModel extends DefaultTableDataModel implements TableDataModel {

	/** The Constant PACKAGE. */
	private static final String PACKAGE = Util.getPackageName(ServerConnectionTableModel.class);
	
	/** The Constant COLUMN_COUNT. */
	private final int COLUMN_COUNT = 7;
	
	/** The translator. */
	private PackageTranslator translator;
	
	/**
	 * Instantiates a new server connection table model.
	 * 
	 * @param objects The objects for the table model
	 * @param locale The locale to show the commands in the appropiate language
	 */
	public ServerConnectionTableModel(List objects, Locale locale) {
		
		super(objects);
		this.translator = new PackageTranslator(PACKAGE, locale);
	}

	/**
	 * Gets the column count.
	 * 
	 * @return The column count
	 * @see org.olat.core.gui.components.table.DefaultTableDataModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		
		return COLUMN_COUNT;
	}

	/**
	 * Gets the value at.
	 * 
	 * @param row The row
	 * @param col The column
	 * @return The value at row and column
	 * @see org.olat.core.gui.components.table.DefaultTableDataModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int row, int col) {
		
		Object[] wrapper = (Object[]) objects.get(row);		
		ServerConnection connection = (ServerConnection) wrapper[0];
		
		switch(col) {
			case 0:
				return connection.getName();
			case 1:
				return connection.getUrl().getHost();
			case 2:
				return connection.getLastContact();
			case 3: //returns the active state
				return (connection.getIsActive() ? translator.translate("label.tablemodel.connection.yes") : translator.translate("label.tablemodel.connection.no"));
			case 4: //returns the "set active"-label
				return (!connection.getIsActive() ? translator.translate("label.tablemodel.connection.switch") : "");
			case 5: //returns the "edit"-label
				return translator.translate("label.tablemodel.connection.edit");			
			case 6: //returns the "delete"-label if connection is currently unused
				List<ServerConnection> usedConnections = ServerConnectionManagerImpl.getInstance().getAllUsedConnections();
				if(usedConnections.contains(connection))
					return "";
				else			
					return translator.translate("label.tablemodel.connection.delete");
			default:
				return "unknown";
		}
	}
	
	/**
	 * Sets the entries.
	 * 
	 * @param objects The new entries
	 */
	public void setEntries (List objects) {
		this.objects = objects;
	}
	
	/**
	 * Gets the server connection at row.
	 * 
	 * @param row The row
	 * @return The server connection at the given row
	 */
	public ServerConnection getServerConnctionAtRow (int row) {
		
		Object[] wrapper = (Object[]) objects.get(row);		
		ServerConnection connection = (ServerConnection) wrapper[0];
		return connection;
	}

}
