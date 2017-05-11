package de.unileipzig.xman.esf.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.util.ComponentUtil;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController; // import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing; // import org.olat.core.logging.UserActivityLogger;    // wird nicht aufgerufen
import org.olat.core.util.Util;
import org.olat.core.util.event.GenericEventListener;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.resource.OLATResourceManager;
import org.olat.user.UserManager;
import org.olat.user.UserPropertiesConfig;

import de.unileipzig.xman.admin.ExamAdminSite;
import de.unileipzig.xman.comment.CommentEntry;
import de.unileipzig.xman.comment.table.CommentEntryTableModel;
import de.unileipzig.xman.esf.DuplicateObjectException;
import de.unileipzig.xman.esf.ElectronicStudentFile;
import de.unileipzig.xman.esf.ElectronicStudentFileManager;
import de.unileipzig.xman.esf.form.ESFCreateForm;
import de.unileipzig.xman.esf.table.ESFTableModel;
import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.exam.ExamDBManager;
import de.unileipzig.xman.exam.controllers.ExamMainController;
import de.unileipzig.xman.protocol.Protocol;
import de.unileipzig.xman.protocol.ProtocolManager;
import de.unileipzig.xman.protocol.archived.ArchivedProtocol;
import de.unileipzig.xman.protocol.archived.ArchivedProtocolManager;
import de.unileipzig.xman.protocol.archived.tables.ArchivedProtocolTableModel;
import de.unileipzig.xman.protocol.tables.ProtocolTableModel;
import de.unileipzig.xman.studyPath.StudyPath;

/**
 * 
 * Description:<br>
 * TODO: gerb Class Description for ESFLaunchController
 * 
 * <P>
 * Initial Date: 22.05.2008 <br>
 * 
 * @author gerb
 */
public class ESFLaunchController extends BasicController {

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(ElectronicStudentFile.class);

	public static final String CHANGE_ESF = "action.changeESF";
	public static final String CREATE_ESF = "action.createESF";

	private ElectronicStudentFile esf;

	private VelocityContainer mainVC;

	// TabelModel and TableController
	private TableController protoTableCtr;
	private ProtocolTableModel protoTableMdl;

	private TableController commentTableCtr;
	private CommentEntryTableModel commentTableMdl;
	
	private TableController archiveTableCtr;
	private ArchivedProtocolTableModel archiveTableMdl;

	// For creating a new nonValidated ESF
	private ESFCreateController esfController;
	private CloseableModalController cmc;

	// Links for create an edit esf
	private Link linkCreateEsf;
	private Link linkChangeEsf;
	
	// user
	private User user;
	
	/**
	 * 
	 * @param ureq
	 * @param control
	 */
	public ESFLaunchController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		setTranslator(Util.createPackageTranslator(ElectronicStudentFile.class, ureq.getLocale()));

		mainVC = new VelocityContainer("esfView", VELOCITY_ROOT + "/esf-launch.html", getTranslator(), this);
		
		this.init(ureq, wControl);
		
