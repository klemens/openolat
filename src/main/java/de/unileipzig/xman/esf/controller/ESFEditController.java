package de.unileipzig.xman.esf.controller;

import java.util.ArrayList;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController; //#### Änderung Name
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;

import de.unileipzig.xman.comment.CommentEntry;
import de.unileipzig.xman.comment.CommentManager;
import de.unileipzig.xman.comment.table.CommentEntryTableModel;
import de.unileipzig.xman.esf.ElectronicStudentFile;
import de.unileipzig.xman.esf.ElectronicStudentFileManager;
import de.unileipzig.xman.esf.form.ESFCommentCreateAndEditForm;
import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.exam.ExamDBManager;
import de.unileipzig.xman.protocol.archived.ArchivedProtocol;
import de.unileipzig.xman.protocol.archived.ArchivedProtocolManager;
import de.unileipzig.xman.protocol.archived.tables.ArchivedProtocolTableModel;
import de.unileipzig.xman.protocol.tables.ProtocolTableModel;

public class ESFEditController extends MainLayoutBasicController {

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(ElectronicStudentFile.class);

	public static final String ADD_COMMENT = "add.comment";
	public static final String REMOVE_COMMENT = "remove.comment";
	public static final String EDIT_COMMENT = "edit.comment";

	private ElectronicStudentFile esf;

	private VelocityContainer mainVC;

	private LayoutMain3ColsController columnLayoutCtr;

	// TabelModel and TableController
	private TableController protocolTableCtr;
	private ProtocolTableModel protocolTableMdl;

	private TableController commentTableCtr;
	private CommentEntryTableModel commentTableMdl;

	private TableController archiveTableCtr;
	private ArchivedProtocolTableModel archiveTableMdl;

	// CommentAddController
	private CloseableModalController addCommentCtr;
	private CloseableModalController editCommentCtr;

	// CommentEntry for editing
	private CommentEntry commentEntry;

	// forms
	private ESFCommentCreateAndEditForm addCommentForm;
	private ESFCommentCreateAndEditForm editCommentForm;

	public ESFEditController(UserRequest ureq, WindowControl wControl,
			ElectronicStudentFile esf) {
		super(ureq, wControl);

		this.esf = esf;
		setTranslator(Util.createPackageTranslator(ElectronicStudentFile.class, ureq.getLocale()));
		mainVC = new VelocityContainer("esfView", VELOCITY_ROOT + "/esf-edit.html", getTranslator(), this);
		
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, null, mainVC, "editESF");
		listenTo(columnLayoutCtr);// cleanup on dispose

