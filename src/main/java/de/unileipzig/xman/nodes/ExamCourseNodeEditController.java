package de.unileipzig.xman.nodes;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.ReferencableEntriesSearchController;

import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.exam.ExamDBManager;
import de.unileipzig.xman.exam.controllers.ExamLaunchController;

/**
 * is called in a course-editor, if adding an Exam as CourseNode
 * it controls the tabpane
 * @author blutz
 *
 */
public class ExamCourseNodeEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {
	
	//titles of tabs
	public static final String PANE_TAB_ACCESSIBILITY = "examEditController.pane.tab.accessibility";
	public static final String PANE_TAB_EXAMCONFIG = "examEditController.pane.tab.examconfig";
	
	private static final String[] paneKeys = { PANE_TAB_EXAMCONFIG, PANE_TAB_ACCESSIBILITY };
	
	
	private static final String PACKAGE = Util.getPackageName(ExamCourseNodeEditController.class);
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(PACKAGE);
	
	private static final String CHOSEN_ENTRY = "chosen_entry";
	private static final String CONFIG_KEY_REPOSITORY_SOFTKEY = "reporef";

	private ModuleConfiguration moduleConfiguration;
	private Translator translator;
	
	private ExamCourseNode examCourseNode;
	
	//controller to tabs
	private ConditionEditController accessCondContr;
	
	
	private TabbedPane tabs;
	private Panel main;
	private VelocityContainer content;
	private ReferencableEntriesSearchController searchController;
	private CloseableModalController cmcExamCtr;
	private CloseableModalController cmcSearchController;
	private Link previewButton;
	private Link chooseButton;
	private Link changeButton;
	private VelocityContainer editAccessVc;
	private Controller examLaunchCtr;