		this.putInitialPanel(mainVC);
	}

	/**
	 * Everything in here, could also be called in the constructor. But for
	 * refreshing the view, it gets outsourced to the init method.
	 * 
	 * @param ureq
	 *            - The UserRequest
	 * @param wControl
	 *            - The WindowControl
	 */
	private void init(UserRequest ureq, WindowControl wControl) {
		// to get the esf for the user
		esf = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(ureq.getIdentity());
		// load by id so we get a fresh version from db
		user = UserManager.getInstance().loadUserByKey(ureq.getIdentity().getUser().getKey());
		
		// to build the for the different roles
		buildView(ureq, wControl);
	}

	/**
	 * Builds the view according to the roles of the user. TODO what happens
	 * exactly
	 * 
	 * @param ureq
	 * @param wControl
	 */
	private void buildView(UserRequest ureq, WindowControl wControl) {
		if (esf == null) {
			// there is no esf for this student
			linkCreateEsf = LinkFactory.createButton("ESFLaunchController.tool.create", mainVC, this);

			mainVC.contextPut("esf_available", false);
			mainVC.contextPut("error", translate("ESFLaunchController.noESF"));
		} else {
			linkChangeEsf = LinkFactory.createButton("ESFLaunchController.tool.change", mainVC, this);

			mainVC.contextPut("esf_available", true);

			// only build tables if an esf exists
			buildTables(ureq, wControl);
		}
	}

	/**
	 * 
	 * @param ureq
	 */
	private void buildTables(UserRequest ureq, WindowControl wControl) {
		// add personal information in the esf-launch.html
		this.mainVC.contextPut("lastName", user.getProperty(UserConstants.LASTNAME, null));
		this.mainVC.contextPut("firstName", user.getProperty(UserConstants.FIRSTNAME, null));
		this.mainVC.contextPut("institutionalIdentifier", user.getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null));
		this.mainVC.contextPut("email", user.getProperty(UserConstants.INSTITUTIONALEMAIL, null));
		this.mainVC.contextPut("studyPath", user.getProperty(UserConstants.STUDYSUBJECT, null));

		this.createTableModels(ureq, wControl);
	}

	/**
	 * creates all table models
	 * 
	 * @param ureq
	 * @param wControl
	 */
	private void createTableModels(UserRequest ureq, WindowControl wControl) {
		createProtocolTableModel(ureq, wControl);
		createArchiveTable(ureq, wControl);
		createCommentTableModel(ureq, wControl);
	}


	/**
	 * creates the comment table model
	 * 
	 * @param ureq
	 * @param wControl
	 */
	private void createCommentTableModel(UserRequest ureq,
			WindowControl wControl) {

		TableGuiConfiguration commentTableConfig = new TableGuiConfiguration();
		commentTableConfig.setTableEmptyMessage(translate("ESFEditController.comment.emptyTableMessage"));
		commentTableConfig.setResultsPerPage(10);
		commentTableCtr = new TableController(commentTableConfig, ureq,
				wControl, getTranslator());
		commentTableMdl = new CommentEntryTableModel(getLocale(), new ArrayList<CommentEntry>(esf.getComments()));
		commentTableMdl.setTable(commentTableCtr);
		commentTableCtr.setTableDataModel(commentTableMdl);
		commentTableCtr.setSortColumn(0, false);

		this.mainVC.put("commentTable", commentTableCtr.getInitialComponent());
	}

	/**
	 * Creates the protocol table model
	 * 
	 * @param ureq
	 * @param wControl
	 */
	private void createProtocolTableModel(UserRequest ureq, WindowControl wControl) {
		// filter out protocols of archived (closed) exams, because they
		// are already shown in the ArchivedProtocol table
		List<Protocol> protocols = new ArrayList<Protocol>();
		for(Protocol protocol : esf.getProtocolList()) {
			if(!ExamDBManager.getInstance().isClosed(protocol.getExam())) {
				protocols.add(protocol);
			}
		}

		TableGuiConfiguration protoTableConfig = new TableGuiConfiguration();
		protoTableConfig.setTableEmptyMessage(translate("ESFLaunchController.protocol.emptyTableMessage"));
		protoTableCtr = new TableController(protoTableConfig, ureq, wControl, getTranslator());
		protoTableMdl = new ProtocolTableModel(protocols, getLocale());
		protoTableMdl.setTable(protoTableCtr);
		protoTableCtr.setTableDataModel(protoTableMdl);
		protoTableCtr.setSortColumn(1, false);

		// NEU
		protoTableCtr.addControllerListener(this);

		this.mainVC.put("protoTable", protoTableCtr.getInitialComponent());
	}
	
	private void createArchiveTable(UserRequest ureq, WindowControl wControl) {
		String studentId = user.getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null);
		List<ArchivedProtocol> protocols = ArchivedProtocolManager.getInstance().findAllByStudent(studentId);
		
		if(protocols.size() == 0) {
			if(archiveTableCtr != null) {
				mainVC.remove(archiveTableCtr.getInitialComponent());
				removeAsListenerAndDispose(archiveTableCtr);
				archiveTableCtr = null;
			}
			return;
		}

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setPreferencesOffered(true, "esf.launch.table.archive");
		tableConfig.setResultsPerPage(15);
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
	}

	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == linkChangeEsf) {
			esfController = new ESFCreateController(ureq, getWindowControl(), getTranslator(), user, CHANGE_ESF);
			esfController.addControllerListener(this);

			cmc = new CloseableModalController(getWindowControl(), translate("close"), esfController.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
		} else if(source == linkCreateEsf) {
			esfController = new ESFCreateController(ureq, getWindowControl(), getTranslator(), user, CREATE_ESF);
			esfController.addControllerListener(this);

			cmc = new CloseableModalController(getWindowControl(), translate("close"), esfController.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
		}
	}

	public void event(UserRequest ureq, Controller ctr, Event event) {
		// student pushed "save" or "cancel" on the modal dialog
		if (ctr == this.esfController) {

			// cancelButton was pressed
			if (event == Event.CANCELLED_EVENT) {
				this.getWindowControl().pop();
			}

			// saveButton was pressed
			if (event.getCommand().equals(ESFCreateController.CHANGE_EVENT)
					|| event.getCommand().equals(
							ESFCreateController.VALIDATE_EVENT)) {
				// a new ESF was created by the ESFCreateController

				this.getWindowControl().pop();
				this.init(ureq, this.getWindowControl());
			}

		}

		// the table Controller
		if (ctr == protoTableCtr) {

			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {

				TableEvent te = (TableEvent) event;
				String actionID = te.getActionId();

				// somebody wants to open an exam
				if (actionID.equals(ProtocolTableModel.EXAM_LAUNCH)) {

					Exam exam = protoTableMdl.getObject(te.getRowId()).getExam();
					// reload exam
					exam = ExamDBManager.getInstance().findExamByID(exam.getKey());
					OLATResourceable ores = OLATResourceManager.getInstance().findResourceable(exam.getResourceableId(), Exam.ORES_TYPE_NAME);

					DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDTabs();
					DTab dt = dts.getDTab(ores);
					if (dt == null) {
						// does not yet exist -> create and add
						ExamMainController examMain = new ExamMainController(ureq, getWindowControl(), exam, ExamMainController.View.STUDENT);
						dt = dts.createDTab(ores, null, examMain, exam.getName());
						if(dt == null) return;
						dts.addDTab(ureq, dt);
					}
					dts.activate(ureq, dt, null);
				}
			}
		}
	}

	public void event(Event event) {
		// TODO Auto-generated method stub

	}
}
