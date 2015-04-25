package de.htwk.autolat.BBautOLAT;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.jdom.JDOMException;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Configuration.ConfigurationManagerImpl;
import de.htwk.autolat.Connector.AutolatConnectorException;
import de.htwk.autolat.Connector.BBautOLATConnector;
import de.htwk.autolat.LivingTaskInstance.LivingTaskInstance;
import de.htwk.autolat.TaskConfiguration.TaskConfiguration;
import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskSolution.TaskSolution;
import de.htwk.autolat.tools.StreamVFSLeaf;
import de.htwk.autolat.tools.XMLParser.OutputObject;
import de.htwk.autolat.tools.XMLParser.Picture;
import de.htwk.autolat.tools.XMLParser.XMLParser;

// TODO: Auto-generated Javadoc
/**
 * The TaskInstanceTestController will display a task and allow generating
 * random task. Solutions can be submitted and a feedback will be displayed.
 * No data base entries will be generated or persisted. 
 * <br><br>
 * To further customize which parts are hidden or visible use the builder. 
 */
public class TaskInstanceTestController extends BasicController
{	
	
	private Panel testPanel; 
	private VelocityContainer testVC;
	private TaskRunForm taskForm;	
	private long courseNodeID;
	private Configuration configuration;
	private TaskConfiguration taskConfiguration;
	private LivingTaskInstance livingTaskInstance;
	private static final String PACKAGE = Util.getPackageName(TaskInstanceTestController.class);
	// dice button
	private Link randomSeed;
	private Link loadExample;
	private String solutionPreset;
	private OutputObject parsedTask = null;
	private Identity userID;
	private BBautOLATConnector connector;
	private TaskSolution taskSolution;
	private OutputObject parsedTaskSolution;
	private boolean displaySolution = false;
	private TaskSeedForm taskSeedForm;
	private String seed;
	private UserCourseEnvironment userCourseEnv;
	
	// Flags
	private boolean showSeedInputField = true;
	private boolean showRandomSeedButton = true;
	private boolean showTaskText = true;
	private boolean showOptions = true;
	private boolean showSolutionForm = true;
	private boolean showSolutionText = false;
	private long courseID;
		
	/**
	 * The inner Class Builder. This class is used to hold several options for the
	 * TaskInstanceTestController. These will be passed to the actual constructor with
	 * the build() method.
	 * <br><br>
	 * <b>Example:</b>
	 * <pre>
	 * taskInstanceTestController = new TaskInstanceTestController
	 * .Builder(ureq, getWindowControl(), courseNodeID, ne)
	 * .showSeedInputField()
   * .showSolutionForm()
	 * .build();
	 * </pre>
	 */
	public static class Builder
	{
		// required parameters				
		protected UserRequest ureq;		
		protected WindowControl wControl;
		protected long courseNodeID;
		
		// optional parameters
		protected boolean showSeedInputField = false;
		protected boolean showRandomSeedButton = false;
		protected boolean showTaskText = false;
		protected boolean showOptions = false;
		protected boolean showSolutionForm = false;
		protected boolean showSolutionText = false;
		protected String seed = null;
		protected LivingTaskInstance livingTaskInstance = null;
		protected TaskSolution taskSolution = null;
		private long courseID;
				
		/**
		 * Instantiates a new builder for a blank TaskTestController. Desired components are
		 * added/activated with the showXXX methods. Additionally a custom seed, a living task
		 * instance and/or a task solution can be passed with the set methods.
		 * 
		 * Setting a living task instances overwrites any seed that might have been set.
		 * If no seed or living task instances is set in the builder a random seed will be used.   
		 *
		 * @param ureq the ureq
		 * @param wControl the w control
		 * @param courseNodeID the course node id
		 */
		public Builder(UserRequest ureq, WindowControl wControl, long courseID, long courseNodeID)
		{
			this.ureq = ureq;
			this.wControl = wControl;
			this.courseID = courseID;
			this.courseNodeID = courseNodeID;
		}
						
		public Builder showSeedInputField()
		{ this.showSeedInputField = true; return this; }
		
