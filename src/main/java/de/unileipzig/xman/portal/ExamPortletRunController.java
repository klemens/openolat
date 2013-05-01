package de.unileipzig.xman.portal;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.home.HomeSite;

import de.unileipzig.xman.protocol.Protocol;
import de.unileipzig.xman.protocol.ProtocolManager;

public class ExamPortletRunController extends DefaultController {

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(ExamPortletRunController.class);

	private TableController examTableCtr;
	private ExamTableModel examTableMdl;
	private VelocityContainer examsVC;
	private Link showAllLink;
	
	public ExamPortletRunController(WindowControl wControl, UserRequest ureq, Translator translator) {
		super(wControl);
		
		this.examsVC = new VelocityContainer("examsVC", VELOCITY_ROOT + "/examsPortlet.html", translator, this);
		showAllLink = LinkFactory.createLink("ExamPortletRunController.showAll", examsVC, this);
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translator.translate("noexams"));
		tableConfig.setDisplayTableHeader(false);
		tableConfig.setCustomCssClass("o_pt_gt");
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);
		tableConfig.setDownloadOffered(false);
		examTableCtr = new TableController(tableConfig, ureq, getWindowControl(), translator);
						
		List<Protocol> entries = this.sortProtocolsByAppointmentDate(ProtocolManager.getInstance().findAllProtocolsByIdentity(ureq.getIdentity()));
		List<Protocol> protoList = new ArrayList<Protocol>(); 
		
		// don't show already done exams
		for ( Protocol p : entries ) {
			
			if ( p.getAppointment().getDate().getTime() > new java.util.Date().getTime() ) {
				
				protoList.add(p);
			}
		}
		
		examTableMdl = new ExamTableModel(ureq.getLocale(), protoList, true);
		examTableMdl.setTable(examTableCtr);
		
		examTableCtr.setTableDataModel(examTableMdl);

		this.examsVC.put("table", examTableCtr.getInitialComponent());

		this.setInitialComponent(examsVC);
	}

	/**
	 * Sorts the given list of protocols by the date of the appointment according to the calender. 
	 * @param tempList
	 * @return the list of protocols
	 */
	private List<Protocol> sortProtocolsByAppointmentDate(List<Protocol> tempList) {
		
		List<Protocol> protoList = new ArrayList<Protocol>();
		if ( tempList.size() > 0 ) {
			protoList.add(tempList.remove(0));
			for ( int j = 0; j < tempList.size(); j++ ) {
				int z = -1;
				for ( int i = 0; i < protoList.size(); i++ ) {
					if ( tempList.get(j).getAppointment().getDate().getTime() < protoList.get(i).getAppointment().getDate().getTime() ) {
						z = i;
						break;
					}
				}
				if ( z == -1 ) protoList.add(tempList.get(j));
				else protoList.add(z, tempList.get(j));
			}
			protoList = protoList.subList(0, protoList.size() > 7 ? 6 : protoList.size());
		}
		return protoList;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		
		if (source == showAllLink) {
			// activate group tab in top navigation
			((DTabs)Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs")).activateStatic(ureq, HomeSite.class.getName(), "exams");
		}
	}

	/**
	 * @see org.olat.core.gui.control.ControllerEventListener#dispatchEvent(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		
		if ( source == examTableCtr ) {
			if ( event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED) ) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if ( actionid.equals(ExamTableModel.CMD_LAUNCH) ) {
					int rowid = te.getRowId();
					Protocol proto = examTableMdl.getEntryAt(rowid);
					ExamListController elCtr = new ExamListController(ureq, this.getWindowControl());
					elCtr.doLaunch(ureq, proto.getExam());
				}
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		
		// POST: all firing event for the source just deregistered are finished
		// (listeners lock in EventAgency)
		if ( this.examTableCtr != null ) {
			
			this.examTableCtr.dispose();
			this.examTableCtr = null;
		}
	}
}
