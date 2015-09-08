package de.htwk.autolat.BBautOLAT;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jdom.JDOMException;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.media.DefaultMediaResource;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.olat.course.run.environment.CourseEnvironment;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Configuration.ConfigurationManagerImpl;
import de.htwk.autolat.Connector.AutolatConnectorException;
import de.htwk.autolat.Connector.BBautOLATConnector;
import de.htwk.autolat.LivingTaskInstance.LivingTaskInstance;
import de.htwk.autolat.LivingTaskInstance.LivingTaskInstanceManagerImpl;
import de.htwk.autolat.Student.Student;
import de.htwk.autolat.Student.StudentManagerImpl;
import de.htwk.autolat.TaskConfiguration.TaskConfiguration;
import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskInstance.TaskInstanceManager;
import de.htwk.autolat.TaskInstance.TaskInstanceManagerImpl;
import de.htwk.autolat.TaskModule.TaskModule;
import de.htwk.autolat.TaskModule.TaskModuleManagerImpl;
import de.htwk.autolat.TaskResult.TaskResult;
import de.htwk.autolat.TaskResult.TaskResultManagerImpl;
import de.htwk.autolat.TaskSolution.TaskSolution;
import de.htwk.autolat.tools.StreamVFSLeaf;
import de.htwk.autolat.tools.Scoring.ScoreObject;
import de.htwk.autolat.tools.Scoring.ScoreObjectManagerImpl;
import de.htwk.autolat.tools.XMLParser.OutputObject;
import de.htwk.autolat.tools.XMLParser.Picture;
import de.htwk.autolat.tools.XMLParser.XMLParser;

/**
 * The Class TaskInstanceRunController.
 */
public class TaskInstanceRunController extends BasicController
{	
	/** The run panel. */
	private Panel runPanel;
	
	/** The run vc. */
	private VelocityContainer runVC;
	
	/** The task form. */
	private TaskRunForm taskForm;	
	
	/** The course id. */
	private long courseNodeID;
	
	/** The configuration. */
	private Configuration configuration;
	
	/** The task configuration. */
	private TaskConfiguration taskConfiguration;
		
	/** The Constant PACKAGE. */
	private static final String PACKAGE = Util.getPackageName(TaskInstanceRunController.class);
	
	/*
	 *  Several buttons to interact
	 */
	
	/** Loads an example that shows how a solutions should be formatted. */
	private Link loadExample;
	
	/** Loads the best solution the student has submitted. */
	private Link loadBest;
	
	/** Loads the last solution the student has submitted. */
	private Link loadLast;
	
	/** Loads the last correct solution the student has submitted. */
	private Link loadLastCorrect;
	
	/** Displays the top list. */
	private Link showTopList; 
	
	/** A button to load a new living task instance. This button will only
	 *  appear if the current living task instance has expired due to the
	 *  task module settings.  
   */
	private Link loadNewLivingTaskInstance;
	
	
	
	
	/** This is the text that will be displayed in the field where the students
	 *  can enter their solutions after the controller is freshly rendered. This
	 *  can be an example text or the last attempt of a solution. 
	 */
	private String solutionPreset;
	
	/** The identity of the user viewing the node. */
	private Identity userID;
	
	/** The student object associated with an identity. Roughly speaking the student
	 *  object will hold references to the tasks and solutions the student accessed
	 *  and submitted. 
	 */
	private Student student;
	
	/** The task instance for the student. Each node produces a task instance for
	 *  each 'participating' student (i.e. for one that's in some learning group).
	 *  The information stored is the current living task instance (for this
	 *  particular task), some of the submitted solutions (best, last, last correct),
	 *  a counter of how many attempts the student has made etc.
	 */
	private TaskInstance taskInstance;
	
	/** The living task instance. This is the task the student is actually working
	 *  on, i.e. it contains the actual task text.
	 */
	private LivingTaskInstance livingTaskInstance;
	
	/** This Object will hold a fully parsed task text that allows convenient
	 *  access of information stored in the task. It's basically a translation
	 *  from an XML to a Java object. 
	 */
	private OutputObject parsedLivingTask = null;

	/** An object that stores the feedback to a submitted attempt to solve the task,
	 *  i.e. an evaluation text, the score etc.
	 */
	private TaskSolution taskSolution;
	
	/** The fully parsed evaluation text. This object is very similar the the
	 *  parsedLivingTask object. */
	private OutputObject parsedTaskSolution;
		
