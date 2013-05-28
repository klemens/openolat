package de.unileipzig.xman.admin.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
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
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.gui.control.generic.tool.ToolFactory;
import org.olat.core.util.Util;

import de.unileipzig.xman.admin.ExamAdminSite;
import de.unileipzig.xman.admin.form.ChooseStudyPathXMLFile;
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

	private VelocityContainer vcMain;
	private ToolController toolCtr;

	private TableController studyPathTableCtr;
	private StudyPathTableModel studyPathTableMdl;

	private ChooseStudyPathXMLFile chooseFileForm;
	private CloseableModalController dialogCtr;

	private StudyPath studyPath;

	public ExamAdminStudyPathController(UserRequest ureq,
			WindowControl windowControl) {
		super(ureq, windowControl);

		setTranslator(Util.createPackageTranslator(StudyPath.class, ureq.getLocale()));

		toolCtr = ToolFactory.createToolController(windowControl);
		toolCtr.addControllerListener(this);
		toolCtr.addHeader(translate("ExamAdminStudyPathController.tool.header"));
		toolCtr.addLink(ACTION_ADD, translate("ExamAdminStudyPathController.tool.add"));

		// it's not possible to use this right now
		// what would happen when a studypath gets deleted
		// the user's property has to be changed back to default
		// at the moment there is only an edit button which simply changes the
		// translation of the studyPath
		// toolCtr.addLink(ACTION_DELETE,
		// translator.translate("ExamAdminStudyPathController.tool.delete"));

		vcMain = new VelocityContainer("vcAttr", VELOCITY_ROOT
				+ "/studyPath.html", getTranslator(), this);

		this.createTableModel(ureq, windowControl);

		this.putInitialPanel(vcMain);
	}

	private void createTableModel(UserRequest ureq, WindowControl windowControl) {

		TableGuiConfiguration catalogEntryTableConfig = new TableGuiConfiguration();
		catalogEntryTableConfig
				.setTableEmptyMessage(translate("ExamAdminStudyPathController.studyPathEntryTable.emptyTableMessage"));
		studyPathTableCtr = new TableController(catalogEntryTableConfig, ureq,
				windowControl, getTranslator());
		// if no catalogEntry is choosen, find all children from root-entry
		studyPathTableMdl = new StudyPathTableModel(ureq.getLocale(), StudyPathManager.getInstance().findAllStudyPaths(), getTranslator());
		studyPathTableMdl.setTable(studyPathTableCtr);
		studyPathTableCtr.setTableDataModel(studyPathTableMdl);
		studyPathTableCtr.setSortColumn(0, true);

		this.vcMain.put("studyPathEntryTable", studyPathTableCtr.getInitialComponent());
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

		if(source == chooseFileForm){
			if(event == Form.EVNT_VALIDATION_OK){
				if(chooseFileForm.getFile() != null){ 
					ArrayList<StudyPath> studys = new ArrayList<StudyPath>();
			        File f = chooseFileForm.getFile();
			        SAXBuilder builder = new SAXBuilder();
			        try {
			        	Document doc = builder.build(f);
				        Element root = doc.getRootElement();
				        
				        Iterator it = root.getChildren("course").iterator();
				        while(it.hasNext()) {
				        	Element course = (Element) it.next();
				        	StudyPath studyPath = StudyPathManager.getInstance().createStudyPath();
				        	studyPath.setName(course.getText());
				        	studys.add(studyPath);
				        }
					} catch (JDOMException e) {
			        	getWindowControl().setError(translate("ExamAdminStudyPathCotroller.JDOOMException", new String[] { e.getMessage() }));
						return;
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					StudyPathManager.getInstance().createAllStudyPaths(studys);
					dialogCtr.deactivate();
				} else {
					showError("ExamAdminStudyPathCotroller.NoFile");
				}
			}
		}
		
		if (source == toolCtr) {
			// somebody wants to upload new study paths
			if (event.getCommand().equals(ACTION_ADD)) {
				removeAsListenerAndDispose(chooseFileForm);
				chooseFileForm = new ChooseStudyPathXMLFile(ureq, this.getWindowControl(), getTranslator());
				listenTo(chooseFileForm);
				
				dialogCtr = new CloseableModalController(this.getWindowControl(), "close", chooseFileForm.getInitialComponent());
				dialogCtr.activate();
			}
		}
	}

	/**
	 * @return the toolController of this Controller
	 */
	public ToolController getToolController() {

		return this.toolCtr;
	}
}
