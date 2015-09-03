package de.htwk.autolat.tools.ImportExport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Connector.AutolatConnectorException;
import de.htwk.autolat.Connector.BBautOLATConnector;
import de.htwk.autolat.TaskConfiguration.TaskConfiguration;
import de.htwk.autolat.TaskConfiguration.TaskConfigurationManagerImpl;
import de.htwk.autolat.TaskType.TaskType;
import de.htwk.autolat.TaskType.TaskTypeImpl;
import de.htwk.autolat.TaskType.TaskTypeManagerImpl;
import de.htwk.autolat.tools.XMLParser.XMLParser;
/**
 * 
 * Description:<br>
 * Retrieves the necessary informations to build a autotool node from a given xml file.
 * 
 * 
 * <P>
 * Initial Date:  27.05.2010 <br>
 * @author Joerg
 */
public class AutOlatNodeImporter {
	private Configuration conf;
	/**
	 * Constructor sets the Configuration. If null is given, a new empty instance of Configuration is created.
	 * @param conf a Configuration.
	 */
	public AutOlatNodeImporter(Configuration conf) {
		this.conf = conf;
		
		if(conf.getTaskConfiguration()==null) {
			conf.setTaskConfiguration(TaskConfigurationManagerImpl.getInstance().createAndPersistTaskConfiguration(null, "", "", "", "", "", "", false, null));
		}
	
	}
	/**
	 * get a Configuration with all informations are used to build a autotool node
	 * @param fileName the file name of the xml file
	 * @return The Configuration filled with data from the xml file
	 * @throws JDOMException
	 * @throws IOException
	 * @throws AutolatConnectorException 
	 */
	public Configuration importFromFile(File file) throws JDOMException, IOException, AutOlatExporterException, AutolatConnectorException {
		Document doc = new SAXBuilder().build(file);
		return importConfig(doc);
	}

	public Configuration importFromStream(InputStream stream) throws JDOMException, IOException, AutOlatExporterException, AutolatConnectorException {
		Document doc = new SAXBuilder().build(stream);
		return importConfig(doc);
	}

