package de.unileipzig.xman.esf.controller;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController; //#### Änderung Name
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.gui.control.generic.tool.ToolFactory;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResourceManager;
import org.olat.user.HomePageConfigManagerImpl;
import org.olat.user.UserInfoMainController;

import de.unileipzig.xman.comment.CommentEntry;
import de.unileipzig.xman.comment.CommentManager;
import de.unileipzig.xman.comment.table.CommentEntryTableModel;
import de.unileipzig.xman.esf.ElectronicStudentFile;
import de.unileipzig.xman.esf.ElectronicStudentFileManager;
import de.unileipzig.xman.esf.form.ESFCommentCreateAndEditForm;
import de.unileipzig.xman.esf.table.ESFTableModel;
import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.exam.controllers.ExamMainController;
import de.unileipzig.xman.protocol.Protocol;
import de.unileipzig.xman.protocol.tables.ProtocolTableModel;
import de.unileipzig.xman.studyPath.StudyPath;

public class ESFEditController extends MainLayoutBasicController {

	private static final String PACKAGE = Util
			.getPackageName(ElectronicStudentFile.class);
	private static final String VELOCITY_ROOT = Util
			.getPackageVelocityRoot(ElectronicStudentFile.class);

	public static final String ADD_COMMENT = "add.comment";
	public static final String ADD_PROTOCOL = "add.protocol";

	public static final String REMOVE_COMMENT = "remove.comment";
	public static final String EDIT_COMMENT = "edit.comment";

	private ElectronicStudentFile esf;

	private Translator translator;
	private VelocityContainer mainVC;
	private ToolController toolCtr;

	private LayoutMain3ColsController columnLayoutCtr;

	// TabelModel and TableController
	private TableController protocolTableCtr;
	private ProtocolTableModel protocolTableMdl;

	private TableController commentTableCtr;
	private CommentEntryTableModel commentTableMdl;

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
		this.translator = new PackageTranslator(PACKAGE, ureq.getLocale());
		this.mainVC = new VelocityContainer("esfView", VELOCITY_ROOT
				+ "/esf-edit.html", translator, this);
		this.toolCtr = ToolFactory.createToolController(wControl);
		this.toolCtr.addControllerListener(this);

		columnLayoutCtr = new LayoutMain3ColsController(ureq,
				getWindowControl(), null, toolCtr.getInitialComponent(),
				mainVC, "editESF");
		listenTo(columnLayoutCtr);// cleanup on dispose
		// add background image to home site
		columnLayoutCtr.addCssClassToMain("o_home");

