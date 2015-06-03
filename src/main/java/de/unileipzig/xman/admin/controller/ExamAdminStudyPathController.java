package de.unileipzig.xman.admin.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;

import de.unileipzig.xman.admin.ExamAdminSite;
import de.unileipzig.xman.admin.form.ChooseStudyPathXMLFile;
import de.unileipzig.xman.studyPath.StudyPath;
import de.unileipzig.xman.studyPath.StudyPathManager;
import de.unileipzig.xman.studyPath.table.StudyPathTableModel;

public class ExamAdminStudyPathController extends BasicController {

	private static final String VELOCITY_ROOT = Util
			.getPackageVelocityRoot(ExamAdminSite.class);

	private VelocityContainer vcMain;

	private TableController studyPathTableCtr;
	private StudyPathTableModel studyPathTableMdl;

	private ChooseStudyPathXMLFile chooseFileForm;
	private CloseableModalController dialogCtr;

	private Link importStudyPathButton;

	public ExamAdminStudyPathController(UserRequest ureq,
			WindowControl windowControl) {
		super(ureq, windowControl);

		setTranslator(Util.createPackageTranslator(StudyPath.class, ureq.getLocale()));

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

		importStudyPathButton = LinkFactory.createButton("ExamAdminStudyPathController.import", vcMain, this);
		importStudyPathButton.setIconLeftCSS("o_icon o_icon-fw o_icon_import");

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
		if(source == importStudyPathButton) {
			// somebody wants to upload new study paths
			removeAsListenerAndDispose(chooseFileForm);
			chooseFileForm = new ChooseStudyPathXMLFile(ureq, this.getWindowControl(), getTranslator());
			listenTo(chooseFileForm);

			dialogCtr = new CloseableModalController(this.getWindowControl(), "close", chooseFileForm.getInitialComponent());
			dialogCtr.activate();
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {

		if(source == chooseFileForm){
			if(event == Form.EVNT_VALIDATION_OK){
				// deactivate dialog
				dialogCtr.deactivate();
				
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
				        System.err.println("got to finish");
					} catch (JDOMException e) {
			        	showError("ExamAdminStudyPathCotroller.JDOOMException", e.getMessage());
			        	System.err.println(translate("ExamAdminStudyPathCotroller.JDOOMException", e.getMessage()));
						return;
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
			        
			        // create studypaths and reload table
					StudyPathManager.getInstance().createAllStudyPaths(studys);
					createTableModel(ureq, getWindowControl());
				} else {
					showError("ExamAdminStudyPathCotroller.NoFile");
				}
			}
		}
	}
}
