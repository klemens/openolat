package de.unileipzig.xman.exam.controllers;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.IAddController;
import org.olat.repository.controllers.RepositoryAddCallback;

import de.unileipzig.xman.catalog.controller.ExamCatalogController;
import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.exam.ExamDBManager;
import de.unileipzig.xman.exam.forms.ChooseExamAttrForm;
import de.unileipzig.xman.module.ModuleManager;

/**
 * 
 * @author
 */
public class ExamCreateController extends DefaultController implements
		IAddController {

	private static final String VELOCITY_ROOT = Util
			.getPackageVelocityRoot(Exam.class);
	private Translator translator;

	private Exam newExam;
	private TabbedPane tabbedPane;
	private VelocityContainer vcAttr;
	private ChooseExamAttrForm chooseExamAttrForm;
	private RepositoryAddCallback addCallback;

	/**
	 * creates the controller for the tabbed pane
	 * 
	 * @param addCallback
	 *            the repositoryAddCallback
	 * @param ureq
	 *            the UserRequest
	 * @param wControl
	 *            the window control
	 */
	public ExamCreateController(RepositoryAddCallback addCallback,
			UserRequest ureq, WindowControl wControl) {
		super(wControl);

		translator = Util.createPackageTranslator(Exam.class, ureq.getLocale());

		tabbedPane = new TabbedPane("examCreatePane", ureq.getLocale());

		vcAttr = new VelocityContainer("vcAttr", VELOCITY_ROOT
				+ "/chooseAttr.html", translator, this);

		chooseExamAttrForm = new ChooseExamAttrForm(ureq, wControl,
				"chooseExamAttrForm", translator);
		chooseExamAttrForm.addControllerListener(this);

		vcAttr.put("chooseExamAttrForm", chooseExamAttrForm
				.getInitialComponent());

		tabbedPane.addTab(translator
				.translate("ExamCreateController.pane.attributes"), vcAttr);

		this.addCallback = addCallback;
	}

	/**
	 * nothing to do here
	 */
	protected void doDispose() {

		// nothing to do here
	}

	/**
	 * @see DefaultController#event(UserRequest, Component, Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// TODO Auto-generated method stub

	}

	public void event(UserRequest ureq, Controller source, Event event) {

		if (source == chooseExamAttrForm) {
			if (event == Form.EVNT_VALIDATION_OK) {
				// create and persist exam
				newExam = ExamDBManager.getInstance().createExam();
				String choice = chooseExamAttrForm.getType();
				if (choice == (Exam.EXAM_TYPE_ORAL))
					newExam.setIsOral(true);
				else
					newExam.setIsOral(false);
				newExam.setModule(ModuleManager.getInstance().findModuleByName(
						chooseExamAttrForm.getModule()));
				newExam.setComments("");
				newExam.setIdentity(ureq.getIdentity());

				ExamDBManager.getInstance().saveExam(newExam);
				Exam exam = ExamDBManager.getInstance().findExamByID(
						newExam.getKey());

				// return to the create dialog
				if (addCallback != null) {
					addCallback.setResourceable(ExamDBManager.getInstance()
							.findExamByID(exam.getKey()));
					addCallback.setDisplayName(translator
							.translate(ExamDBManager.getInstance()
									.findExamByID(exam.getKey())
									.getResourceableTypeName()));
					addCallback.setResourceName(Exam.ORES_TYPE_NAME);
					addCallback.finished(ureq);
				}
			}
		}
	}

	/**
	 * @return the tabbed pane
	 */
	public Component getTransactionComponent() {

		return tabbedPane;
	}

	/**
	 * nothing to do here
	 */
	public void repositoryEntryCreated(RepositoryEntry re) {

		// TODO called after exam has been created
	}

	/**
	 * deletes the exam if the transaction was aborted
	 */
	public void transactionAborted() {

		if (newExam != null)
			ExamDBManager.getInstance().deleteExam(newExam);
	}

	/**
	 * @return true
	 */
	public boolean transactionFinishBeforeCreate() {

		return true;
	}

}