	/** The connector handles all interaction with the (external) autotool server. */
	private BBautOLATConnector connector;
	
	
	
	/** A flag that shows if the student submitted a solutions that has been stored.
	 *  This flag determines if the buttons to load solutions will be activated or not.
	 */
	private boolean hasSolution = false;
	
	/** A flag that shows if the student submitted a CORRECT solutions that has been
	 *  stored. This flag determines if the buttons to load the last correct solutions
	 *  will be activated or not.
	 */
	private boolean hasCorrectSolution = false;
	
	/** After an previously persisted solution has been loaded the controller will show
	 *  the old evaluation text the student saw when they first submitted their solution.
	 *  So if an old solutions gets loaded this flag will be set to true.
	 */
	private boolean displayEvaluation = false;
	
	/** If a is expired because of the attached task module (i.e. the student exceeded
	 *  the maximum attempts or it took too much time) this flag will be set to true.
	 *  The input field will be disabled and the button to load a new living task
	 *  instance will be shown.  
	 */
	private boolean taskExpired;

	/** There might be a final task module with a absolute deadline. Once this task module
	 *  is expired getting a new task module will be pointless (because it will be the
	 *  same task module with the same dead line). So in this case we will lock the task
	 *  completely.
	 */
	private boolean taskLocked = false;


	/** The date when the node as a whole starts being active. Students can only score when
	 *  the node is active. However, they can still submit solutions (but they don't
	 *  get credit for them.)
	 */
	private Date beginDate;
	
	/** The date when the node as a whole starts being active. See beginDate for details. 
	  */
	private Date endDate;

	/** The current task module for the current livingTaskInstance. The task module holds
	 *  information of how many attempts a student can make to solve the task and/or how
	 *  much time they have to come up with a solution.
	 */
	private TaskModule taskModule;

	
	/** Once the student has passed the task (= come up with a correct solution) this
	 *  flag will be set and a notification will be displayed.
	 */
	private boolean hasPassed;
	
	/** The seed that will be used to generate a new living task instance. */
	private String seed;
			
	/** The max score. */
	private double maxScore = new Double(0);
	
	/** The CMC top list controller. */
	private CMCTopListController CMCTopListController;
	
	/** The CMC dialog. */
	private CloseableModalController CMCDialog;
	
	/** The user course env. */
	private CourseEnvironment userCourseEnv;
	
	/** The top list table. */
	private TableController topListTable;
	
	/** A flag that determines if the controller is viewed in preview mode or in
	 *  normal mode.
	 */
	private boolean isPreview;

	private Long courseID;
	
	/**
	 * Instantiates a new task instance run controller.
	 *
	 * @param ureq the ureq
	 * @param wControl the w control
	 * @param courseID the course id
	 * @param userCourseEnv the user course env
	 * @param isPreview the is preview
	 */
	public TaskInstanceRunController(UserRequest ureq, WindowControl wControl, long courseNodeID, CourseEnvironment userCourseEnv, boolean isPreview)
	{
		super(ureq, wControl);
		this.courseNodeID = courseNodeID;
		this.courseID = userCourseEnv.getCourseResourceableId();
		this.userCourseEnv = userCourseEnv;		
		this.isPreview = isPreview;
		PackageTranslator translator = new PackageTranslator(PACKAGE, this.getLocale());
		setTranslator(translator);
				
		this.configuration = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, courseNodeID);
		this.taskConfiguration = configuration.getTaskConfiguration();			
		
		try {
			connector = new BBautOLATConnector(configuration);
		} catch (JDOMException e) {
			showError("error.form.editconnection.XMLerror");
			connector = null;
		} catch (IOException e) {
			showError("error.form.editconnection.IOerror");
			connector = null;
		} catch (AutolatConnectorException e) {
			showError("error.form.editconnection.Servererror");
			connector = null;
		}				
		userID = ureq.getIdentity();
		
		initForms(ureq);
		