		public Builder showRandomSeedButton()
		{ this.showRandomSeedButton = true; return this; }
		
		public Builder showTaskText()
		{ this.showTaskText = true; return this; }

		public Builder showOptions()
		{ this.showOptions = true; return this; }
		
		public Builder showSolutionForm()
		{	this.showSolutionForm = true; return this; }
		
		public Builder showSolutionText()
		{	this.showSolutionText = true; return this; }
				
		public Builder setSeed(String seed)
		{ this.seed = seed; return this; }
				
		public Builder setLivingTaskInstance(LivingTaskInstance livingTaskInstance)
		{ this.livingTaskInstance = livingTaskInstance; return this; }
		
		public Builder setTaskSolution(TaskSolution taskSolution)
		{ this.taskSolution = taskSolution; return this; }
				
		public TaskInstanceTestController build()
		{ return new TaskInstanceTestController(this); }
	}
	
	
	/**
	 * Instantiates a new task instance test controller using the builder to
	 * pass several options and parameters.
	 *
	 * @param builder the builder
	 */
	public TaskInstanceTestController(Builder builder)
	{
		super(builder.ureq, builder.wControl);
		this.courseID = builder.courseID;
		this.courseNodeID = builder.courseNodeID;
		PackageTranslator translator = new PackageTranslator(PACKAGE, this.getLocale());
		setTranslator(translator);
		userID = builder.ureq.getIdentity();
		configuration = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, courseNodeID);		
		taskConfiguration = configuration.getTaskConfiguration();
		try {
			connector = new BBautOLATConnector(configuration);
		} catch (JDOMException e) {
			showInfo("error.form.editconnection.XMLerror");
			connector = null;
		} catch (IOException e) {
			showInfo("error.form.editconnection.IOerror");
			connector = null;
		} catch (AutolatConnectorException e) {
			showInfo("error.form.editconnection.Servererror");
			connector = null;
		}				
		
		// retrieve the flags from the builder
		this.showSeedInputField = builder.showSeedInputField;
		this.showRandomSeedButton = builder.showRandomSeedButton;
		this.showTaskText = builder.showTaskText;
		this.showOptions = builder.showOptions;
		this.showSolutionForm = builder.showSolutionForm;
		this.showSolutionText = builder.showSolutionText;
		this.taskSolution = builder.taskSolution;

		initForms(builder.ureq);
		
		testPanel = new Panel("testPanel");
		if(builder.livingTaskInstance == null)
			setTaskSeed(builder.seed, builder.ureq);
		else
			setLivingTaskInstance(builder.livingTaskInstance, builder.ureq);
		
