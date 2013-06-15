/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package de.htwk.autolat.BBautOLAT;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.record.formula.functions.Even;
import org.jdom.JDOMException;
import org.json.XML;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

//import org.olat.core.gui.formelements.CheckBoxElement;
//import org.olat.core.gui.formelements.StaticSingleSelectionElement;
//import org.olat.core.gui.formelements.TextElement;
import org.olat.core.gui.translator.Translator;
import org.olat.course.nodes.en.ENEditController;

import de.bps.course.nodes.vc.DefaultVCConfiguration;
import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Configuration.ConfigurationManagerImpl;
import de.htwk.autolat.ServerConnection.ServerConnection;
import de.htwk.autolat.ServerConnection.ServerConnectionManagerImpl;
import de.htwk.autolat.tools.XMLParser.AutotoolServer;
import de.htwk.autolat.tools.XMLParser.XMLParser;

/**
 * Description:<br>
 * Form to edit the Connection to the autotool Server
 * 
 * <P>
 * Initial Date:  05.05.2012 <br>
 * @author Jörg Werner
 */
public class EditConnectionForm extends FormBasicController {
	
	public static final String NAME = "EditConnectionForm";
	
	private Configuration conf;
	private boolean usedAsPopup;
	private ServerConnection connection;
	private List<ServerConnection> connectionList;
	private List<AutotoolServer> serverList;
	
	private long courseID;
	private long courseNodeID;
	
	//GUI
	//private FormSubmit subm;
	//private TextElement enteredName;
	//private TextElement enteredConnection;
	//private SingleSelection selectGivenConnection;
	//private String selectedConnection;
	//private SelectionElement useGivenConnection;
	private SingleSelection selectAutotoolServer;
	
	public static final String CONST_NOTCHOSEN = ".notchosen.";
	
	public EditConnectionForm(UserRequest ureq, WindowControl wControl, boolean usedAsPopup, long courseID, long courseNodeID, ServerConnection conn) {
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		
		this.courseID = courseID;
		this.courseNodeID = courseNodeID;
		
		if(!usedAsPopup) this.conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, courseNodeID);
		else this.conf = null;
		this.usedAsPopup = usedAsPopup;
		this.connection = conn;
		
		connectionList =  ServerConnectionManagerImpl.getInstance().getAllActiveServerConnections();
		XMLParser parser = new XMLParser();
		try {
			serverList = parser.getServerListByNameAndVersion(null, null);
		} catch (JDOMException e) {
			serverList = null;
			showInfo("error.form.editconnection.XMLerror");
		} catch (IOException e) {
			serverList = null;
			showInfo("error.form.editconnection.IOerror");
		}
		