		init(ureq, wControl);
		putInitialPanel(columnLayoutCtr.getInitialComponent());
	}

	private void init(UserRequest ureq, WindowControl wControl) {
		buildView(ureq, wControl);
	}

	private void buildView(UserRequest ureq, WindowControl wControl) {
		// refresh esf
		esf = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(esf.getIdentity());

		User user = esf.getIdentity().getUser();

		// add personal information in the esf-edit.html
		mainVC.contextPut("lastName", user.getProperty(UserConstants.LASTNAME, null));
		mainVC.contextPut("firstName", user.getProperty(UserConstants.FIRSTNAME, null));
		mainVC.contextPut("institutionalIdentifier", user.getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null));
		mainVC.contextPut("email", user.getProperty(UserConstants.INSTITUTIONALEMAIL, null));
		mainVC.contextPut("studyPath", user.getProperty(UserConstants.STUDYSUBJECT, null));

		createTableModels(ureq, wControl);
	}

	private void createTableModels(UserRequest ureq, WindowControl wControl) {
		createProtocolTableModel(ureq, wControl);
		createArchiveTable(ureq, wControl);
		createCommentTableModel(ureq, wControl);
	}


	private void createCommentTableModel(UserRequest ureq,
			WindowControl wControl) {

		TableGuiConfiguration commentTableConfig = new TableGuiConfiguration();
		commentTableConfig.setMultiSelect(true);
		commentTableConfig.setDownloadOffered(true);
		commentTableConfig.setPageingEnabled(true);
		commentTableConfig.setTableEmptyMessage(translate("ESFEditController.comment.emptyTableMessage"));
		commentTableConfig.setShowAllLinkEnabled(true);
		commentTableCtr = new TableController(commentTableConfig, ureq, wControl, getTranslator());
		commentTableCtr.setMultiSelect(true);
		commentTableCtr.addMultiSelectAction("ESFEditController.add", ADD_COMMENT);
		commentTableCtr.addMultiSelectAction("ESFEditController.remove", REMOVE_COMMENT);
		commentTableCtr.addMultiSelectAction("ESFEditController.edit", EDIT_COMMENT);
		// if esf is null, give an empty list to the model
		commentTableMdl = new CommentEntryTableModel(getLocale(), new ArrayList<CommentEntry>(esf.getComments()));
		commentTableMdl.setTable(commentTableCtr);
		commentTableCtr.setTableDataModel(commentTableMdl);
		commentTableCtr.setSortColumn(1, false);
		
		commentTableCtr.addControllerListener(this);
		
		this.mainVC.put("commentTable", commentTableCtr.getInitialComponent());
	}

	private void createProtocolTableModel(UserRequest ureq,
			WindowControl wControl) {

		// at the moment multiselection is not needed
		TableGuiConfiguration protocolTableConfig = new TableGuiConfiguration();
		// protocolTableConfig.setMultiSelect(true);
		protocolTableConfig.setDownloadOffered(true);
		protocolTableConfig.setPageingEnabled(true);
		protocolTableConfig.setTableEmptyMessage(translate("ESFEditController.protocol.emptyTableMessage"));
		protocolTableConfig.setShowAllLinkEnabled(true);
		protocolTableCtr = new TableController(protocolTableConfig, ureq, wControl, getTranslator());
		// protocolTableCtr.setMultiSelect(true);
		// protocolTableCtr.addMultiSelectAction("ESFEditController.addProtocol",
		// ADD_PROTOCOL);
		// if esf is null, give an empty list to the model
		protocolTableMdl = new ProtocolTableModel(esf.getProtocolList(), getLocale());
		protocolTableMdl.setTable(protocolTableCtr);
		protocolTableCtr.setTableDataModel(protocolTableMdl);
		protocolTableCtr.setSortColumn(1, false);

		protocolTableCtr.addControllerListener(this);
		
		this.mainVC
				.put("protocolTable", protocolTableCtr.getInitialComponent());
	}

	private void createArchiveTable(UserRequest ureq, WindowControl wControl) {
		String studentId = esf.getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null);
		List<ArchivedProtocol> protocols = ArchivedProtocolManager.getInstance().findAllByStudent(studentId);
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setPreferencesOffered(true, "esf.edit.table.archive");
		archiveTableCtr = new TableController(tableConfig, ureq, wControl, getTranslator());
		
		archiveTableMdl = new ArchivedProtocolTableModel(protocols, getLocale());
		archiveTableMdl.initTable(archiveTableCtr);
		archiveTableCtr.setTableDataModel(archiveTableMdl);
		
		archiveTableCtr.setSortColumn(1, false);
		
		listenTo(archiveTableCtr);
		mainVC.put("archiveTable", archiveTableCtr.getInitialComponent());
	}

	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	protected void doDispose() {
		removeAsListenerAndDispose(archiveTableCtr);
		removeAsListenerAndDispose(columnLayoutCtr);
	}

	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	protected void event(UserRequest ureq, Component source, Event event) {

	}

	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller,
	 *      org.olat.core.gui.control.Event)
	 */
	protected void event(UserRequest ureq, Controller source, Event event) {

		// exam office or author wants to add a comment to the choosen esf
		if (source == addCommentForm) {

			// add comment was canceled
			if (event == Form.EVNT_FORM_CANCELLED) {

				this.getWindowControl().pop();
			}

			// validation ok, comment should be saved
			if (event == Form.EVNT_VALIDATION_OK) {
				// reload esf from database
				esf = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(esf.getIdentity());

				// add new comment
				CommentManager.getInstance().createCommentInEsf(esf, addCommentForm.getComment(), ureq.getIdentity());
				
				// save changed esf
				ElectronicStudentFileManager.getInstance().updateElectronicStundentFile(esf);

				// deactivate the modal dialog
				getWindowControl().pop();

				// refresh view
				buildView(ureq, this.getWindowControl());
			}
		}

		if (source == editCommentForm) {

			// edit comment was cancelled
			if (event == Form.EVNT_FORM_CANCELLED) {

				this.getWindowControl().pop();
			}

			// edit comment successful
			if (event == Form.EVNT_VALIDATION_OK) {

				// close modal dialog
				this.getWindowControl().pop();

				// update comment
				CommentManager.getInstance().updateCommentInEsa(commentEntry, editCommentForm.getComment());

				// refresh view
				this.buildView(ureq, this.getWindowControl());
			}
		}

		// the table Controller
		if (source == protocolTableCtr) {

			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {

				TableEvent te = (TableEvent) event;
				String actionID = te.getActionId();

				if(actionID.equals(ProtocolTableModel.EXAM_LAUNCH)) {
					// open exam
					Exam exam = protocolTableMdl.getObject(te.getRowId()).getExam();

					String businessPath = "[RepositoryEntry:" + ExamDBManager.getInstance().findRepositoryEntryKey(exam) + "]";
					NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
				}
			}
		}

		if (source == commentTableCtr) {

			// 
			if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {

				TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;

				// 
				if (tmse.getAction().equals(REMOVE_COMMENT)) {

					List<CommentEntry> commentList = this.commentTableMdl
							.getObjects(tmse.getSelection());

					if (commentList.size() > 0) {

						ElectronicStudentFile file = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(esf.getIdentity());
						file.removeCommentEntries(commentList);
						ElectronicStudentFileManager.getInstance().updateElectronicStundentFile(file);

						// update the field
						this.esf = file;

						this.createCommentTableModel(ureq, this
								.getWindowControl());
					} else {
						showWarning("ESFEditController.pleaseChoseAtLeastOneComment");
					}
				}

				// somebody wants to edit a comment
				if (tmse.getAction().equals(EDIT_COMMENT)) {

					List<CommentEntry> commentList = this.commentTableMdl
							.getObjects(tmse.getSelection());

					// you could only edit one comment at a time
					if (commentList.size() == 1) {

						commentEntry = commentList.get(0);

						editCommentForm = new ESFCommentCreateAndEditForm(ureq,
								getWindowControl(),
								"ESFCommentCreateAndEditForm", getTranslator(),
								this.commentEntry.getComment());
						editCommentForm.addControllerListener(this);

						editCommentCtr = new CloseableModalController(
								getWindowControl(), translate("close"),
								editCommentForm.getInitialComponent());
						listenTo(editCommentCtr);
						editCommentCtr.activate();
					} else {
						showWarning("ESFEditController.pleaseChoseOnlyOneComment");
					}
				}

				if(tmse.getAction().equals(ADD_COMMENT)) {
					addCommentForm = new ESFCommentCreateAndEditForm(ureq, getWindowControl(), "ESFCommentCreateForm", getTranslator(), "");
					addCommentForm.addControllerListener(this);

					addCommentCtr = new CloseableModalController(getWindowControl(), translate("close"), addCommentForm.getInitialComponent());
					listenTo(addCommentCtr);
					addCommentCtr.activate();
				}
			}
		}
	}
}
