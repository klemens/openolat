package de.unileipzig.xman.admin.controller;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;

import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.util.ComponentUtil;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.resource.OresHelper;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.wiki.WikiManager;
import org.olat.modules.wiki.WikiPage;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResourceManager;
import org.olat.user.HomePageConfigManager;
import org.olat.user.HomePageConfigManagerImpl;
import org.olat.user.UserInfoMainController;

import de.unileipzig.xman.admin.ExamAdminSite;
import de.unileipzig.xman.admin.mail.MailManager;
import de.unileipzig.xman.comment.CommentEntry;
import de.unileipzig.xman.comment.CommentManager;
import de.unileipzig.xman.esf.ElectronicStudentFile;
import de.unileipzig.xman.esf.ElectronicStudentFileManager;
import de.unileipzig.xman.esf.controller.ESFCreateController;
import de.unileipzig.xman.esf.controller.ESFEditController;
import de.unileipzig.xman.esf.controller.ESFLaunchController;
import de.unileipzig.xman.esf.table.ESFTableModel;
import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.exam.ExamDBManager;
import de.unileipzig.xman.protocol.Protocol;
import de.unileipzig.xman.protocol.tables.ProtocolTableModel;

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

	private static final String VELOCITY_ROOT = Util
			.getPackageVelocityRoot(ExamAdminSite.class);

	private Translator translator;
	private VelocityContainer mainVC;

	private TableController esfTableCtr;
	private ESFTableModel esfTableMdl;

	private ContactFormController contactFormController;

	private CloseableModalController sendMailCtr;

	private DialogBoxController deleteDialog;
	private List<ElectronicStudentFile> esfList;

	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param provideLaunchButton
	 *            - true, for showing all esf which are NOT validated yet
	 */
	protected ExamAdminESFController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		this.translator = Util.createPackageTranslator(ExamAdminSite.class, ureq.getLocale());
		this.mainVC = new VelocityContainer("examCategories", VELOCITY_ROOT + "/esf.html", translator, this);

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
		esfTableConfig.setTableEmptyMessage(translator.translate("ExamAdminESFController.emptyTableMessage"));
		esfTableConfig.setShowAllLinkEnabled(true);
		esfTableConfig.setPreferencesOffered(true, "pref");
		esfTableCtr = new TableController(esfTableConfig, ureq, wControl, translator, true /*enableTableSearch*/);
		esfTableCtr.setMultiSelect(true);
		esfTableCtr.addMultiSelectAction("ExamAdminESFController.delete", ESFTableModel.COMMAND_DELETE);
		esfTableCtr.addMultiSelectAction("ExamAdminESFController.sendMail", ESFTableModel.COMMAND_SENDMAIL);
		esfTableMdl = new ESFTableModel(translator.getLocale(), esfList);
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
		this.translator = null;
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

					ElectronicStudentFile esf = esfTableMdl.getObject(te.getRowId());
					OLATResourceable ores = OLATResourceManager.getInstance()
							.findResourceable(esf.getResourceableId(),
									ElectronicStudentFile.ORES_TYPE_NAME);

					// add the esf in a dtab
					DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDTabs();
					DTab dt = dts.getDTab(ores);
					if (dt == null) {
						// does not yet exist -> create and add
						dt = dts.createDTab(ores, esf.getIdentity().getName());
						if (dt == null)
							return;
						ESFEditController esfLaunchCtr = new ESFEditController(
								ureq, dt.getWindowControl(), esf);
						dt.setController(esfLaunchCtr);
						dts.addDTab(ureq, dt);
					}
					dts.activate(ureq, dt, null);
				}
			}

			// multiple identities were choosen
			if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
				TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;

				if (tmse.getAction().equals(ESFTableModel.COMMAND_DELETE)) {
					// get all selected esf's and save them in a field cause we
					// need later (deleteDialog)
					esfList = esfTableMdl.getObjects(tmse.getSelection());

					if (esfList.size() == 0) {
						getWindowControl().setWarning(translator.translate("ExamAdminESFController.nobodySelected"));
					} else {
						deleteDialog = DialogBoxUIFactory.createOkCancelDialog(ureq, this.getWindowControl(),
										translator.translate("ExamAdminESFController.deleteESF.title"),
										translator.translate("ExamAdminESFController.deleteESF.text"));
						deleteDialog.addControllerListener(this);
						deleteDialog.activate();
					}

				}

				// someone wants to send students an email
				if (tmse.getAction().equals(ESFTableModel.COMMAND_SENDMAIL)) {
					this.sendMailsToSelectedStudents(esfTableMdl.getObjects(tmse.getSelection()), ureq);
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
			ContactList emailList = new ContactList(translator
					.translate("ExamAdminESFController.sendMails.recipients"));

			// add selected students to to-list of email
			for (ElectronicStudentFile esf : esfList) {

				emailList.add(esf.getIdentity());
			}

			contactMsg.addEmailTo(emailList);

			contactFormController = new ContactFormController(ureq, getWindowControl(), true, false, false, contactMsg);
			listenTo(contactFormController);

			sendMailCtr = new CloseableModalController(getWindowControl(),
					translator.translate("close"), contactFormController
							.getInitialComponent());
			this.listenTo(sendMailCtr);
			sendMailCtr.activate();
		} else {

			this
					.getWindowControl()
					.setInfo(
							translator
									.translate("ExamAdminESFController.sendEmail.nothingSelected"));
		}
	}
}