		initForm(this.flc, this, ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
			
			String[] keys = new String[serverList.size()+1];
			String[] values = new String[serverList.size()+1];
			
			keys[0] = CONST_NOTCHOSEN;
			values[0] = translate("label.selectelem.editconnection.noserverchosen");
			for(int i = 0; i < serverList.size(); i++) {
				keys[i+1] = serverList.get(i).getName() + " <split> " + serverList.get(i).getVersion();
				values[i+1] = serverList.get(i).getName() + " (Version: " + serverList.get(i).getVersion() + ")";
			}
		
			selectAutotoolServer = uifactory.addDropdownSingleselect("selectServer", "label.form.editconnection.selectserver",
					formLayout, keys, values, null);
			
			if(conf.getAutolatServer() != null) {
				String serverName = null;
				String serverVersion = null;
				try {
					serverName = conf.getAutolatServer().split("<split>")[0].trim();
					serverVersion = conf.getAutolatServer().split("<split>")[1].trim();
					selectAutotoolServer.select(serverName + " <split> " + serverVersion, true);
				} catch (Exception e) {
					selectAutotoolServer.select(CONST_NOTCHOSEN, true);
				}
				
			} else {
				selectAutotoolServer.select(CONST_NOTCHOSEN, true);
			}
			
			//enteredName = uifactory.addTextElement("enteredName", "label.form.editconnection.enteredname", 100, "", formLayout);
			//enteredConnection = uifactory.addTextElement("enteredConnection", "label.form.editconnection.enteredconnection", 150, "", formLayout);
		
			//String keys[] = new String[connectionList.size()+1];
			//String labels[] = new String[connectionList.size()+1];
			//keys[0] = CONST_NOTCHOSEN;
			//labels[0] = translate("label.selectelem.editconnection.noconnectionchosen");
			
			/*if(!usedAsPopup) {
				int i = 0;
				Iterator<ServerConnection> connectionIterator = connectionList.iterator();
				while(connectionIterator.hasNext()) {
					ServerConnection temp = connectionIterator.next();
					i++;
					keys[i] = String.valueOf(temp.getKey());
					labels[i] = temp.getName() + " / " + temp.getUrl().getHost() + temp.getUrl().getPath();
				}
			}*/
			
			/*if(!usedAsPopup) {
				String cbkey[] = {"0"};
				String cblabel[] = {translate("label.form.editconnection.usegivenconnection")};
				useGivenConnection = uifactory.addCheckboxesHorizontal("useGivenConnection", "label.form.editconnection.usegivenconnection", formLayout,
						cbkey, cblabel, null);
				selectGivenConnection = uifactory.addDropdownSingleselect("selectGivenConnection", "label.form.editconnection.givenconnections",
						formLayout, keys, labels, null);
				//selectGivenConnection.addActionListener(this, FormEvent.ONCHANGE);
				//useGivenConnection.addActionListener(this, FormEvent.ONCHANGE);
			}
			else {//set the entries to the text elements
				enteredName.setValue(connection.getName());
				enteredConnection.setValue(connection.getPath());
			}*/
			
			/*if(!usedAsPopup) {
				if(conf.getServerConnection()!=null) {
					useGivenConnection.select("0", true);
					//getCheckBoxElement("useGivenConnection").setChecked(true);
					selectGivenConnection.select(String.valueOf(conf.getServerConnection().getKey()), true);
					//getSingleSelectionElement("selectGivenConnection").select(String.valueOf(conf.getServerConnection().getKey()), true);
					selectGivenConnection.setMandatory(true);
					//getSingleSelectionElement("selectGivenConnection").setMandatory(true);
				} else {
					enteredName.setMandatory(true);
					enteredConnection.setMandatory(true);
				}
			}*/
			
			uifactory.addFormSubmitButton("submit", formLayout);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		/*if(!usedAsPopup) {//register the choosen connection for the given configuration
			if(useGivenConnection.isSelected(0)) {
				if(selectGivenConnection.getSelectedKey()!=CONST_NOTCHOSEN) {
					String key = selectGivenConnection.getSelectedKey();
					ServerConnection connection = ServerConnectionManagerImpl.getInstance().getServerConnectionByKey(Long.parseLong(key));
					conf.setServerConnection(connection);
					ConfigurationManagerImpl.getInstance().updateConfiguration(conf);
				}
				else {
					selectGivenConnection.setErrorKey("error.selectelem.editconnection.noconnectionchosen", null);
				}
			}
			else {
				String newConnection = enteredConnection.getValue();
				try {
					URL url = new URL(newConnection); //to provoke an error if the given path is not a valid url
					String name = enteredName.getValue();
					ServerConnection connection = ServerConnectionManagerImpl.getInstance().createAndPersistServerConnection(new Date(), name, newConnection, true);
					conf.setServerConnection(connection);
					ConfigurationManagerImpl.getInstance().updateConfiguration(conf);
					//return true;
				} catch(Exception e) {
					enteredConnection.setErrorKey("error.form.editconnection.invalidurl", null);
					return;
				}
			}
		}
		else { //update the given connection
			String newConnection = enteredConnection.getValue();
			try {
				URL url = new URL(newConnection); //to provoke an error if the given path is not a valid url
				String name = enteredName.getValue();
				connection.setName(name);
				connection.setPath(newConnection);
				ServerConnectionManagerImpl.getInstance().updateServerConnection(connection);
				//return true;
			} catch(Exception e) {
				enteredConnection.setErrorKey("error.form.editconnection.invalidurl", null);
				return;
			}
		}*/
		if(selectAutotoolServer.getSelectedKey().equals(CONST_NOTCHOSEN)) {
			selectAutotoolServer.setErrorKey("error.selectelem.editconnection.noserverchosen", null);
			return;
		} else {
			String serverName, serverVersion, selectedKey;
			selectedKey = selectAutotoolServer.getSelectedKey();
			String[] tempSplit = selectedKey.split("<split>");
			try {
				serverName = tempSplit[0].trim();
				serverVersion = tempSplit[1].trim();
			} catch (ArrayIndexOutOfBoundsException e) {
				selectAutotoolServer.setErrorKey("error.selectelem.editconnection.arrayerror", null);
				return;
			}
			AutotoolServer selectedServer;
			XMLParser parser = new XMLParser();
			try {
				selectedServer = parser.getServerListByNameAndVersion(serverName, serverVersion).get(0);
			} catch (JDOMException e) {
				showInfo("error.form.editconnection.XMLerror");
				return;
			} catch (IOException e) {
				showInfo("error.form.editconnection.IOerror");
				return;
			}
			conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, courseNodeID);
			conf.setAutolatServer(selectedServer.getName() + " <split> " + selectedServer.getVersion());
			ConfigurationManagerImpl.getInstance().updateConfiguration(conf);
			fireEvent(ureq, FormEvent.DONE_EVENT);
		}
		
		
		
	}
	
	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
	}

}
