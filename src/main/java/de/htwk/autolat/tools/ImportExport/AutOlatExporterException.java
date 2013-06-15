package de.htwk.autolat.tools.ImportExport;
/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  06.06.2010 <br>
 * @author Joerg
 */
public class AutOlatExporterException extends Exception {
	public static String TASKTYPENULL = "Tasktype must not be null";
	public static String TASKCONFIGURATIONNULL = "Taskconfiguration must not be null";
	public static String SERVERCONNNULL = "Serverconnection must not ne null";
	public static String CONFNULL = "Configuration must not be null";
	public static String PARSING_ERROR = "The following field is not valid: ";
	
	private Object wrongField;
	
	public AutOlatExporterException() {
	}
	
	public AutOlatExporterException(String message) {
		super(message);
		wrongField = null;
	}
	
	public AutOlatExporterException(String message, Object wrongField) {
		super(message);
		this.wrongField = wrongField;
	}
	
	public Object getWrongField() {
		return wrongField;
	}
}
