package de.unileipzig.xman.esf.controller;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.olat.user.UserManager;

import de.unileipzig.xman.comment.CommentEntry;
import de.unileipzig.xman.comment.CommentManager;
import de.unileipzig.xman.esf.DuplicateObjectException;
import de.unileipzig.xman.esf.ElectronicStudentFile;
import de.unileipzig.xman.esf.ElectronicStudentFileManager;
import de.unileipzig.xman.esf.form.ESFCreateForm;

/**
 * 
 * Description:<br>
 * TODO: gerb Class Description for ESFCreateController
 * 
 * <P>
 * Initial Date: 26.05.2008 <br>
 * 
 * @author gerb
 */
public class ESFCreateController extends BasicController {

	private static final String VELOCITY_ROOT = Util
			.getPackageVelocityRoot(ElectronicStudentFile.class);

	public static final String CHANGE_EVENT = "esf_changend";
	public static final String VALIDATE_EVENT = "esf_validated";

	private ESFCreateForm esfCreateForm;
	private Translator translator;
	private String action;
	private User user;

	private VelocityContainer vcMain;

	/**
	 * 
	 * @param control
	 * @param translator
	 * @param identity
	 * @param title
	 */
	public ESFCreateController(UserRequest ureq, WindowControl wControl,
			Translator translator, User user, String title,
			String action) {
		super(ureq, wControl);

		vcMain = new VelocityContainer("esfCreate", VELOCITY_ROOT
				+ "/esf-create.html", translator, this);
		vcMain.contextPut("title", title);

		this.user = user;
		this.action = action;
		this.translator = translator;
		this.esfCreateForm = new ESFCreateForm(ureq, wControl, "esfCreateForm",
				translator, user);
		this.esfCreateForm.addControllerListener(this);

		vcMain.put("esfCreateForm", esfCreateForm.getInitialComponent());
		this.putInitialPanel(vcMain);
		// this.setInitialComponent(this.vcMain);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	protected void doDispose() {

		this.esfCreateForm = null;
	}

	/**
	 * 
	 * 
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	protected void event(UserRequest ureq, Controller source, Event event) {

		if (source == esfCreateForm) {

			if (event == Form.EVNT_FORM_CANCELLED)
				fireEvent(ureq, Event.CANCELLED_EVENT);

			if (event == Form.EVNT_VALIDATION_OK) {
				ElectronicStudentFile esf;

				// if someone wants to change his esf, it should be set to
				// invalidated
				if (action.equals(ESFLaunchController.CHANGE_ESF)) {
					
					String name = this.getIdentity().getName();
					
					String oldFirstName = this.getIdentity().getUser().getProperty(UserConstants.FIRSTNAME, ureq.getLocale());
					String oldLastName = this.getIdentity().getUser().getProperty(UserConstants.LASTNAME, ureq.getLocale());
					String oldStudyPath = this.getIdentity().getUser().getProperty(UserConstants.STUDYSUBJECT, ureq.getLocale());
					
					this.updateUserInformation(user);

					String newFirstName = esfCreateForm.getFirstName();
					String newLastName = esfCreateForm.getLastName();
					String newStudyPath = esfCreateForm.getStudyPath();

					// set esf to not validated
					esf = ElectronicStudentFileManager.getInstance()
							.retrieveESFByIdentity(ureq.getIdentity());

					// create a comment for change of esf
					// the comment
					String[] comment = { new Date().toString(), oldLastName, oldFirstName, oldStudyPath, newLastName, newFirstName, newStudyPath };
					String entry = translator.translate(
							"ESFCreateController.changed", comment);

					CommentEntry commentEntry = CommentManager.getInstance()
							.createCommentEntry();
					commentEntry.setAuthor(ureq.getIdentity());
					commentEntry.setComment(entry);

					// add the commentEntry
					esf.addCommentEntry(commentEntry);

					ElectronicStudentFileManager.getInstance()
							.updateElectronicStundentFile(esf);
					this.fireEvent(ureq, new Event(
							ESFCreateController.CHANGE_EVENT));
					return;
				}
				// someone wants to validate/create his esf
				if (action.equals(ESFLaunchController.CREATE_ESF)) {

					this.updateUserInformation(user);

					esf = ElectronicStudentFileManager.getInstance()
							.createElectronicStudentFileForStudent(ureq.getIdentity());

					// try to persist the esf
					try {

						ElectronicStudentFileManager.getInstance()
								.persistElectronicStudentFile(esf);
					} catch (DuplicateObjectException e) {

						this
								.getWindowControl()
								.setWarning(
										this.translator
												.translate("ESFCreateController.ESF.already.available"));
					}
					this.fireEvent(ureq, new Event(
							ESFCreateController.VALIDATE_EVENT));
					return;
				}
			}
		}
	}

	private void updateUserInformation(User user) {

		User updateUser = UserManager.getInstance()
				.loadUserByKey(user.getKey());
		// update the user's information in his profile
		updateUser.setProperty(UserConstants.FIRSTNAME, esfCreateForm
				.getFirstName());
		updateUser.setProperty(UserConstants.LASTNAME, esfCreateForm
				.getLastName());
		updateUser.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER,
				esfCreateForm.getInstitutionalIdentifier());
		updateUser.setProperty(UserConstants.INSTITUTIONALEMAIL, esfCreateForm
				.getInstitutionalEmail());
		updateUser.setProperty(UserConstants.STUDYSUBJECT, esfCreateForm
				.getStudyPath());
		UserManager.getInstance().updateUser(updateUser);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// TODO Auto-generated method stub

	}
}
