package de.htwk.autolat.BBautOLAT;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.jdom.JDOMException;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.DefaultMediaResource;
import org.olat.core.gui.translator.Translator;

import de.htwk.autolat.LivingTaskInstance.LivingTaskInstance;
import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskSolution.TaskSolution;
import de.htwk.autolat.tools.XMLParser.OutputObject;
import de.htwk.autolat.tools.XMLParser.Picture;
import de.htwk.autolat.tools.XMLParser.XMLParser;

public class CMCTaskViewController extends BasicController{
	private VelocityContainer testVC;
	private TaskInstance taskInstance;
	private TaskSolution taskSolution;
	private TaskInstanceTestController testCon;
	
	public CMCTaskViewController(UserRequest ureq, WindowControl wControl, TaskInstance taskInstance, TaskSolution taskSolution, long courseID, long courseNodeID) {
		super(ureq, wControl);
		this.taskInstance = taskInstance;
		this.taskSolution = taskSolution;

		testVC = createVelocityContainer("CMCTaskViewController");
		createOutput(ureq, wControl, courseID, courseNodeID);
		putInitialPanel(testVC);
	}
	
	private void createOutput(UserRequest ureq, WindowControl wControl, long courseID, long courseNodeID){
		testCon = new TaskInstanceTestController.Builder(ureq, wControl, courseID, courseNodeID)
			.showTaskText()
			.showSolutionText()
			.setLivingTaskInstance(taskInstance.getLivingTaskInstance())
			.setTaskSolution(taskSolution)
			.build();
		testVC.put("testController", testCon.getInitialComponent());
	}

	/*private void createOutput(UserRequest ureq) {
		
		//TASK --------------------------------------------------------------------------
		OutputObject parsedTask = null;
		LivingTaskInstance livingTaskInstance = taskInstance.getLivingTaskInstance();
		
		try {
			parsedTask = new XMLParser().parseString(livingTaskInstance.getTaskText());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String html = parsedTask.toString();
		
 		// html code and images are rendered differently. html code can just be passed into
 		// the velocity container. images on the other hand have to be put in ImageComponents
 		// first.  
		
		List<String> htmlPieces = Arrays.asList(html.split("</?OLATPIC>"));
		List<Picture> pictureList = parsedTask.getPictures();		
		
		// System.out.println(html);
		
		// unneeded (since objects are put into velocity container)
		// List<ImageComponent> imageComponents = new ArrayList<ImageComponent>();
		// List<DefaultMediaResource> mediaResources = new ArrayList<DefaultMediaResource>();
		
		for(Picture aPic : pictureList)
		{
			DefaultMediaResource mediaResource = new DefaultMediaResource();
			mediaResource.setInputStream(aPic.getDecodedPictureStream());
			ImageComponent taskImage = new ImageComponent(aPic.getName());
			taskImage.setMediaResource(mediaResource);			
			
			// unneeded (since objects are put into velocity container)
			// mediaResources.add(mediaResource);
			// imageComponents.add(taskImage);
		
			// set double to int or long
			// taskImage.setWidth(aPic.getWidth());
			// taskImage.setHeight(aPic.getHeight());			
			mainVC.put(aPic.getName(), taskImage);
		}

		mainVC.contextPut("htmlPieces", htmlPieces);
		
		
		//SOLUTION -------------------------------------------------------------------------------
		mainVC.contextPut("bestSolutionView", taskSolution.getSolutionText());
		mainVC.contextPut("taskView", taskInstance.getLivingTaskInstance().getTaskText());
		
		OutputObject parsedTaskSolution = null;
		TaskSolution taskSolution = taskInstance.getBestSolution();
		
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
			DefaultMediaResource mediaResource = new DefaultMediaResource();
			mediaResource.setInputStream(aPic.getDecodedPictureStream());
			ImageComponent taskImage = new ImageComponent(aPic.getName());
			taskImage.setMediaResource(mediaResource);			
			// taskImage.setWidth(aPic.getWidth());
			// taskImage.setHeight(aPic.getHeight());			
			mainVC.put(aPic.getName(), taskImage);
		}
		mainVC.contextPut("solutionPieces", solutionPieces);
		mainVC.contextPut("score", taskSolution.getScore());
	} */

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void event(UserRequest arg0, Component arg1, Event arg2) {
		// TODO Auto-generated method stub
		
	}

}
