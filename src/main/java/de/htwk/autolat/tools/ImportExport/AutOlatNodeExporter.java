package de.htwk.autolat.tools.ImportExport;

import java.io.OutputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.ServerConnection.ServerConnection;
import de.htwk.autolat.TaskConfiguration.TaskConfiguration;
import de.htwk.autolat.TaskType.TaskType;
import de.htwk.autolat.tools.XMLParser.AutotoolServer;
import de.htwk.autolat.tools.XMLParser.XMLParser;


/**
 * 
 * Description:<br>
 * The Exporter for the autotool node
 * <P>
 * Initial Date:  27.05.2010 <br>
 * @author Joerg
 */
public class AutOlatNodeExporter {
	/**
	 * The Configuration of the Node, that want to be exported
	 */
	private Configuration conf;
	/**
	 * Constructor sets the Configuration
	 * @param obj
	 */
	public AutOlatNodeExporter(Configuration conf) throws AutOlatExporterException {
		if(conf == null) {
			throw new AutOlatExporterException(AutOlatExporterException.CONFNULL);
		}
		this.conf = conf;
		
	}

	/**
	 * write all necessary informations of an autotool node to the given outputstream
	 * @param foStream 
	 * @return the OutputStream 
	 * @throws ParserConfigurationException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 * @throws DOMException
	 * @throws AutOlatExporterException
	 */
	public OutputStream exportNode(OutputStream foStream) throws ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException, DOMException, AutOlatExporterException {
		//create the DOM Document
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		//Define a root Element
		Element root = doc.createElement(ImportExportConstants.ROOT_ELEMENT);
		//append root Element
		doc.appendChild(root);
		
		//generate childs
		//tasktype
		root.appendChild(generateTaskTypeXML(doc));
		//TaskConfig
		root.appendChild(generateTaskConfigurationXML(doc));
		//Configuration
		root.appendChild(generateConfigurationXML(doc));
		//Server
		root.appendChild(generateServerXML(doc));
		
		//write the xml
		
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(foStream);
		transformer.transform(source, result);
		return result.getOutputStream();
		
	}
	
	private Node generateServerXML(Document doc) throws AutOlatExporterException {
		ServerConnection conn = conf.getServerConnection();
		String serverString = conf.getAutolatServer();
		if(serverString == null || serverString.equals("")) {
			throw new AutOlatExporterException(AutOlatExporterException.SERVERCONNNULL);
		}
		String serverName = null; 
		String serverVersion = null;
		AutotoolServer server = null;
		String[] temp = serverString.split("<split>");
		try {
			serverName = temp[0].trim();
			serverVersion = temp[1].trim();
			server = new XMLParser().getServerListByNameAndVersion(serverName, serverVersion).get(0);
		} catch (Exception e) {
			throw new AutOlatExporterException(AutOlatExporterException.SERVERCONNNULL);
		}
		
		if(server == null) {
			throw new AutOlatExporterException(AutOlatExporterException.SERVERCONNNULL);
		}
		
		//serverName = conn.getName();
		//String serverPath = conn.getPath();
		//String serverURL = conn.getUrl().toString();
		
		Element serverElement = doc.createElement(ImportExportConstants.SERVERCONN_TAG);
		serverElement.setAttribute(ImportExportConstants.SERVER_NAME, serverName);
		serverElement.setAttribute(ImportExportConstants.SERVER_VERSION, serverVersion);
		//server.setAttribute(ImportExportConstants.SERVER_URL, serverURL);
		//server.setAttribute(ImportExportConstants.SERVER_PATH, serverPath);
		
		return serverElement;
	}

	private Node generateConfigurationXML(Document doc) {
		List<Integer> scorepoints = conf.getScorePoints();
		String scoreString = "";
		for(int i = 0; i < scorepoints.size(); i++) {
			if(i != 0) {
				scoreString = scoreString + ",";
			}
			scoreString = scoreString + String.valueOf(scorepoints.get(i));
		}

		Element config = doc.createElement(ImportExportConstants.CONF_TAG);
		
		Element score = doc.createElement(ImportExportConstants.SCOREPOINTS);
		//score.setNodeValue(scoreString);
		score.setTextContent(scoreString);
		config.appendChild(score);
		
		return config;
	}

	private Node generateTaskConfigurationXML(Document doc) throws AutOlatExporterException {
		TaskConfiguration taskConf = conf.getTaskConfiguration();
		if(taskConf == null) {
			throw new AutOlatExporterException(AutOlatExporterException.TASKCONFIGURATIONNULL);
		}
		String authCom = taskConf.getAuthorComment();
		String confText = taskConf.getConfigurationText();		
		String docText = taskConf.getDocumentationText();
		String signature = taskConf.getSignature();
		String discription = taskConf.getDescriptionText();
		boolean isAltered = taskConf.getIsAltered();
		
		
		Element taskConfiguration = doc.createElement(ImportExportConstants.TASKCONFIG_TAG);
		taskConfiguration.setAttribute(ImportExportConstants.TASKCONFIG_IS_ALTERED, String.valueOf(isAltered));
		
		Element authComment = doc.createElement(ImportExportConstants.TASKCONFIG_AUTH_COMMENT);
		//authComment.setNodeValue(authCom);
		authComment.setTextContent(authCom);
		taskConfiguration.appendChild(authComment);
		
		Element configText = doc.createElement(ImportExportConstants.TASKCONFIG_CONFIG_TEXT);
		//configText.setNodeValue(confText);
		configText.setTextContent(confText);
		taskConfiguration.appendChild(configText);
		
		Element docuText = doc.createElement(ImportExportConstants.TASKCONFIG_DOC_TEXT);
		//docuText.setNodeValue(docText);
		docuText.setTextContent(docText);
		taskConfiguration.appendChild(docuText);
		
		Element sig = doc.createElement(ImportExportConstants.TASKCONFIG_SIG);
		//sig.setNodeValue(signature);
		sig.setTextContent(signature);
		taskConfiguration.appendChild(sig);
		
		Element disc = doc.createElement(ImportExportConstants.TASKCONFIG_DESC);
		//disc.setNodeValue(discription);
		disc.setTextContent(discription);
		taskConfiguration.appendChild(disc);		
		
		return taskConfiguration;
	}


	private Node generateTaskTypeXML(Document doc) throws AutOlatExporterException {
		TaskConfiguration taskConf = conf.getTaskConfiguration();
		if(taskConf == null) {
			throw new AutOlatExporterException(AutOlatExporterException.TASKCONFIGURATIONNULL);
		}
		TaskType ttype = taskConf.getTaskType();
		if(ttype == null) {
			throw new AutOlatExporterException(AutOlatExporterException.TASKTYPENULL);
		}
		String taskType = ttype.getType();
		String scoringOrder = ttype.getScoringOrder();
		
		Element type = doc.createElement(ImportExportConstants.TAKTYPE_TAG);
		type.setAttribute(ImportExportConstants.TASKTYPE_NAME, taskType);
		type.setAttribute(ImportExportConstants.TASKTYPE_SCORING, scoringOrder);
		return type;
	}
	
	@Deprecated
	@SuppressWarnings("unused")
	private void parseConfText(String configurationText, Document doc, Element configText) {
		
		String seperator = System.getProperty("line.separator");
		String[] substrings = configurationText.split(seperator);
		for(String aString : substrings) {
			
				Element line = doc.createElement(ImportExportConstants.TASKCONFIG_CONFIG_TEXT_LINE);
				//line.setNodeValue(aString);
				line.setTextContent(aString);				
				configText.appendChild(line);
		}
	}

}