		// try to get a registration number
		this.seed = ureq.getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null);
		
		if(seed == null || seed.equals(""))
		{
			/*
			 *  quick hack to generate a fake registration number from the user name
			 *  in case there is no institutional user identifier
			 */
			String name = userID.getKey().toString();
			int namenumber = 1;
			int prime[] = {2, 3, 5, 7, 11, 13, 17, 19};		
			for(int i = 0; i < name.length() && i < 8; i++)
			{
				namenumber *= java.lang.Math.pow(prime[i], name.charAt(i) % 4);
				namenumber = namenumber % 1000000 + 1; 
			}
			seed = Integer.toString(namenumber);
		}
											
		this.student = StudentManagerImpl.getInstance().getStudentByIdentity(userID);
				
		if(student == null) // student does not exist yet
		{			
			student = StudentManagerImpl.getInstance().createStudent(null, userID);			
			
			createNewTaskInstance();

			// don't touch the data base if the node is only previewed!
			if(!isPreview)
			{
				StudentManagerImpl.getInstance().saveStudent(student);
			}

			/*
			 *  note:
			 *  livingTaskInstance and taskInstance are already set accordingly
			 *  because they were just freshly generated and persisted   
			 */
		}
		else //	student exists already
		{
			taskInstance = student.getTaskInstanceByConfiguration(configuration);
			
			if(taskInstance == null) // but has not accessed this very node before
			{  
				createNewTaskInstance();
				// don't touch the data base if the node is only previewed!
				if(!isPreview)
				{
					StudentManagerImpl.getInstance().updateStudent(student);
				}
			}
			else // student was here before, so just extract the data from the taskInstance
			{
				livingTaskInstance = taskInstance.getLivingTaskInstance();
				if(livingTaskInstance==null) {// might be null if the configuration was changed 
					createNewLivingTaskInstance();
				}
				if(livingTaskInstance!=null) // might STILL be null if the server is down
				{
					taskModule = taskInstance.getTaskModule();
					configuration = taskInstance.getConfiguration();
					taskConfiguration = taskInstance.getTaskConfiguration();
					try {
						connector = new BBautOLATConnector(configuration);
					} catch (JDOMException e) {
						showError("error.form.editconnection.XMLerror");
						connector = null;
					} catch (IOException e) {
						showError("error.form.editconnection.IOerror");
						connector = null;
					} catch (AutolatConnectorException e) {
						showError("error.form.editconnection.Servererror");
						connector = null;
					}				
				
					TaskResult taskResult = taskInstance.getResult(); 
				
					if(taskResult != null && taskResult.getHasPassed())
					{
						hasPassed = true;
						maxScore  = taskResult.getMaxScore();
					}
					else
						hasPassed = false;
				}
			}
		}			

		if(taskInstance != null)
		{
			beginDate = configuration.getBeginDate();
			endDate = configuration.getEndDate();
		
			taskInstance.getTaskModule();
						
			hasSolution = TaskInstanceManagerImpl.getInstance().hasSolution(taskInstance);
			hasCorrectSolution = TaskInstanceManagerImpl.getInstance().getLatestCorrectSolutionInTaskInstance(taskInstance) != null ?
					true : false;			
		}
		else {// just to be on the safe side 
			livingTaskInstance = null;
		}		
		// parse the livingTaskInstance
		
		if(livingTaskInstance != null)
		{
			try {
				parsedLivingTask = new XMLParser().parseString(livingTaskInstance.getTaskText());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JDOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			solutionPreset = livingTaskInstance.getSampleSolution();
		}
		
		runPanel = new Panel("runPanel");
		createOutput(ureq);
		putInitialPanel(runPanel);		
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub		
	}
	
	@Override
	protected void event(UserRequest ureq, Component comp, Event event)
	{
		if(comp == loadExample)
		{
			solutionPreset = livingTaskInstance.getSampleSolution();			
			displayEvaluation = false;
			createOutput(ureq);
		}
		
		if(comp == loadLast)
		{
			taskSolution = TaskInstanceManagerImpl.getInstance().getLatestSolutionInTaskInstance(taskInstance);
			solutionPreset = taskSolution.getSolutionText();
			displayEvaluation = true;
			createOutput(ureq);
		}
		
		if(comp == loadLastCorrect)
		{
			taskSolution = TaskInstanceManagerImpl.getInstance().getLatestCorrectSolutionInTaskInstance(taskInstance);
			solutionPreset = taskSolution.getSolutionText();
			displayEvaluation = true;
			createOutput(ureq);
		}
		
		if(comp == loadBest)
		{	
			taskSolution = TaskInstanceManagerImpl.getInstance().getBestSolutionInTaskInstance(taskInstance);			
			solutionPreset = taskSolution.getSolutionText();
			displayEvaluation = true;
			createOutput(ureq);
		}
		
		if(comp == loadNewLivingTaskInstance)
		{
			createNewLivingTaskInstance();
			createOutput(ureq);
		}
		
		if(comp == showTopList)
		{
			
	    	CMCTopListController = new CMCTopListController(ureq, getWindowControl(), student.getIdentity(), courseID, courseNodeID);			
			CMCDialog = new CloseableModalController(getWindowControl(),
					translate("label.controller.cmceditscorecontroller.close"), CMCTopListController.getInitialComponent());			
			CMCDialog.activate();
			
			
			/*
			CourseGroupManager cgm = userCourseEnv.getCourseGroupManager();
			Identity userID = student.getIdentity();
			List<BusinessGroup> grouplist = cgm.getParticipatingLearningGroupsFromAllContexts(userID);
			
			ArrayList<Identity> userIDs = new ArrayList<Identity>();
			
			for(BusinessGroup group : grouplist)			
				userIDs.addAll(cgm.getParticipantsFromLearningGroup(group.getName()));			
			*/
					
		}		
	}

	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller ctr, Event event)
	{
		if(ctr == taskForm)
		{
			if(event.equals(Form.EVNT_VALIDATION_OK))
			{			
				if(connector == null) {
					showError("error.form.editconnection.Servererror");
					return;
				}
				solutionPreset = taskForm.solution.getValue();				

				try {
					taskSolution = connector.gradeTaskSolution(livingTaskInstance, solutionPreset);
				} catch (AutolatConnectorException e) {
					showError("error.form.taskform.rpcerror", e.getCause().getMessage());
					return;
				}

				if(beginDate.before(new Date()) && endDate.after(new Date()))
				{
					/*
					 * The following function call ensures, that the current task type has been classified
					 * towards the scoring order.
					 */
					connector.getTaskConfiguration(taskInstance.getTaskConfiguration().getTaskType());

					// reload taskInstance to avoid hibernate duplicate object exception
					taskInstance = TaskInstanceManagerImpl.getInstance().loadTaskInstanceByID(taskInstance.getKey());

					// don't touch the data base if the node is only previewed!
					if(!isPreview)
					{
						TaskInstanceManagerImpl.getInstance().addTaskSolution(taskInstance, taskSolution);
					}
					
					taskExpired = TaskModuleManagerImpl.getInstance().evaluateTaskModuleConditions(
							taskModule, 
							taskInstance.getInstanceCounter(), 
							livingTaskInstance.getCreationDate()
					);
																	
					taskInstance.setInstanceCounter(taskInstance.getInstanceCounter()+1);
					
					// don't touch the data base if the node is only previewed!
					if(!isPreview)
					{
						TaskInstanceManagerImpl.getInstance().updateTaskInstance(taskInstance);
					}
				}
				else
				{
					if(endDate.before(new Date()))
					{
						//taskForm.getTextAreaElement("solution").setErrorKey("info.form.taskform.solutiontoolate");
						showWarning("info.form.taskform.solutiontoolate");
					}
					else
					{
						// taskForm.getTextAreaElement("solution").setErrorKey("info.form.taskform.solutiontoosoon");
						showWarning("info.form.taskform.solutiontoosoon");
					}
				}
				
				// update solution flags				
				hasSolution = TaskInstanceManagerImpl.getInstance().hasSolution(taskInstance);
				hasCorrectSolution = TaskInstanceManagerImpl.getInstance().getLatestCorrectSolutionInTaskInstance(taskInstance) != null ?
						true : false;
								
				// display the evaluation text for the solution just submitted
				displayEvaluation = true;
				
				// test if student has passed the task 
				TaskResult taskResult = taskInstance.getResult(); 				
				if(taskResult != null && taskResult.getHasPassed())
				{
					hasPassed = true;
					maxScore  = taskResult.getMaxScore();
				}
				else
					hasPassed = false;
				
				createOutput(ureq);

				// some experimental extra feedback
				/*
				if(endDate.before(new Date()))
				{
					taskForm.getTextAreaElement("solution").setExample(
							"<font style=\"color:#FF0000; font-size:120%\">"
							+ translate("info.form.taskform.solutiontoolate")
							+ "</font>");					
				}
				if(beginDate.after(new Date()))
				{					
					taskForm.getTextAreaElement("solution").setExample(
							"<font style=\"color:#FF0000; font-size:120%\">"
							+ translate("info.form.taskform.solutiontoosoon")
							+ "</font>");
				}
				*/
			}
		}
		
		if(ctr == loadExample)
		{
			solutionPreset = livingTaskInstance.getSampleSolution();			
			displayEvaluation = false;
			createOutput(ureq);
		}
		
		if(ctr == loadLast)
		{
			taskSolution = TaskInstanceManagerImpl.getInstance().getLatestSolutionInTaskInstance(taskInstance);
			solutionPreset = taskSolution.getSolutionText();
			displayEvaluation = true;
			createOutput(ureq);
		}
		
		if(ctr == loadLastCorrect)
		{
			taskSolution = TaskInstanceManagerImpl.getInstance().getLatestCorrectSolutionInTaskInstance(taskInstance);
			solutionPreset = taskSolution.getSolutionText();
			displayEvaluation = true;
			createOutput(ureq);
		}
		
		if(ctr == loadBest)
		{	
			taskSolution = TaskInstanceManagerImpl.getInstance().getBestSolutionInTaskInstance(taskInstance);			
			solutionPreset = taskSolution.getSolutionText();
			displayEvaluation = true;
			createOutput(ureq);
		}
		
		if(ctr == loadNewLivingTaskInstance)
		{
			createNewLivingTaskInstance();
			createOutput(ureq);
		}
		
		if(ctr == showTopList)
		{
			/* -- autolat8
	    	CMCTopListController = new CMCTopListController(ureq, getWindowControl(), student.getIdentity(), courseID);			
			CMCDialog = new CloseableModalController(getWindowControl(),
					translate("label.controller.cmceditscorecontroller.close"), CMCTopListController.getInitialComponent());			
			CMCDialog.activate();
			*/
			
			/*
			CourseGroupManager cgm = userCourseEnv.getCourseGroupManager();
			Identity userID = student.getIdentity();
			List<BusinessGroup> grouplist = cgm.getParticipatingLearningGroupsFromAllContexts(userID);
			
			ArrayList<Identity> userIDs = new ArrayList<Identity>();
			
			for(BusinessGroup group : grouplist)			
				userIDs.addAll(cgm.getParticipantsFromLearningGroup(group.getName()));			
			*/
					
		}
	}
	
	private void initForms(UserRequest ureq)
	{
		taskForm = new TaskRunForm(TaskRunForm.NAME, ureq, getWindowControl(), courseID, courseNodeID, null, true);
		taskForm.addControllerListener(this);
	}
	
	/**
	 * This method is called by the controller itself to assign variables
	 * accordingly, parse the text of the task and put the necessary data
	 * into the velocity container.
	 *
	 * @param ureq the ureq
	 */
	protected void createOutput(UserRequest ureq)
	{	
		runVC = null;
		runVC = this.createVelocityContainer("taskRunController");
		
		if(livingTaskInstance == null)
		{
			runVC = this.createVelocityContainer("noLivingTaskInstance");
			runPanel.setContent(runVC);
			return;
		}
		
		taskExpired = TaskModuleManagerImpl.getInstance().evaluateTaskModuleConditions(
				taskModule, 
				taskInstance.getInstanceCounter(), 
				livingTaskInstance.getCreationDate()
		);
		
		String html = parsedLivingTask.toString();
		/*
 		 * html code and images are rendered differently. html code can just be passed into
 		 * the velocity container. images on the other hand have to be put in ImageComponents
 		 * first.  
		 */
		List<String> htmlPieces = Arrays.asList(html.split("</?OLATPIC>"));
		List<Picture> pictureList = parsedLivingTask.getPictures();		
						
		for(Picture aPic : pictureList)
		{
			ImageComponent taskImage = new ImageComponent(ureq.getUserSession(), aPic.getName());
			taskImage.setMedia(new StreamVFSLeaf(aPic.getName(), aPic.getBase64()), aPic.getMimeType());
			runVC.put(aPic.getName(), taskImage);
		}

		runVC.contextPut("htmlPieces", htmlPieces);
		
		//taskForm = new TaskRunForm(TaskRunForm.NAME, ureq, wControl, courseID, taskInstance, true);
		//taskForm.addControllerListener(this);
		taskForm.solution.setValue(solutionPreset);
		taskForm.setDocumentation(livingTaskInstance.getSampleDocumentation());
		
		//taskForm = new TaskRunForm(TaskRunForm.NAME, getTranslator(), courseID, taskInstance, true);
		//taskForm.addListener(this);		
		//taskForm.getTextAreaElement("solution").setValue(solutionPreset);
		
				
		loadExample = LinkFactory.createButton("label.controller.task.solutions.loadexample", runVC, this);
		loadLast = LinkFactory.createButton("label.controller.task.solutions.loadlast", runVC, this);
		loadLastCorrect = LinkFactory.createButton("label.controller.task.solutions.loadlastcorrect", runVC, this);
		loadBest = LinkFactory.createButton("label.controller.task.solutions.loadbest", runVC, this);
						
		loadLast.setEnabled(hasSolution);
		loadLastCorrect.setEnabled(hasCorrectSolution);
		loadBest.setEnabled(hasSolution);
		
		showTopList = LinkFactory.createButton("label.controller.task.toplist.show", runVC, this);
						
		if(taskExpired)
		{
			loadNewLivingTaskInstance = LinkFactory.createButton("label.controller.task.newtaskinstance", runVC, this);
			//loadNewLivingTaskInstance.addListener(this);
			runVC.put("getNewTaskInstance", loadNewLivingTaskInstance); 
			taskForm.setDisplayOnly(true);
		}
		
		// if task is expired check if the next task module would allow a new living task instance
		// if not, then lock the task (i.e. display that there are no new living task instances)
		
		
		if(taskExpired && (connector != null))
		{
			// generate a new seed based on the old seed
			String tempSeed = getNextSeed(seed);
			
			// get a new livingTaskInstance
			LivingTaskInstance tempLivingTaskInstance = connector.getLivingTaskInstance(taskConfiguration, tempSeed);
			
			// get the next task module
			TaskModule tempTaskModule = TaskModuleManagerImpl.getInstance().getNextTaskModule(courseID, courseNodeID, taskModule);
			
			// check if the new task module is already expired ...
			Boolean nextTaskExpired = TaskModuleManagerImpl.getInstance().evaluateTaskModuleConditions(
					tempTaskModule, 
					0, 
					tempLivingTaskInstance.getCreationDate()
			);
			
			// ... and if so then lock the task (and don't load any new stuff into the taskInstance),
			// otherwise activate the new living task instance
			if(nextTaskExpired)
			{
				taskLocked = true;
			}
		}
				
		if(displayEvaluation)
		{
			parsedTaskSolution = null;
			
			try {
				parsedTaskSolution = new XMLParser().parseString(taskSolution.getEvaluationText());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JDOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
						
			String solution = parsedTaskSolution.toString();
			List<String> solutionPieces = Arrays.asList(solution.split("</?OLATPIC>"));
			List<Picture> solutionPictureList = parsedTaskSolution.getPictures();		
						
			for(Picture aPic : solutionPictureList)
			{
				ImageComponent taskImage = new ImageComponent(ureq.getUserSession(), aPic.getName());
				taskImage.setMedia(new StreamVFSLeaf(aPic.getName(), aPic.getBase64()), aPic.getMimeType());
				runVC.put(aPic.getName(), taskImage);
			}
			runVC.contextPut("solutionPieces", solutionPieces);
			runVC.contextPut("score", taskSolution.getScore());
		}
		
		// build the string for the description text, i.e. leave some space between
		// description and task text and prefix the description text with a string
		// (like "Remark:" or "N.B.") -- but only do so, if there actually is a
		// description text
		String descriptionText = taskConfiguration.getDescriptionText();
		if(!descriptionText.equals(""))
		{
			descriptionText = "<div style=\"margin-top:1em\"><font style=\"font-style: italic;\">"
				+ translate("label.controller.task.description") + ": " + descriptionText
				+ "</font></div>";
		}
		
		runVC.contextPut("descriptionText", descriptionText);
		runVC.contextPut("isPreview", isPreview);
		runVC.contextPut("displayEvaluation", displayEvaluation);
		runVC.contextPut("taskExpired", taskExpired);
		runVC.contextPut("taskLocked", taskLocked);
		runVC.contextPut("hasPassed", hasPassed);
		runVC.contextPut("maxScore", maxScore);
		runVC.put("taskForm", taskForm.getInitialComponent());
		runVC.put("loadExample", loadExample);
		runVC.put("loadLast", loadLast);
		runVC.put("loadLastCorrect", loadLastCorrect);
		runVC.put("loadBest", loadBest);
		runVC.put("showTopList", showTopList);
		
		runPanel.setContent(runVC);
	}
	
	/**
	 * Creates the new task instance, i.e. create a taskInstance object with
	 * the required data (configuration, livingTaskInstance, counter, etc...)
	 * This method also retrieves a living task instance from the autotool
	 * server. It also attaches the task instance to the student object.
	 */
	private void createNewTaskInstance()
	{	
		if(connector == null) {
			return;
		}
		// get a new livingTaskIntance
		livingTaskInstance = connector.getLivingTaskInstance(taskConfiguration, seed);
		
		/*
		 *  if the previous line yields a livingTaskInstance == null, the autotool-Server
		 *  might be down. everything else will just make things worse, so we
		 *  gracefully end this here.
		 */
		if(livingTaskInstance == null)
			return;
		
		// don't touch the data base if the node is only previewed!
		if(!isPreview)
		{
			LivingTaskInstanceManagerImpl.getInstance().saveLivingTaskInstance(livingTaskInstance);
		}
		
		// get the first taskModule for this course node 
		taskModule = TaskModuleManagerImpl.getInstance().getNextTaskModule(courseID, courseNodeID, null);
		
		// create a taskInstance that holds all the info obtained before
		taskInstance = TaskInstanceManagerImpl.getInstance().createTaskInstance(
				null, // List solutions, it's empty since we just created the first livingTaskInstance
				configuration,
				0, // counter
				livingTaskInstance,
				null, // TaskResult
				student,
				taskConfiguration,
				taskModule); // TaskModule
		
		// since we just successfully added a new living task instance increase the counter
		taskInstance.incrementLivingInstanceCounter();

		// don't touch the data base if the node is only previewed!
		if(!isPreview)
		{
			TaskInstanceManagerImpl.getInstance().saveTaskInstance(taskInstance);
		}
		
		// add this taskInstance to the student who accessed this course node 
		student.addTaskInstance(taskInstance);
	}
	
	/**
	 * Creates a new living task instance (but not the first). New living task
	 * instances need to be created if the old one was lost (due to a changed
	 * configuration) or expired (due to the task module).
	 */
	private void createNewLivingTaskInstance()
	{	
		if(connector == null) {
			return;
		}
		// generate a new seed based on the old seed
		seed = getNextSeed(seed);
		
		// get a new livingTaskInstance
		livingTaskInstance = connector.getLivingTaskInstance(taskConfiguration, seed);
		
		/*
		 *  if the last line yields a livingTaskInstance == null, the autotool-Server
		 *  might be down. everything else will just make things worse, so we
		 *  gracefully end this here.
		 */
		if(livingTaskInstance == null)
			return;
		
		// get the next task module
		taskModule = TaskModuleManagerImpl.getInstance().getNextTaskModule(courseID, courseNodeID, taskModule);
						
		// delete the old livingTaskInstance
		taskInstance.setLivingTaskInstance(null);
		// don't touch the data base if the node is only previewed!
		if(!isPreview)
		{
			TaskInstanceManagerImpl.getInstance().updateTaskInstance(taskInstance);
			LivingTaskInstanceManagerImpl.getInstance().deleteLivingTaskInstance(livingTaskInstance);
		}
							
		// don't touch the data base if the node is only previewed!
		if(!isPreview)
		{
			LivingTaskInstanceManagerImpl.getInstance().saveLivingTaskInstance(livingTaskInstance);
		}
		
		// update the taskInstance with the new stuff
		taskInstance.setLivingTaskInstance(livingTaskInstance);		
		taskInstance.setInstanceCounter(0);
		taskInstance.incrementLivingInstanceCounter();
		taskInstance.setTaskModule(taskModule);

		// don't touch the data base if the node is only previewed!
		if(!isPreview)
		{
			TaskInstanceManagerImpl.getInstance().updateTaskInstance(taskInstance);
		}
				
		// parse the livingTaskInstance		
		try {
			parsedLivingTask = new XMLParser().parseString(livingTaskInstance.getTaskText());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		solutionPreset = livingTaskInstance.getSampleSolution();
		taskForm.setDisplayOnly(false);
		displayEvaluation = false;
	}
	
	/** Generates a new seed from a given seed.
	 *  
	 */
	private String getNextSeed(String seed)
	{
		int seednumber = Integer.valueOf(seed.trim());
		seednumber = ((seednumber*3) % 1000000) + 1;
		String newSeed = String.valueOf(seednumber);
		return newSeed;
	}

}

