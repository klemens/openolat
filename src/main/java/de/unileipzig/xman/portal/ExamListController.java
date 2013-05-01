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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package de.unileipzig.xman.portal;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.RepositoryDetailsController;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.resource.OLATResourceManager;

import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.protocol.Protocol;
import de.unileipzig.xman.protocol.ProtocolManager;

/**
 * Description:<br>
 * Shows the list of all exams for this user
 * <P>
 * Initial Date:  11.07.2005 <br>
 * @author blutz
 */
public class ExamListController extends DefaultController {
	
	private static final String CMD_SHOW = "ExamListController.cmd.show";
	
	private TableController examTableCtr;
	private ExamTableModel examTableMdl;
	private Translator translator;
	private Panel content;
	
	/**
	 * Constructor
	 * @param wControl
	 * @param ureq
	 */
	public ExamListController(UserRequest ureq, WindowControl wControl) { 
		super(wControl);
		
		this.translator = new PackageTranslator(Util.getPackageName(ExamListController.class), ureq.getLocale());
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translator.translate("noexams"));
		examTableCtr = new TableController(tableConfig, ureq, getWindowControl(), translator);
		
		List<Protocol> protocols = ProtocolManager.getInstance().findAllProtocolsByIdentity(ureq.getIdentity());
		examTableMdl = new ExamTableModel(ureq.getLocale(), protocols, false);
		examTableMdl.setTable(examTableCtr);
		
		examTableCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_SHOW, "ExamListController.table.header.action", translator.translate(CMD_SHOW)));
		examTableCtr.setTableDataModel(examTableMdl);

		content = new Panel("content");
		content.setContent(examTableCtr.getInitialComponent());
		
		setInitialComponent(content);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		
		// nothing to catch
	}

	/**
	 * @see org.olat.core.gui.control.ControllerEventListener#dispatchEvent(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		
		if ( source == examTableCtr ) {
			if ( event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED) ) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				int rowid = te.getRowId();
				if (actionid.equals(CMD_SHOW)) {
					Protocol proto = examTableMdl.getEntryAt(rowid);
					this.doLaunch(ureq, proto.getExam());
				}
			}
		} 
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		
		if (this.examTableCtr != null) {
			this.examTableCtr.dispose();
			this.examTableCtr = null;
		}
		
	}
	
	/**
	 * Launches the given exam as a dtab. Fires an Event.DONE_EVENT when done.
	 * @param ureq
	 * @param exam
	 */
	public void doLaunch(UserRequest ureq, Exam exam) {
		
		OLATResourceable ores = OLATResourceManager.getInstance().findResourceable(exam.getResourceableId(), Exam.ORES_TYPE_NAME);
		RepositoryEntry repositoryEntry = RepositoryManager.getInstance().lookupRepositoryEntry(ores, false);
		
		RepositoryHandler typeToLaunch = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
		if (typeToLaunch == null){
			StringBuffer sb = new StringBuffer(translator.translate("error.launch"));
			sb.append(": No launcher for repository entry: ");
			sb.append(repositoryEntry.getKey());
			throw new OLATRuntimeException(RepositoryDetailsController.class,sb.toString(), null);
		}
		RepositoryManager.getInstance().incrementLaunchCounter(repositoryEntry);
		String displayName = repositoryEntry.getDisplayname();

		DTabs dts = (DTabs)Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs");
		DTab dt = dts.getDTab(ores);
		if (dt == null) {
			// does not yet exist -> create and add
			dt = dts.createDTab(ores, displayName);
			if (dt == null) return;
			
			// build up the context path
			OLATResourceable businessOres = repositoryEntry;
			ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(businessOres);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, dt.getWindowControl());

	// --------------------------------------------- alt: getLaunchController()
			Controller ctrl = typeToLaunch.createLaunchController(ores, ureq, bwControl);
			// if resource is an image, PDF or eq. (e.g. served by resulting media request), no controller is returned.
			// FIXME:fj:test this
			if (ctrl == null) return;
			dt.setController(ctrl);
			dts.addDTab(ureq, dt);
		}
		dts.activate(ureq, dt, RepositoryDetailsController.ACTIVATE_RUN);	

		fireEvent(ureq, Event.DONE_EVENT);
	}

}