	/**
	 * Constructor for wiki page editor controller
	 * 
	 * @param config The node module configuration
	 * @param ureq The user request
	 * @param wikiCourseNode The current wiki page course node
	 * @param course
	 */
	public ExamCourseNodeEditController(ModuleConfiguration config, UserRequest ureq, WindowControl wControl, ExamCourseNode examCourseNode,
			ICourse course, UserCourseEnvironment euce) {
		super(ureq, wControl);
		
		this.moduleConfiguration = config;
		this.examCourseNode = examCourseNode;
		
		translator = new PackageTranslator(PACKAGE, ureq.getLocale());
		main = new Panel("exammain");

		
		//tab to add an exam
		content = new VelocityContainer("exameditcontent", VELOCITY_ROOT + "/edit.html", translator, this);
		
		previewButton = LinkFactory.createButtonSmall("examEditController.command.preview", content, this);
		chooseButton = LinkFactory.createButtonSmall("examEditController.command.choose", content, this);
		changeButton = LinkFactory.createButtonSmall("examEditController.command.change", content, this);
				
		if (config.get(CONFIG_KEY_REPOSITORY_SOFTKEY) != null) {
			// fetch repository entry to display the repository entry title of the
			// chosen cp
			RepositoryEntry re = getExamRepoReference(config, false);
			if (re == null) { // we cannot display the entrie's name, because the
				// repository entry had been deleted between the time
				// when it was chosen here, and now
				getWindowControl().setError(translator.translate("examEditController.error.repoentrymissing"));
				content.contextPut("showPreviewButton", Boolean.FALSE);
				content.contextPut(CHOSEN_ENTRY, translator.translate("examEditController.no_entry_chosen"));
			} else {
				content.contextPut("showPreviewButton", Boolean.TRUE);
				content.contextPut(CHOSEN_ENTRY, re.getDisplayname());
			}
		} else {
			// no valid config yet
			content.contextPut("showPreviewButton", Boolean.FALSE);
			content.contextPut(CHOSEN_ENTRY, translator.translate("examEditController.no_entry_chosen"));
		}
		
		
		
		
		//tab to edit accesibility
		editAccessVc = new VelocityContainer("olatmodaccessedit", VELOCITY_ROOT + "/edit_access.html", translator, this);
		
		CourseGroupManager groupMgr = course.getCourseEnvironment().getCourseGroupManager();
		CourseEditorTreeModel editorModel = course.getEditorTreeModel();
		
		// Accessibility precondition
		Condition accessCondition = examCourseNode.getPreConditionAccess();
		accessCondContr = new ConditionEditController(ureq, wControl, groupMgr, accessCondition, "accessConditionForm", 
				AssessmentHelper.getAssessableNodes(editorModel, examCourseNode), euce);
		accessCondContr.addControllerListener(this);
		editAccessVc.put("readerCondition", accessCondContr.getInitialComponent());
		
		main.setContent(content);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == previewButton) {
			// Preview as modal dialogue only if the config is valid
			RepositoryEntry re = getExamRepoReference(moduleConfiguration, false);
			if (re == null) { // we cannot preview it, because the repository entry
				// had been deleted between the time when it was
				// chosen here, and now
				getWindowControl().setError(translator.translate("examEditController.error.repoentrymissing"));
			} else {
				
				Exam exam = ExamDBManager.getInstance().findExamByID(re.getOlatResource().getResourceableId());
				examLaunchCtr = new ExamLaunchController(ureq, this.getWindowControl(), exam, false, true);
				
				cmcExamCtr = new CloseableModalController(getWindowControl(), translator.translate("examEditController.command.close"), examLaunchCtr.getInitialComponent());
				cmcExamCtr.addControllerListener(this);
				cmcExamCtr.insertHeaderCss();
				cmcExamCtr.activate();
			}
		} else if (source == chooseButton || source == changeButton) {
			searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq,
					Exam.ORES_TYPE_NAME, translator.translate("examEditController.command.choose"));
			searchController.addControllerListener(this);
			cmcSearchController = new CloseableModalController(getWindowControl(), translator.translate("close"), searchController.getInitialComponent(), true, translator.translate("examEditController.command.choose"));
			cmcSearchController.activate();
		} 

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == searchController) {
			cmcSearchController.deactivate();
			// repository search controller done
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				RepositoryEntry re = searchController.getSelectedEntry();
				
				Exam exam = ExamDBManager.getInstance().findExamByID(re.getOlatResource().getResourceableId());
				//checks if exam is already used in a other course
				if (exam.getCourseReference() != null) re = null;
				
				if (re != null) {
					setExamRepoReference(re, moduleConfiguration);
					content.contextPut(CHOSEN_ENTRY, re.getDisplayname());
					content.contextPut("showPreviewButton", Boolean.TRUE);
					// fire event so the updated config is saved by the
					// editormaincontroller
					fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
				} else{ //exam is already used in an other course
					this.getWindowControl().setError(translator.translate("examCourseNodeEditController.exam.already.used"));					
				}
			}	// else cancelled repo search
		} else if (source == accessCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = accessCondContr.getCondition();
				examCourseNode.setPreConditionAccess(cond);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == cmcExamCtr) {
			if (event == CloseableModalController.CLOSE_MODAL_EVENT) {
				examLaunchCtr.dispose();
				cmcExamCtr.dispose();
			}
		}

	}

	/**
	 * @see org.olat.core.gui.control.generic.tabbable.TabbableController#addTabs(org.olat.core.gui.components.TabbedPane)
	 */
	public void addTabs(TabbedPane tabbedPane) {
	//  wird anscheinend nicht mehr benötigt
	//	accessCondContr.setUserActivityLogger(getUserActivityLogger());
		tabs = tabbedPane;
		tabbedPane.addTab(translator.translate(PANE_TAB_ACCESSIBILITY), editAccessVc);
		tabbedPane.addTab(translator.translate(PANE_TAB_EXAMCONFIG), main);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		
		if (accessCondContr != null) {
			
			accessCondContr.dispose();
			accessCondContr = null;
		}
		
		if (cmcExamCtr != null) {
			
			cmcExamCtr.dispose();
			cmcExamCtr = null;
		}
		
		if (searchController != null) {
			
			searchController.dispose();
			searchController = null;
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController#getTranslator()
	 */
	public Translator getTranslator() {
		return translator;
	}

	/**
	 * @see org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController#getPaneKeys()
	 */
	public String[] getPaneKeys() {
		return paneKeys;
	}

	/**
	 * @see org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController#getTabbedPane()
	 */
	public TabbedPane getTabbedPane() {
		return tabs;
	}

	/**
	 * @param config the moduleConfig
	 * @param strict an assertion exception is thrown if no entry is found when
	 *          strict is set to true, otherwise, null is returned
	 * @return the repositoryentry or null if not in strict mode and no entry
	 *         found
	 * @throws AssertException when in strict mode and no entry is found
	 */
	public static RepositoryEntry getExamRepoReference(ModuleConfiguration config, boolean strict) {
		if (config == null) throw new AssertException("missing config in exam course node");
		String repoSoftkey = (String) config.get(ExamCourseNodeEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		if (repoSoftkey == null) throw new AssertException("invalid config when being asked for references");
		RepositoryManager rm = RepositoryManager.getInstance();
		RepositoryEntry entry = rm.lookupRepositoryEntryBySoftkey(repoSoftkey, strict);
		// entry can be null only if !strict
		return entry;
	}

	/**
	 * set an repository reference to an wiki course node
	 * @param re
	 * @param moduleConfiguration
	 */
	public static void setExamRepoReference(RepositoryEntry re, ModuleConfiguration moduleConfiguration) {
		moduleConfiguration.set(CONFIG_KEY_REPOSITORY_SOFTKEY, re.getSoftkey());
	}
	
	/**
	 * @param moduleConfiguration
	 * @return boolean
	 */
	public static boolean isModuleConfigValid(ModuleConfiguration moduleConfiguration) {
		return (moduleConfiguration.get(CONFIG_KEY_REPOSITORY_SOFTKEY) != null);
	}
	
	/**
	 * remove ref to wiki from the config
	 * @param moduleConfig
	 */
	public static void removeExamReference(ModuleConfiguration moduleConfig) {
		moduleConfig.remove(ExamCourseNodeEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
	}
	
}

