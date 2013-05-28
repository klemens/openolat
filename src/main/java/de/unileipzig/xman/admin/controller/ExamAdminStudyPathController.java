package de.unileipzig.xman.admin.controller;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.olat.catalog.CatalogEntry;
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
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.gui.control.generic.tool.ToolFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;

import de.unileipzig.xman.admin.ExamAdminSite;
import de.unileipzig.xman.esf.DuplicateObjectException;
import de.unileipzig.xman.studyPath.StudyPath;
import de.unileipzig.xman.studyPath.StudyPathManager;
import de.unileipzig.xman.studyPath.form.StudyPathCreateAndEditForm;
import de.unileipzig.xman.studyPath.table.StudyPathTableModel;

public class ExamAdminStudyPathController extends BasicController {

	private static final String VELOCITY_ROOT = Util
			.getPackageVelocityRoot(ExamAdminSite.class);
	private static final String ACTION_ADD = "add.StudyPath";
	private static final String ACTION_DELETE = "delete.StudyPath";
	private static final String ACTION_EDIT_STUDY_PATH = "edit.studyPath";

	private Translator translator;
	private VelocityContainer vcMain;
	private ToolController toolCtr;

	private TableController studyPathTableCtr;
	private StudyPathTableModel studyPathTableMdl;

	private CloseableModalController dialogCtr;
	private StudyPathCreateAndEditForm studyPathCreateForm;
	private StudyPathCreateAndEditForm studyPathEditForm;

	private StudyPath studyPath;

	public ExamAdminStudyPathController(UserRequest ureq,
			WindowControl windowControl) {
		super(ureq, windowControl);

		translator = Util.createPackageTranslator(StudyPath.class, ureq
				.getLocale());

		toolCtr = ToolFactory.createToolController(windowControl);
		toolCtr.addControllerListener(this);
		toolCtr.addHeader(translator
				.translate("ExamAdminStudyPathController.tool.header"));
		toolCtr.addLink(ACTION_ADD, translator
				.translate("ExamAdminStudyPathController.tool.add"));

		// it's not possible to use this right now
		// what would happen when a studypath gets deleted
		// the user's property has to be changed back to default
		// at the moment there is only an edit button which simply changes the
		// translation of the studyPath
		// toolCtr.addLink(ACTION_DELETE,
		// translator.translate("ExamAdminStudyPathController.tool.delete"));

		vcMain = new VelocityContainer("vcAttr", VELOCITY_ROOT
				+ "/studyPath.html", translator, this);

		this.createTableModel(ureq, windowControl);

		this.putInitialPanel(vcMain);
	}

	private void createTableModel(UserRequest ureq, WindowControl windowControl) {

		TableGuiConfiguration catalogEntryTableConfig = new TableGuiConfiguration();
		catalogEntryTableConfig
				.setTableEmptyMessage(this.translator
						.translate("ExamAdminStudyPathController.studyPathEntryTable.emptyTableMessage"));
		studyPathTableCtr = new TableController(catalogEntryTableConfig, ureq,
				windowControl, translator);
		// if no catalogEntry is choosen, find all children from root-entry
		studyPathTableMdl = new StudyPathTableModel(ureq.getLocale(),
				StudyPathManager.getInstance().findAllStudyPaths(), translator);
		studyPathTableMdl.setTable(studyPathTableCtr);
		studyPathTableCtr.setTableDataModel(studyPathTableMdl);
		studyPathTableCtr.setSortColumn(0, true);
		// not needed at the moment, maybe if the studypath gets a little bit
		// more extended
		studyPathTableCtr.setMultiSelect(false);
		// studyPathTableCtr.addMultiSelectAction("ExamAdminStudyPathController.studyPathTable.edit",
		// ACTION_EDIT_STUDY_PATH);

		this.vcMain.put("studyPathEntryTable", studyPathTableCtr
				.getInitialComponent());
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// Empty due to the forms bringing their own controllers now.
		System.err.println(source);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {

		if (source == studyPathCreateForm) {

			if (event == Form.EVNT_VALIDATION_OK) {
				
				this.getWindowControl().pop();
				
				if (this.checkTakenNames(ureq, studyPathCreateForm)) {

					StudyPath sp = StudyPathManager.getInstance()
							.createStudyPath();
					sp.setName(studyPathCreateForm.getName());

					try {
						StudyPathManager.getInstance().saveStudyPath(sp);
					} catch (DuplicateObjectException e) {

						this
								.getWindowControl()
								.setWarning(
										translator
												.translate(
														"ExamAdminStudyPathController.duplicateObject",
														new String[] { studyPathCreateForm
																.getName() }));
					}

					this.createTableModel(ureq, this.getWindowControl());
				} else
					this
							.getWindowControl()
							.setInfo(
									translator
											.translate(
													"ExamAdminStudyPathController.duplicateObject",
													new String[] { studyPathCreateForm
															.getName() }));
			}
		}

		if (source == studyPathEditForm) {

			if (event == Form.EVNT_VALIDATION_OK) {
				StudyPathManager.getInstance().updateStudyPaths();
				this.getWindowControl().pop();

				if (this.checkTakenNames(ureq, studyPathEditForm)) {
					StudyPath oldPath = StudyPathManager.getInstance().findStudyPath(studyPath.getName());
					String newName = studyPathEditForm.getName();
					String oldName = studyPath.getName();

					this.createTableModel(ureq, this.getWindowControl());
				} else
					this
							.getWindowControl()
							.setInfo(
									translator
											.translate(
													"ExamAdminStudyPathController.duplicateObject",
													new String[] { studyPathEditForm
															.getName() }));
			}
		}

		if (source == toolCtr) {

			// somebody wants to add a new study path
			if (event.getCommand().equals(ACTION_ADD)) {
				StudyPathManager.getInstance().updateStudyPaths();
		/*		studyPathCreateForm = new StudyPathCreateAndEditForm(ureq,
						getWindowControl(), "studyPathForm", translator, null);
				studyPathCreateForm.addControllerListener(this);

				// make it a modal dialog
				dialogCtr = new CloseableModalController(getWindowControl(),
						translator.translate("close"), studyPathCreateForm
								.getInitialComponent());
				dialogCtr.activate();
		*/	}
		}

		if (source == studyPathTableCtr) {

			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {

				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				int rowid = te.getRowId();
				studyPath = studyPathTableMdl.getEntryAt(rowid);

				if (actionid.equals(StudyPathTableModel.ENTRY_SELECTED)) {

					studyPathEditForm = new StudyPathCreateAndEditForm(ureq,
							getWindowControl(), "studyPathForm", translator,
							studyPath);
					studyPathEditForm.addControllerListener(this);

					// make it a modal dialog
					dialogCtr = new CloseableModalController(
							getWindowControl(), translator.translate("close"),
							studyPathEditForm.getInitialComponent());
					dialogCtr.activate();
				}
			}
		}
	}

	/**
	 * @return the toolController of this Controller
	 */
	public ToolController getToolController() {

		return this.toolCtr;
	}

	/**
	 * 
	 * @param ureq
	 * @param form
	 * @return
	 */
	private boolean checkTakenNames(UserRequest ureq, StudyPathCreateAndEditForm form) {

		String[] takenNames = StudyPathManager.getInstance().getAllStudyPathsAsString();
		for (int i = 0; i < takenNames.length; i++) {
			if (takenNames[i].equals(form.getName())) {
				return false;
			}
		}
		return true;
	}
}
