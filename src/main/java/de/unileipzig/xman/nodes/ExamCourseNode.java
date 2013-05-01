package de.unileipzig.xman.nodes;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.editor.*;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.StatusDescriptionHelper;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntry;

import de.unileipzig.xman.exam.Exam;

/**
 * 
 * 
 * 
 * @author blutz
 *
 */
public class ExamCourseNode extends AbstractAccessableCourseNode {

	private static final long serialVersionUID = 1L;
		
	public ExamCourseNode() {
		super(Exam.ORES_TYPE_NAME);
		
	}

	/**
	 *@see org.olat.course.nodes.CourseNode#createEditController(UserRequest, WindowControl, ICourse, UserCourseEnvironment) 
	 */
	public TabbableController createEditController(UserRequest ureq,
			WindowControl wControl, ICourse course, UserCourseEnvironment euce) {
		
		ExamCourseNodeEditController eec = new ExamCourseNodeEditController(this.getModuleConfiguration(), ureq, wControl, this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		
		return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, course.getCourseEnvironment().getCourseGroupManager(), euce, eec);

	}

	/**
	 * @see org.olat.course.nodes.CourseNode#createNodeRunConstructionResult(UserRequest, WindowControl, UserCourseEnvironment, NodeEvaluation, String)
	 */
	public NodeRunConstructionResult createNodeRunConstructionResult(
			UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne,
			String nodecmd) {
			return new NodeRunConstructionResult(new ExamCourseNodeRunController(wControl, ureq, this, userCourseEnv.getCourseEnvironment(), ne));
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#isConfigValid(CourseEditorEnv)
	 */
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		//only here we know which translator to take for translating condition error messages
		String translatorStr = Util.getPackageName(ExamCourseNodeEditController.class);
		List sds = isConfigValidWithTranslator(cev, translatorStr,getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#getReferencedRepositoryEntry()
	 */
	public RepositoryEntry getReferencedRepositoryEntry() {
//		"false" because we do not want to be strict, but just indicate whether
		// the reference still exists or not
		RepositoryEntry entry = ExamCourseNodeEditController.getExamRepoReference(getModuleConfiguration(), false);
		return entry;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#isConfigValid()
	 */
	public StatusDescription isConfigValid() {
		/*
		 * first check the one click cache
		 */
		if(oneClickStatusCache!=null) {
			return oneClickStatusCache[0];
		}
		
		StatusDescription sd =  StatusDescription.NOERROR;
		boolean isValid = ExamCourseNodeEditController.isModuleConfigValid(getModuleConfiguration());
		if (!isValid) {
			String shortKey = "examCourseNode.error.noreference.short";
			String longKey = "examCourseNode.error.noreference.long";
			String[] params = new String[] { this.getShortTitle() };
			String translPackage = Util.getPackageName(ExamCourseNodeEditController.class);
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, translPackage);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
			sd.setActivateableViewIdentifier(ExamCourseNodeEditController.PANE_TAB_EXAMCONFIG);
		}
		return sd;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#needsReferenceToARepositoryEntry()
	 */
	public boolean needsReferenceToARepositoryEntry() {
		//exam is a repo entry
		return true;
	}
	
		
	/**
	 * 
	 * @see org.olat.course.nodes.GenericCourseNode#calcAccessAndVisibility(org.olat.course.condition.interpreter.ConditionInterpreter, org.olat.course.run.userview.NodeEvaluation)
	 */
	protected void calcAccessAndVisibility(ConditionInterpreter ci, NodeEvaluation nodeEval) {
		super.calcAccessAndVisibility(ci, nodeEval);
		
	}

}
