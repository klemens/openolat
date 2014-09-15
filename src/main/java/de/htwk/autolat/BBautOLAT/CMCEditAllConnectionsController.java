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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.util.Util;

import de.htwk.autolat.ServerConnection.ServerConnection;
import de.htwk.autolat.ServerConnection.ServerConnectionManagerImpl;
import de.htwk.autolat.ServerConnection.ServerConnectionTableModel;

/**
 * Description:<br>
 * TODO: Tom Class Description for CMCEditAllConnectionsController
 * 
 * <P>
 * Initial Date:  20.11.2009 <br>
 * @author Tom
 */
public class CMCEditAllConnectionsController extends BasicController {

	private final String KEY_SWITCHACTIVE = "key.switchactive";
	private final String KEY_DELETE = "key.delete";
	private final String KEY_EDIT = "key.edit";
	
	private TableController connectionListCtr;
	private ServerConnectionTableModel connectionTableModel;

	private EditConnectionForm editConnectionForm;
	private CloseableModalController CMCEditConnectionCtr;
	
	private static final String PACKAGE = Util.getPackageName(CMCEditAllConnectionsController.class);
	
	private VelocityContainer mainvc;
	
	/**
	 * @param ureq
	 * @param control
	 */
	public CMCEditAllConnectionsController(UserRequest ureq, WindowControl control) {
		super(ureq, control);
		
		this.setTranslator(new PackageTranslator(PACKAGE, getLocale()));
		
		mainvc = this.createVelocityContainer("CMCeditAllConnectionsController");
		initConnectionListCtr(ureq);
		updateConnectionListCtr(ServerConnectionManagerImpl.getInstance().getAllServerConnections());
		putInitialPanel(mainvc);
	}
	
	private void initConnectionListCtr(UserRequest ureq) {
		// 1) initialize listing controller
		TableGuiConfiguration tableConfig = new TableGuiConfiguration(); // table configuration
		tableConfig.setTableEmptyMessage(translate("label.controller.cmceditallconnections.noconnections")); // message for empty table
		removeAsListenerAndDispose(connectionListCtr);
	  connectionListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator()); // reference on table controller
		listenTo(connectionListCtr); // focus on table controller

		// 2) naming the columns
		connectionListCtr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.cmceditallconnections.name", 0, null, getLocale()));
		connectionListCtr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.cmceditallconnections.path", 1, null, getLocale()));
		connectionListCtr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.cmceditallconnections.lastcontact", 2, null, getLocale()));
		connectionListCtr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.cmceditallconnections.isactive", 3, null, getLocale()));
		connectionListCtr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.cmceditallconnections.switchactive", 4, KEY_SWITCHACTIVE, getLocale()));
		connectionListCtr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.cmceditallconnections.edit", 5, KEY_EDIT, getLocale()));
		connectionListCtr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.cmceditallconnections.delete", 6, KEY_DELETE, getLocale()));
		
		// 3) initialize the model
		connectionTableModel = new ServerConnectionTableModel(new ArrayList(), getLocale());
		connectionListCtr.setTableDataModel(connectionTableModel);
		mainvc.put("connectionList", connectionListCtr.getInitialComponent());
	}
	
	private void updateConnectionListCtr(List inputList) {
		List wrapped = new ArrayList();
		List connections = inputList;
		Iterator iter = connections.iterator();
		while (iter.hasNext()) {   // running through all connections
			ServerConnection connection = (ServerConnection) iter.next();
			wrapped.add(wrapServerConnection(connection));
		}

		connectionTableModel.setEntries(wrapped);
		connectionListCtr.modelChanged();   // update is needed in the model
	}
	
	private Object wrapServerConnection(ServerConnection connection) {

		return new Object[] {connection};
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
	// TODO Auto-generated method stub

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component comp, Event evnt) {

	}
	
	protected void event(UserRequest ureq, Controller ctr, Event evnt) {
		
		if(ctr == editConnectionForm) {
			if(evnt.equals(Form.EVNT_VALIDATION_OK)) {
				CMCEditConnectionCtr.deactivate();
				updateConnectionListCtr(ServerConnectionManagerImpl.getInstance().getAllServerConnections());
			}
		}
		
		if(ctr == connectionListCtr) {
			if(evnt.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) evnt;
				if(te.getActionId().equals(KEY_SWITCHACTIVE)) {
					ServerConnectionManagerImpl.getInstance().setActive(connectionTableModel.getServerConnctionAtRow(te.getRowId()),true);
					updateConnectionListCtr(ServerConnectionManagerImpl.getInstance().getAllServerConnections());
				}
				if(te.getActionId().equals(KEY_DELETE)) {
					ServerConnectionManagerImpl.getInstance().deleteServerConnection(connectionTableModel.getServerConnctionAtRow(te.getRowId()));
					updateConnectionListCtr(ServerConnectionManagerImpl.getInstance().getAllServerConnections());
				}
				if(te.getActionId().equals(KEY_EDIT)) {
					//FIXME Warum ist der courseNodeID-Parameter 0 ?
					editConnectionForm = new EditConnectionForm(ureq, getWindowControl(), true, 0, 0,
							connectionTableModel.getServerConnctionAtRow(te.getRowId()));
					//editConnectionForm = new EditConnectionForm(EditConnectionForm.NAME, getTranslator(), true, 0,
					//		connectionTableModel.getServerConnctionAtRow(te.getRowId()));
					editConnectionForm.addControllerListener(this);
					VelocityContainer newVC = this.createVelocityContainer("CMCEditConnectionController");
					newVC.put("editConnectionForm", editConnectionForm.getInitialComponent());
					CMCEditConnectionCtr = new CloseableModalController(getWindowControl(), 
							translate("label.controller.cmceditallconnections.close"), newVC);
					CMCEditConnectionCtr.activate();
				}
			}
		}
	}

}
