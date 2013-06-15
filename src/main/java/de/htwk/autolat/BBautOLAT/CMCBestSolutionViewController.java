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

import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskSolution.TaskSolution;
import de.htwk.autolat.tools.XMLParser.OutputObject;
import de.htwk.autolat.tools.XMLParser.Picture;
import de.htwk.autolat.tools.XMLParser.XMLParser;


public class CMCBestSolutionViewController extends BasicController{
	private VelocityContainer mainvc;
	private Panel main;
	private TaskSolution taskSolution;
	private TaskInstance taskInstance;

	public CMCBestSolutionViewController(UserRequest ureq, WindowControl wControl, TaskSolution taskSolution, TaskInstance taskInstance) {
		super(ureq, wControl);
		this.taskSolution = taskSolution;
		this.taskInstance = taskInstance;
		main = new Panel("viewBestSolution");
		mainvc = createVelocityContainer("CMCBestSolutionViewController");
		
		mainvc.contextPut("bestSolutionView", taskSolution.getSolutionText());
		createOutput(ureq);
		main = this.putInitialPanel(mainvc);
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void event(UserRequest arg0, Component arg1, Event arg2) {
		// TODO Auto-generated method stub
		
	}
	
	private void createOutput(UserRequest ureq) {
		mainvc.contextPut("taskView", taskInstance.getLivingTaskInstance().getTaskText());
		
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
			mainvc.put(aPic.getName(), taskImage);
		}
		mainvc.contextPut("solutionPieces", solutionPieces);
		mainvc.contextPut("score", taskSolution.getScore());
		
	}

}
