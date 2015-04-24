package de.htwk.autolat.BBautOLAT;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.Identity;
import org.olat.core.util.ExportUtil;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.WebappHelper;
import org.olat.course.ICourse;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.StatusDescriptionHelper;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Configuration.ConfigurationManagerImpl;
import de.htwk.autolat.Student.Student;
import de.htwk.autolat.Student.StudentManagerImpl;
import de.htwk.autolat.TaskConfiguration.TaskConfigurationManagerImpl;
import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskResult.TaskResult;
import de.htwk.autolat.TaskType.TaskTypeManagerImpl;
import de.htwk.autolat.tools.ImportExport.AutOlatNodeExporter;
import de.htwk.autolat.tools.ImportExport.AutOlatNodeImporter;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class BBautOLATCourseNode extends AbstractAccessableCourseNode implements AssessableCourseNode {

	public static final String TYPE = "autOLAT";

	/**
	 * Default constructor for course node of type single page
	 */
	public BBautOLATCourseNode() {
		super(TYPE);		
		updateModuleConfigDefaults(true);		
	}

	public void updateModuleConfigDefaults(boolean isNewNode) {
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			// use defaults for new course building blocks
			config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, false);
			config.setConfigurationVersion(1);
			// create the needed values in the config for controlling the edit process
			// config.setBooleanEntry("ConfigurationCreated", false);
			config.setBooleanEntry("ServerConnectionSet", false);
			config.setBooleanEntry("TaskTypeValid", false);
			config.setBooleanEntry("GradingTimeSet", false); 
			// set a server connection at the creation time
			// FIXME : statt "42" müsste da die courseID stehen... aber woher nehmen?	
			//Configuration conf = ConfigurationManagerImpl.getInstance().createAndPersistConfiguration(null, null, null, null, 42, Long.valueOf(getIdent()), null, null, null, null);
			// the flag here is actually superfluous (but is nevertheless still used) 
			config.setBooleanEntry("ConfigurationCreated", true);
			// the next block is obsolete
			/*
			ServerConnection connection = ServerConnectionManagerImpl.getInstance().getRandomServerConnection();
			if(connection != null) {
				conf.setServerConnection(connection);
				ConfigurationManagerImpl.getInstance().updateConfiguration(conf);
				config.setBooleanEntry("ServerConnectionSet", true);
			}
			*/
		}
	}
	/*
		moduleConfiguration = getModuleConfiguration();
		if (isNewNode) {
			// use defaults for new course building blocks
			moduleConfiguration.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, false);
			moduleConfiguration.setConfigurationVersion(1);
			// create the needed values in the config for controlling the edit process
			moduleConfiguration.setBooleanEntry("ConfigurationCreated", false);
			moduleConfiguration.setBooleanEntry("ServerConnectionSet", false);
			moduleConfiguration.setBooleanEntry("TaskTypeValid", false);
			moduleConfiguration.setBooleanEntry("GradingTimeSet", false);
			// set a server connection at the creation time
			Configuration conf = ConfigurationManagerImpl.getInstance().createAndPersistConfiguration(null, null, null, null, Long.valueOf(getIdent()), null, null, null);
			moduleConfiguration.setBooleanEntry("ConfigurationCreated", true);
			ServerConnection connection = ServerConnectionManagerImpl.getInstance().getRandomServerConnection();
			if(connection != null) {
				conf.setServerConnection(connection);
				ConfigurationManagerImpl.getInstance().updateConfiguration(conf);
				moduleConfiguration.setBooleanEntry("ServerConnectionSet", true);
			}
		}
	}
	*/
	
	@Override
	public TabbableController createEditController(UserRequest ureq,
			WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment euce) {
		// TODO Auto-generated method stub
		BBautOLATEditController childTabCntrllr = new BBautOLATEditController(getModuleConfiguration(), ureq, wControl, this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, euce, childTabCntrllr);
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#createEditController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl, org.olat.course.ICourse)
	 */
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, ICourse course, UserCourseEnvironment euce) {
		
		BBautOLATEditController childTabCntrllr = new BBautOLATEditController(getModuleConfiguration(), ureq, wControl, this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, euce, childTabCntrllr);

	}

	/**
	 * @see org.olat.course.nodes.CourseNode#createNodeRunConstructionResult(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.course.run.userview.NodeEvaluation)
	 */
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne, String nodecmd) {		
		return new NodeRunConstructionResult(new BBautOLATRunController(wControl, ureq, this, userCourseEnv, ne, false));
	}

	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		return new BBautOLATRunController(wControl, ureq, this, userCourseEnv, ne, true);
	}
	
	/**
	 * @see org.olat.course.nodes.CourseNode#isConfigValid()
	 */
	public StatusDescription isConfigValid() {
		
		StatusDescription sd = StatusDescription.NOERROR;
		String key_short = "", key_long = "";
		boolean connectionError = false;
		
		ModuleConfiguration config = this.getModuleConfiguration();
		
		//no grading time is set
		if(!config.getBooleanSafe("GradingTimeSet")) {
			key_short = "error.coursenode.missinggradingtime_short";
			key_long = "error.coursenode.missinggradingtime_long";
		}
		
		//no tasktype is set in the configuration
		if(!config.getBooleanSafe("TaskTypeValid")) {
			key_short = "error.coursenode.missingtasktype_short";
			key_long = "error.coursenode.missingtasktype_long";
		}
		
		//no server connection could be set
		if(!config.getBooleanSafe("ServerConnectionSet")) {
			key_short = "error.coursenode.missingserverconnection_short";
			key_long = "error.coursenode.missingserverconnection_long";
			connectionError = true;
		}
		
		if(key_short!="") {
			String translatorStr = Util.getPackageName(BBautOLATCourseNode.class);
			sd = new StatusDescription(ValidationStatus.ERROR, key_short, key_long, new String[]{this.getShortTitle()}, translatorStr);
			sd.setDescriptionForUnit(getIdent());
			if(connectionError) 
				sd.setActivateableViewIdentifier(BBautOLATEditController.PANE_KEY_SERVERCONFIG);
			else
				sd.setActivateableViewIdentifier(BBautOLATEditController.PANE_KEY_TASKCONFIG);
		}
		return sd;
	}


	/**
	 * @see org.olat.course.nodes.CourseNode#isConfigValid(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		//only here we know which translator to take for translating condition error messages
		String translatorStr = Util.getPackageName(BBautOLATCourseNode.class);
		List sds = isConfigValidWithTranslator(cev, translatorStr,getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}
	
	
	/**
	 * @see org.olat.course.nodes.CourseNode#getReferencedRepositoryEntry()
	 */
	public RepositoryEntry getReferencedRepositoryEntry() {
		//TODO
		return null;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#needsReferenceToARepositoryEntry()
	 */
	public boolean needsReferenceToARepositoryEntry() {
		//TODO
		return false;
	}
	
	//the following methods belong to the assessable course node

	public Float getCutValueConfiguration() {
		return null;
	}

	public String getDetailsListView(UserCourseEnvironment userCourseEnvironment) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDetailsListViewHeaderKey() {
		// TODO Auto-generated method stub
		return null;
	}

	public Float getMaxScoreConfiguration() {
		return null;
	}

	public Float getMinScoreConfiguration() {
		return null;
	}

	public Integer getUserAttempts(UserCourseEnvironment userCourseEnvironment) {
		
		//not needed, because the attempts are not just a counter but a limiter
		try {
			Long courseID = userCourseEnvironment.getCourseEnvironment().getCourseResourceableId();
			Long courseNodeID = Long.valueOf(getIdent());
			Student student = StudentManagerImpl.getInstance().getStudentByIdentity(userCourseEnvironment.getIdentityEnvironment().getIdentity());
			TaskInstance taskInstance = student.getTaskInstanceByConfiguration(ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, courseNodeID));	
			return (int) taskInstance.getLivingInstanceCounter();
		}
		catch (Exception e) {
			return 0;
		}
	}

	public String getUserCoachComment(UserCourseEnvironment userCourseEnvironment) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserLog(UserCourseEnvironment userCourseEnvironment) {
		// TODO Auto-generated method stub
		return null;
	}

	public ScoreEvaluation getUserScoreEvaluation(UserCourseEnvironment userCourseEnv) {
		
		ScoreEvaluation scoreEvaluation = new ScoreEvaluation(0f, false);
		
		try {
			Long courseID = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
			Long courseNodeID = Long.valueOf(getIdent());
			Student student = StudentManagerImpl.getInstance().getStudentByIdentity(userCourseEnv.getIdentityEnvironment().getIdentity());
			TaskInstance taskInstance = student.getTaskInstanceByConfiguration(ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, courseNodeID));	
			TaskResult taskResult = taskInstance.getResult();
			if(taskResult!=null) {
				scoreEvaluation = new ScoreEvaluation(Float.valueOf(String.valueOf(taskResult.getMaxScore())), true);
			}
		}
		catch (Exception e) {};
		
		return scoreEvaluation;
	}

	public String getUserUserComment(UserCourseEnvironment userCourseEnvironment) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasAttemptsConfigured() {
		return true;
	}

	public boolean hasCommentConfigured() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasDetails() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasPassedConfigured() {
		
		return true;
	}

	public boolean hasScoreConfigured() {
		
		return true;
	}

	public boolean hasStatusConfigured() {

		return false;
	}

	public void incrementUserAttempts(UserCourseEnvironment userCourseEnvironment) {
		// TODO Auto-generated method stub
		
	}

	public boolean isEditableConfigured() {
		// TODO Auto-generated method stub
		return false;
	}

	public void updateUserAttempts(Integer userAttempts, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity) {
		// TODO Auto-generated method stub
		
	}

	public void updateUserCoachComment(String coachComment, UserCourseEnvironment userCourseEnvironment) {
		// TODO Auto-generated method stub
		
	}

	public void updateUserScoreEvaluation(ScoreEvaluation scoreEvaluation, UserCourseEnvironment userCourseEnvironment,
			Identity coachingIdentity, boolean incrementAttempts) {
		// TODO Auto-generated method stub
		
	}

	public void updateUserUserComment(String userComment, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity) {
		// TODO Auto-generated method stub
		
	}
	
	private String getExportFilename() {
		return "autolatExport_"+this.getIdent()+".xml";
	}
	
	/*@Override
	public void cleanupOnDelete(ICourse course) {
		try {
			long courseNodeID = Long.valueOf(getIdent());
			Configuration conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(course.getResourceableId(), courseNodeID);
			TaskConfiguration taskConf = conf.getTaskConfiguration();
		
			ConfigurationManagerImpl.getInstance().deleteConfiguration(conf);
			TaskConfigurationManagerImpl.getInstance().deleteTaskConfiguration(taskConf);		
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
	*/
	
	@Override
	public void exportNode(File exportDirectory, ICourse course) {		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			long courseNodeID = Long.valueOf(getIdent());
			Configuration conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(course.getResourceableId(), courseNodeID);
			AutOlatNodeExporter exporter = new AutOlatNodeExporter(conf);
			exporter.exportNode(baos);
			ExportUtil.writeContentToFile(getExportFilename(), baos.toString(), exportDirectory, WebappHelper.getDefaultCharset());	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void importNode(File importDirectory, ICourse course, Identity identity, Locale locale)
	{		
		ModuleConfiguration config = getModuleConfiguration();
		File importFile = new File(importDirectory, getExportFilename());
		
		Configuration conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(course.getResourceableId(), Long.valueOf(getIdent()));
		
		if (importFile.exists()) {
			AutOlatNodeImporter importer = new AutOlatNodeImporter(conf);
			try {																	
				importer.importFromFile(importFile);
					
				//ServerConnectionManagerImpl.getInstance().saveOrUpdateServerConnection(conf.getServerConnection());
				TaskTypeManagerImpl.getInstance().saveOrUpdateTaskType(conf.getTaskConfiguration().getTaskType());
				TaskConfigurationManagerImpl.getInstance().saveOrUpdateTaskConfiguration(conf.getTaskConfiguration());
				ConfigurationManagerImpl.getInstance().updateConfiguration(conf);
							
				config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, false);
				config.setConfigurationVersion(1);
				// the flag here is actually superfluous (but is nevertheless still used) 			
				config.setBooleanEntry("ConfigurationCreated", true);
				config.setBooleanEntry("ServerConnectionSet", true);
				config.setBooleanEntry("TaskTypeValid", true);
				config.setBooleanEntry("GradingTimeSet", false);
			} catch (Exception e) {
				// import failed, roll the node back to factory settings
				updateModuleConfigDefaults(true);
				TaskConfigurationManagerImpl.getInstance().deleteTaskConfiguration(conf.getTaskConfiguration());
				ConfigurationManagerImpl.getInstance().deleteConfiguration(conf);
				// optional: create a new, empty configuration (not needed, missing configuration = empty configuration
				e.printStackTrace();				
			}
		} else {
			// nothing there to import, leave the node as it is
		}
	}

	@Override
	public Controller getDetailsEditController(UserRequest ureq,
			WindowControl wControl, BreadcrumbPanel stackPanel,
			UserCourseEnvironment userCourseEnvironment) {
		// TODO Auto-generated method stub
		return null;
	}
}