	public Configuration importConfig(Document doc) throws JDOMException, IOException, AutOlatExporterException, AutolatConnectorException {
		Element root = doc.getRootElement();
		
		@SuppressWarnings("rawtypes")
		List childs = root.getChildren();
		
		//first the server
		for (Object aChild : childs) {
			Element el = (Element)aChild;
			if(el.getName().equals(ImportExportConstants.SERVERCONN_TAG)){
				parseServerConn(el);
			}
		}
		//and now the rest
		for(Object aChild : childs) {
			Element el = (Element)aChild;
			if(el.getName().equals(ImportExportConstants.TAKTYPE_TAG)) {
				parseTaskType(el);
			}
			if(el.getName().equals(ImportExportConstants.TASKCONFIG_TAG)) {
				parseTaskConfiguration(el);
			}
			if(el.getName().equals(ImportExportConstants.CONF_TAG)) {
				parseConfiguration(el);
			}
		}
		//validate Configuration first
		validateConfiguration();
		return conf;
	}
	/**
	 * validate the configuration
	 * @throws AutOlatExporterException
	 * @throws AutolatConnectorException 
	 * @throws IOException 
	 * @throws JDOMException 
	 */
	private void validateConfiguration() throws AutOlatExporterException, JDOMException, IOException, AutolatConnectorException {
	  validateTaskType(conf.getTaskConfiguration().getTaskType());
		validateTaskConfiguration(conf.getTaskConfiguration());
		
		
		
	}
	/**
	 * validate the taskType 
	 * @param taskType
	 * @throws AutOlatExporterException
	 * @throws AutolatConnectorException 
	 * @throws IOException 
	 * @throws JDOMException 
	 */
	private void validateTaskType(TaskType taskType) throws AutOlatExporterException, JDOMException, IOException, AutolatConnectorException {
		//search Typetree for this TaskType
		BBautOLATConnector conn = new BBautOLATConnector(conf);
		
		ArrayList<TaskType> typeList = (ArrayList<TaskType>) conn.getTaskTypes();
		//if its i the List so return, else throw a exception
		for(TaskType aType : typeList) {
			if(aType.getType().equals(taskType.getType())) {
				return;
			}
		}
		throw new AutOlatExporterException(AutOlatExporterException.PARSING_ERROR + "TaskType", taskType);
	}
	/**
	 * validate the configuration
	 * @param taskConfiguration
	 * @throws AutOlatExporterException
	 * @throws AutolatConnectorException 
	 * @throws IOException 
	 * @throws JDOMException 
	 */
	private void validateTaskConfiguration(TaskConfiguration taskConfiguration) throws AutOlatExporterException, JDOMException, IOException, AutolatConnectorException {
		//validate the configuration whith the connector
		BBautOLATConnector connector = new BBautOLATConnector(conf);
		
		boolean result = connector.verifyTaskConfiguration(taskConfiguration);
		if(result) {
			connector = null;
			return;
		}
		else {
			connector = null;
			throw new AutOlatExporterException(AutOlatExporterException.PARSING_ERROR + taskConfiguration);
		}
	}
	private void parseConfiguration(Element el) {
		List<Integer> scorePoints = new ArrayList<Integer>();
		String points = el.getChildText(ImportExportConstants.SCOREPOINTS);
		if(points != null && !points.equals("")) {
			char[] values = points.toCharArray();
			String tempvalue = "";
			for(char c : values) {
				if(c!=',') tempvalue+=String.valueOf(c);
				else {
					try {
						tempvalue = tempvalue.trim();
						scorePoints.add(Integer.parseInt(tempvalue));
						tempvalue = "";
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
			if(tempvalue!="") {
				try {
					tempvalue = tempvalue.trim();
					scorePoints.add(Integer.parseInt(tempvalue));
					tempvalue = "";
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		conf.setScorePoints(scorePoints);
	}

	private void parseServerConn(Element el) throws AutOlatExporterException {
		
		String serverName = el.getAttributeValue(ImportExportConstants.SERVER_NAME);
		String serverVersion = el.getAttributeValue(ImportExportConstants.SERVER_VERSION);
		
		try {
			new XMLParser().getServerListByNameAndVersion(serverName, serverVersion).get(0);
		} catch (Exception e) {
			throw new AutOlatExporterException(AutOlatExporterException.SERVERCONNNULL);
		}
		
		conf.setAutolatServer(serverName + " <split> " + serverVersion); 
		//conf.setServerConnection(null);
	}
	
	@SuppressWarnings("unused")
	@Deprecated
	private boolean pathIsValid(String path) {
		try {
			URL url = new URL(path);
			return true;
		} catch (MalformedURLException e) {
			return false;
		}
	}
	private void parseTaskConfiguration(Element el) throws AutOlatExporterException {
		TaskConfiguration taskConf = conf.getTaskConfiguration();		
		taskConf.setAuthorComment(el.getChildText(ImportExportConstants.TASKCONFIG_AUTH_COMMENT));
		taskConf.setConfigurationText(el.getChildText(ImportExportConstants.TASKCONFIG_CONFIG_TEXT));
		taskConf.setDocumentationText(el.getChildText(ImportExportConstants.TASKCONFIG_DOC_TEXT));
		taskConf.setSignature(el.getChildText(ImportExportConstants.TASKCONFIG_SIG));
		taskConf.setIsAltered(Boolean.valueOf(el.getChildTextTrim(ImportExportConstants.TASKCONFIG_IS_ALTERED)));
		taskConf.setDescriptionText(el.getChildText(ImportExportConstants.TASKCONFIG_DESC));
		conf.setTaskConfiguration(taskConf);
	}

	private void parseTaskType(Element el) throws AutOlatExporterException, JDOMException, IOException, AutolatConnectorException {
		TaskConfiguration taskConf = conf.getTaskConfiguration();
		String typeName = el.getAttributeValue(ImportExportConstants.TASKTYPE_NAME);
		TaskType taskType = TaskTypeManagerImpl.getInstance().findTaskTypeByType(typeName);
		TaskType temp = new TaskTypeImpl();
		temp.setScoringOrder(el.getAttributeValue(ImportExportConstants.TASKTYPE_SCORING));
		temp.setType(el.getAttributeValue(ImportExportConstants.TASKTYPE_NAME));
		validateTaskType(temp);

		if(taskType != null) {
			taskConf.setTaskType(taskType);
		}
		else {
			taskType = TaskTypeManagerImpl.getInstance().createAndPersistTaskType(
					el.getAttributeValue(ImportExportConstants.TASKTYPE_NAME),
			el.getAttributeValue(ImportExportConstants.TASKTYPE_SCORING));
			taskConf.setTaskType(taskType);
		}
	}
}