		init(ureq, wControl);
		this.putInitialPanel(columnLayoutCtr.getInitialComponent());
	}

	private void init(UserRequest ureq, WindowControl wControl) {

		this.toolCtr.addHeader(this.translator
				.translate("ESFEditController.toolheader"));
		// useless
		// this.toolCtr.addLink(ADD_PROTOCOL,
		// this.translator.translate("ESFEditController.addProtocol"));
		this.toolCtr.addLink(ADD_COMMENT, this.translator
				.translate("ESFEditController.addComment"));
	
		this.buildView(ureq, wControl);
	}

	private void buildView(UserRequest ureq, WindowControl wControl) {

		// refresh esf
		this.esf = ElectronicStudentFileManager.getInstance()
				.retrieveESFByIdentity(esf.getIdentity());

		User user = esf.getIdentity().getUser();

		// add personal information in the esf-edit.html
		this.mainVC.contextPut("lastName", user.getProperty(
				UserConstants.LASTNAME, null));
		this.mainVC.contextPut("firstName", user.getProperty(
				UserConstants.FIRSTNAME, null));
		this.mainVC.contextPut("institutionalIdentifier", user.getProperty(
				UserConstants.INSTITUTIONALUSERIDENTIFIER, null));
		this.mainVC.contextPut("email", user.getProperty(
				UserConstants.INSTITUTIONALEMAIL, null));

		// create a translator with the studyPath translations
		Translator translator = Util.createPackageTranslator(StudyPath.class,
				ureq.getLocale());
		this.mainVC.contextPut("studyPath", user.getProperty(UserConstants.STUDYSUBJECT, null));

		this.createTableModels(ureq, wControl);

	}

	private void createTableModels(UserRequest ureq, WindowControl wControl) {

		this.createProtocolTableModel(ureq, wControl);
		this.createCommentTableModel(ureq, wControl);
	
	}


	private void createCommentTableModel(UserRequest ureq,
			WindowControl wControl) {

		TableGuiConfiguration commentTableConfig = new TableGuiConfiguration();
		commentTableConfig.setMultiSelect(true);
		commentTableConfig.setColumnMovingOffered(true);
		commentTableConfig.setDownloadOffered(true);
		commentTableConfig.setPageingEnabled(true);
		commentTableConfig.setTableEmptyMessage(this.translator
				.translate("ESFEditController.comment.emptyTableMessage")); // TODO
		commentTableConfig.setShowAllLinkEnabled(true);
		commentTableCtr = new TableController(commentTableConfig, ureq,
				wControl, translator);
		commentTableCtr.setMultiSelect(true);
		commentTableCtr.addMultiSelectAction("ESFEditController.remove",
				REMOVE_COMMENT);
		commentTableCtr.addMultiSelectAction("ESFEditController.edit",
				EDIT_COMMENT);
		// if esf is null, give an empty list to the model
		commentTableMdl = new CommentEntryTableModel(translator.getLocale(), new ArrayList<CommentEntry>(esf.getComments()));
		commentTableMdl.setTable(commentTableCtr);
		commentTableCtr.setTableDataModel(commentTableMdl);
		commentTableCtr.setSortColumn(0, true);
		
		commentTableCtr.addControllerListener(this);
		
		this.mainVC.put("commentTable", commentTableCtr.getInitialComponent());
	}

	private void createProtocolTableModel(UserRequest ureq,
			WindowControl wControl) {

		// at the moment multiselection is not needed
		TableGuiConfiguration protocolTableConfig = new TableGuiConfiguration();
		// protocolTableConfig.setMultiSelect(true);
		protocolTableConfig.setColumnMovingOffered(true);
		protocolTableConfig.setDownloadOffered(true);
		protocolTableConfig.setPageingEnabled(true);
		protocolTableConfig.setTableEmptyMessage(this.translator
				.translate("ESFEditController.protocol.emptyTableMessage")); // TODO
		protocolTableConfig.setShowAllLinkEnabled(true);
		protocolTableCtr = new TableController(protocolTableConfig, ureq,
				wControl, translator);
		// protocolTableCtr.setMultiSelect(true);
		// protocolTableCtr.addMultiSelectAction("ESFEditController.addProtocol",
		// ADD_PROTOCOL);
		// if esf is null, give an empty list to the model
		protocolTableMdl = new ProtocolTableModel(esf.getProtocolList(), translator.getLocale());
		protocolTableMdl.setTable(protocolTableCtr);
		protocolTableCtr.setTableDataModel(protocolTableMdl);
		protocolTableCtr.setSortColumn(1, false);

		protocolTableCtr.addControllerListener(this);
		
		this.mainVC
				.put("protocolTable", protocolTableCtr.getInitialComponent());
	}

	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	protected void doDispose() {

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



		// toolController was pressed
		if (source == toolCtr) {

			// somebody wants to add a comment
			if (event.getCommand().equals(ADD_COMMENT)) {
				
				addCommentForm = new ESFCommentCreateAndEditForm(ureq,
						getWindowControl(), "ESFCommentCreateForm",
						this.translator, "");
				addCommentForm.addControllerListener(this);

				addCommentCtr = new CloseableModalController(
						getWindowControl(), translate("close"), addCommentForm
								.getInitialComponent());
				listenTo(addCommentCtr);
				addCommentCtr.activate();
			}

			// somebody wants to add a protocol
			if (event.getCommand().equals(ADD_PROTOCOL)) {

				this
						.getWindowControl()
						.setInfo(
								translator
										.translate("ESFEditController.atTheMomentNotAvailable"));
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
					OLATResourceable ores = OLATResourceManager.getInstance().findResourceable(exam.getResourceableId(), Exam.ORES_TYPE_NAME);

					// add the esf in a dtab
					DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDTabs();
					DTab dt = dts.getDTab(ores);
					if (dt == null) {
						// does not yet exist -> create and add
						dt = dts.createDTab(ores, exam.getName());
						if(dt == null) return;
						
						ExamMainController examMain = new ExamMainController(ureq, getWindowControl(), exam, ExamMainController.View.LECTURER);
						dt.setController(examMain);
						
						dts.addDTab(ureq, dt);
					}
					dts.activate(ureq, dt, null);

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

						this
								.getWindowControl()
								.setWarning(
										translator
												.translate("ESFEditController.pleaseChoseAtLeastOneComment"));
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
								"ESFCommentCreateAndEditForm", this.translator,
								this.commentEntry.getComment());
						editCommentForm.addControllerListener(this);

						editCommentCtr = new CloseableModalController(
								getWindowControl(), translate("close"),
								editCommentForm.getInitialComponent());
						listenTo(editCommentCtr);
						editCommentCtr.activate();
					} else {

						this
								.getWindowControl()
								.setWarning(
										translator
												.translate("ESFEditController.pleaseChoseOnlyOneComment"));
					}
				}
			}
		}


	}

	public void setEsf(ElectronicStudentFile esf) {
		this.esf = esf;
	}

}
