package de.unileipzig.xman.admin.controller;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.modules.co.ContactFormController;
import org.olat.resource.OLATResourceManager;

import de.unileipzig.xman.admin.ExamAdminSite;
import de.unileipzig.xman.esf.ElectronicStudentFile;
import de.unileipzig.xman.esf.ElectronicStudentFileManager;
import de.unileipzig.xman.esf.controller.ESFEditController;
import de.unileipzig.xman.esf.table.ESFTableModel;

/**
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date: 22.05.2008 <br>
 * 
 * @author gerb
 */
public class ExamAdminESFController extends BasicController {
	private VelocityContainer mainVC;

	private TableController esfTableCtr;
	private ESFTableModel esfTableMdl;

	private ContactFormController contactFormController;

	private CloseableModalController sendMailCtr;

	private DialogBoxController deleteDialog;
	private List<ElectronicStudentFile> esfList;

	private BreadcrumbedStackedPanel stack;

	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param provideLaunchButton
	 *            - true, for showing all esf which are NOT validated yet
	 */
	protected ExamAdminESFController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stack) {
		super(ureq, wControl);

		setTranslator(Util.createPackageTranslator(ExamAdminSite.class, ureq.getLocale()));

		this.stack = stack;

		setVelocityRoot(Util.getPackageVelocityRoot(ExamAdminSite.class));
		this.mainVC = createVelocityContainer("examCategories", "esf");

		init(ureq, wControl);
		
		this.putInitialPanel(mainVC);
	}
	
	private void init(UserRequest ureq, WindowControl wControl) {
		buildView(ureq, wControl);
	}

	/**
	 * Build the ESFTableDataModel
	 * 
	 * @param ureq
	 *            - the UserRequest
	 * @param wControl
	 *            - the WindowControl
	 */
	private void buildView(UserRequest ureq, WindowControl wControl) {
		List<ElectronicStudentFile> esfList = ElectronicStudentFileManager.getInstance().retrieveAllElectronicStudentFiles();

		TableGuiConfiguration esfTableConfig = new TableGuiConfiguration();
		esfTableConfig.setMultiSelect(true);
		esfTableConfig.setDownloadOffered(true);
		esfTableConfig.setPageingEnabled(true);
		esfTableConfig.setTableEmptyMessage(translate("ExamAdminESFController.emptyTableMessage"));
		esfTableConfig.setShowAllLinkEnabled(true);
		esfTableConfig.setPreferencesOffered(true, "pref");
		esfTableCtr = new TableController(esfTableConfig, ureq, wControl, getTranslator(), true /*enableTableSearch*/);
		esfTableCtr.setMultiSelect(true);
		esfTableCtr.addMultiSelectAction("ExamAdminESFController.delete", ESFTableModel.COMMAND_DELETE);
		esfTableCtr.addMultiSelectAction("ExamAdminESFController.sendMail", ESFTableModel.COMMAND_SENDMAIL);
		esfTableMdl = new ESFTableModel(getLocale(), esfList);
		esfTableMdl.setTable(esfTableCtr);
		esfTableCtr.setTableDataModel(esfTableMdl);
		// 0+1 because multiselection counts as 1 (0 is username)
		esfTableCtr.setSortColumn(0 + 1, true);
		esfTableCtr.addControllerListener(this);

		mainVC.put("esfTable", esfTableCtr.getInitialComponent());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	protected void doDispose() {

		this.esfTableCtr = null;
		this.esfTableMdl = null;
		this.mainVC = null;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller ctr, Event event) {
		// the table Controller
		if (ctr == esfTableCtr) {

			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {

				TableEvent te = (TableEvent) event;
				String actionID = te.getActionId();

				// somebody wants to open an esf
				if (actionID.equals(ESFTableModel.COMMAND_OPEN)) {
					ElectronicStudentFile esf = (ElectronicStudentFile) esfTableCtr.getTableDataModel().getObject(te.getRowId());

					// add the esf to stack
					ESFEditController esfEditCtr = new ESFEditController(ureq, getWindowControl(), esf);
					stack.pushController(esf.getIdentity().getName(), esfEditCtr);
				}
			}

			// multiple identities were choosen
			if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
				TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;

				// get all selected esf's and save them in a field cause we need later (deleteDialog)
				esfList = new ArrayList<ElectronicStudentFile>();
				for (int i = tmse.getSelection().nextSetBit(0); i >= 0; i = tmse.getSelection().nextSetBit(i+1)) {
					esfList.add((ElectronicStudentFile) esfTableCtr.getTableDataModel().getObject(i));
				}

				if (tmse.getAction().equals(ESFTableModel.COMMAND_DELETE)) {
					if (esfList.size() == 0) {
						getWindowControl().setWarning(translate("ExamAdminESFController.nobodySelected"));
					} else {
						deleteDialog = DialogBoxUIFactory.createOkCancelDialog(ureq, this.getWindowControl(),
										translate("ExamAdminESFController.deleteESF.title"),
										translate("ExamAdminESFController.deleteESF.text"));
						deleteDialog.addControllerListener(this);
						deleteDialog.activate();
					}

				}

				// someone wants to send students an email
				if (tmse.getAction().equals(ESFTableModel.COMMAND_SENDMAIL)) {
					this.sendMailsToSelectedStudents(esfList, ureq);
				}
			}
		}

		if (ctr == deleteDialog) {

			if (DialogBoxUIFactory.isOkEvent(event)) {

				for (ElectronicStudentFile esf : esfList) {

					// refresh possible modifications on the esf
					ElectronicStudentFile tempESF = ElectronicStudentFileManager
							.getInstance().retrieveESFByIdentity(
									esf.getIdentity());
					ElectronicStudentFileManager.getInstance()
							.removeElectronicStudentFile(tempESF);
				}
				init(ureq, this.getWindowControl());
			}
		}

		// remove the modal controller
		if (ctr == contactFormController) {

			if (event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT
					|| event == Event.FAILED_EVENT) {

				this.getWindowControl().pop();
			}
		}
	}

	/**
	 * 
	 * 
	 * @param objects
	 * @param ureq
	 */
	private void sendMailsToSelectedStudents(
			List<ElectronicStudentFile> objects, UserRequest ureq) {

		List<ElectronicStudentFile> esfList = (List<ElectronicStudentFile>) objects;

		if (esfList.size() >= 1) {

			// add the user as sender
			ContactMessage contactMsg = new ContactMessage(ureq.getIdentity());

			// create the recipients list
			ContactList emailList = new ContactList(translate("ExamAdminESFController.sendMails.recipients"));

			// add selected students to to-list of email
			for (ElectronicStudentFile esf : esfList) {

				emailList.add(esf.getIdentity());
			}

			contactMsg.addEmailTo(emailList);

			contactFormController = new ContactFormController(ureq, getWindowControl(), true, false, false, contactMsg);
			listenTo(contactFormController);

			sendMailCtr = new CloseableModalController(getWindowControl(),
					translate("close"), contactFormController
							.getInitialComponent());
			this.listenTo(sendMailCtr);
			sendMailCtr.activate();
		} else {

			this
					.getWindowControl()
					.setInfo(translate("ExamAdminESFController.sendEmail.nothingSelected"));
		}
	}
}
