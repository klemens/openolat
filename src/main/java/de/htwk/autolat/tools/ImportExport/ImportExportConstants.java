package de.htwk.autolat.tools.ImportExport;
/**
 * 
 * Description:<br>
 * XML constants for the importer and exporter
 * 
 * <P>
 * Initial Date:  28.05.2010 <br>
 * @author Joerg
 */
public class ImportExportConstants {
	public static final String ROOT_ELEMENT = "autotoolnode";
	
	public static final String TAKTYPE_TAG = "tasktype";
	public static final String TASKTYPE_NAME = "type_name";
	public static final String TASKTYPE_SCORING = "type_scoring";
	
	public static final String TASKCONFIG_TAG = "taskconfiguration";
	public static final String TASKCONFIG_AUTH_COMMENT = "auth_comment";
	public static final String TASKCONFIG_CONFIG_TEXT = "conf_text";
	public static final String TASKCONFIG_CONFIG_TEXT_LINE = "line";
	public static final String TASKCONFIG_DOC_TEXT = "doc_text";
	public static final String TASKCONFIG_IS_ALTERED = "is_altered";
	public static final String TASKCONFIG_SIG = "signature";
	public static final String TASKCONFIG_DESC = "description";
	
	public static final String SERVERCONN_TAG = "server_conn";
	public static final String SERVER_NAME = "name";
	@Deprecated
	public static final String SERVER_URL = "url";
	@Deprecated
	public static final String SERVER_PATH = "path";
	public static final String SERVER_VERSION = "version";
	
	public static final String CONF_TAG = "configuration";
	public static final String SCOREPOINTS = "scorepoints";
}