		putInitialPanel(testPanel);		
	}
	
	
	/**
	 * Instantiates a basic task instance test controller. All fields and buttons
	 * will be displayed by default. A seed will be randomly generated.
	 * Normally one would use the builder for extended control over the appearance.
	 *
	 * @param ureq the user request
	 * @param wControl the window control
	 * @param courseNodeID the course node ID
	 * @param ne the NodeEvaluation
	 */
	public TaskInstanceTestController(UserRequest ureq, WindowControl wControl, long courseID, long courseNodeID)
	{
		super(ureq, wControl);
		this.courseID = courseID;
		this.courseNodeID = courseNodeID;		
		PackageTranslator translator = new PackageTranslator(PACKAGE, this.getLocale());
		setTranslator(translator);
		userID = ureq.getIdentity();		
		
		configuration = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, courseNodeID);		
		taskConfiguration = configuration.getTaskConfiguration();
		try {
			connector = new BBautOLATConnector(configuration);
		} catch (JDOMException e) {
			showInfo("error.form.editconnection.XMLerror");
			connector = null;
		} catch (IOException e) {
			showInfo("error.form.editconnection.IOerror");
			connector = null;
		} catch (AutolatConnectorException e) {
			showInfo("error.form.editconnection.Servererror");
			connector = null;
		}				

		initForms(ureq);
		
		testPanel = new Panel("testPanel");
		// null -> random seed
		setTaskSeed(null, ureq);		
		
		putInitialPanel(testPanel);		
	}	

	/**
	 * Do dispose.
	 *
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub		
	}

	/**
	 * Event.
	 *
	 * @param ureq the ureq
	 * @param comp the comp
	 * @param event the event
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component comp, Event evnt)
	{
		if(comp == randomSeed)
		{
			setTaskSeed(null, ureq);
			taskSolution = null;
		}
				
		if(comp == loadExample)
		{
			solutionPreset = livingTaskInstance.getSampleSolution();			
			taskSolution = null;
			displaySolution = false;
			createOutput(ureq);
		}

    }

	/**
	 * Event.
	 *
	 * @param ureq the ureq
	 * @param ctr the ctr
	 * @param evnt the evnt
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller ctr, Event event)
	{
		if(ctr == taskForm)
		{
			if(event.equals(Form.EVNT_VALIDATION_OK))
			{
				if(connector == null) {
					showInfo("error.form.editconnection.Servererror");
					return;
				}
				String enteredTaskSolution = taskForm.solution.getValue();				
				try {
					taskSolution = connector.gradeTaskSolution(livingTaskInstance, enteredTaskSolution);
				} catch (AutolatConnectorException e) {
					showError("error.form.taskform.rpcerror", e.getCause().getMessage());
					return;
				}

				showInfo("info.form.taskform.solutionsubmitted");
				solutionPreset = taskSolution.getSolutionText();				
				// displaySolution = true;
				createOutput(ureq);
			}
		}
		
		if(ctr == taskSeedForm)
		{			
			if(event.equals(Form.EVNT_VALIDATION_OK))
			{				
				String newSeed = taskSeedForm.seed.getValue();				
				setTaskSeed(newSeed.equals("") ? null : newSeed, ureq);
				taskSolution = null;
				createOutput(ureq);
			}
		}
	}
	
	
	private void initForms(UserRequest ureq)
	{
		taskSeedForm = new TaskSeedForm(TaskSeedForm.NAME, ureq, getWindowControl(), courseNodeID);
		taskSeedForm.addControllerListener(this);
		
		taskForm = new TaskRunForm(TaskRunForm.NAME, ureq, getWindowControl(), courseID, courseNodeID, null, false);
		taskForm.addControllerListener(this);
	}
	
	/**
	 * Creates the output for the controller, i.e. initializing components,
	 * assigning variables and setting up the velocity container.
	 *
	 * @param ureq the ureq
	 */
	private void createOutput(UserRequest ureq)
	{					
		testVC = null;
		testVC = this.createVelocityContainer("taskTestController");
		
		if(livingTaskInstance == null)
		{
			testVC = this.createVelocityContainer("noLivingTaskInstance");
			testPanel.setContent(testVC);
			return;
		}
			
		//taskSeedForm = new TaskSeedForm(TaskSeedForm.NAME, ureq, getWindowControl(), courseNodeID);
		//taskSeedForm.addControllerListener(this);
		taskSeedForm.seed.setValue(seed);		
				
		// parse the task text and put it into the velocity container
		// parseLivingTaskInstance();
		
		String html = parsedTask.toString();
		List<String> htmlPieces = Arrays.asList(html.split("</?OLATPIC>"));
		testVC.contextPut("htmlPieces", htmlPieces);
		
		// put the pictures (if any) into the velocity container
				
		List<Picture> pictureList = parsedTask.getPictures();					
		for(Picture aPic : pictureList)
		{
			ImageComponent taskImage = new ImageComponent(ureq.getUserSession(), aPic.getName());
			taskImage.setMedia(new StreamVFSLeaf(aPic.getName(), aPic.getBase64()), aPic.getMimeType());
			testVC.put(aPic.getName(), taskImage);
		}

		// create the form where the solution can be entered and submitted  
												
		//taskForm = new TaskRunForm(TaskRunForm.NAME, ureq, wControl, courseNodeID, null, false);
		//taskForm.addControllerListener(this);
		taskForm.solution.setValue(solutionPreset);
		taskForm.setDocumentation(livingTaskInstance.getSampleDocumentation());
				
		randomSeed = LinkFactory.createButton("label.controller.task.random.button", testVC, this);
		
		// create a button to load the example into the solution text box
		
		loadExample = LinkFactory.createButton("label.controller.task.solutions.loadexample", testVC, this);
				
		// if a solution has been submitted, display the evaluation (called 'solution' as well) 
		
		if(taskSolution!=null)
		{	
			testVC.contextPut("solutionText", taskSolution.getSolutionText());
			// first parse the solution			
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
			
			// then put it into the velocity container (same method as above)						
			
			String solution = parsedTaskSolution.toString();
			List<String> solutionPieces = Arrays.asList(solution.split("</?OLATPIC>"));
			testVC.contextPut("solutionPieces", solutionPieces);
			
			List<Picture> solutionPictureList = parsedTaskSolution.getPictures();				
			for(Picture aPic : solutionPictureList)
			{
				ImageComponent taskImage = new ImageComponent(ureq.getUserSession(), aPic.getName());
				taskImage.setMedia(new StreamVFSLeaf(aPic.getName(), aPic.getBase64()), aPic.getMimeType());
				testVC.put(aPic.getName(), taskImage);
			}
		} // end if(displaySolution)
		
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
		
		testVC.contextPut("descriptionText", descriptionText);
		
		testVC.contextPut("displaySolution", taskSolution!=null);
		
		testVC.contextPut("showSeedInputField", showSeedInputField);
		testVC.contextPut("showRandomSeedButton", showRandomSeedButton);
		testVC.contextPut("showTaskText", showTaskText);
		testVC.contextPut("showOptions", showOptions);
		testVC.contextPut("showSolutionForm", showSolutionForm);
		testVC.contextPut("showSolutionText", showSolutionText);
		
		testVC.put("taskSeedForm", taskSeedForm.getInitialComponent());
		testVC.put("taskForm", taskForm.getInitialComponent());
		testVC.put("randomSeed", randomSeed);
		testVC.put("loadExample", loadExample);		
		
		testPanel.setContent(testVC);
	}
	
	/**
	 * Sets a living task instance as input for the test controller.
	 * The displayed task will the the task of that livingTaskInstance.
	 * The task controller will automatically be re-rendered.
	 * <br><br>
	 * This method is typically used to display the task a student is currently
	 * working on.
	 *
	 * @param taskInstance the new living task instance
	 */
	public void setLivingTaskInstance(LivingTaskInstance livingTaskInstance, UserRequest ureq)
	{
		if(livingTaskInstance != null)
		{
			seed = "";
			this.livingTaskInstance = livingTaskInstance;
			//taskSolution = null;
			//displaySolution = false;
			parseLivingTaskInstance();
			solutionPreset = livingTaskInstance.getSampleSolution();
		}
		else
			setTaskSeed(seed, ureq);			

		createOutput(ureq);
	}
	
	/**
	 * Manually sets the task seed by passing a string. Passing NULL will generate a
	 * random seed. The task controller will automatically be re-rendered.
	 * <br><br>
	 * This method is called by the controller itself if one of the according buttons
	 * are pressed. It can also be called by another class, e.g. to set the seed if 
	 * the test controller is displayed in button-less mode.  
	 * 
	 * @param seed the new task seed
	 */
	public void setTaskSeed(String seed, UserRequest ureq)
	{		
		if(connector == null) {
			showInfo("error.form.editconnection.Servererror");
			return;
		}
		if(seed != null)
			this.seed = seed;
		else
		{
			Random generator = new Random(System.currentTimeMillis());			 			 
			this.seed = Integer.toString(100000 + generator.nextInt(900000));
		}
		
		livingTaskInstance = connector.getLivingTaskInstance(taskConfiguration, this.seed);
		if(livingTaskInstance != null)
		{
			//taskSolution = null;
			//displaySolution = false;
			parseLivingTaskInstance();
			solutionPreset = livingTaskInstance.getSampleSolution();
		}
		createOutput(ureq);
	}
	
	/**
	 * Parses the living task instance.
	 */
	private void parseLivingTaskInstance()
	{	
		// parse the livingTaskInstance		
		try {
			parsedTask = new XMLParser().parseString(livingTaskInstance.getTaskText());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
