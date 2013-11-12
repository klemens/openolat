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

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(ElectronicStudentFile.class);

	public static final String CHANGE_EVENT = "esf_changend";
	public static final String VALIDATE_EVENT = "esf_validated";

	private ESFCreateForm esfCreateForm;
	private String action;
	private User user;

	private VelocityContainer vcMain;

	public ESFCreateController(UserRequest ureq, WindowControl wControl, Translator translator, User user, String title, String action) {
		super(ureq, wControl);
		
		this.user = user;
		this.action = action;
		setTranslator(translator);

		vcMain = new VelocityContainer("esfCreate", VELOCITY_ROOT + "/esf-create.html", translator, this);
		vcMain.contextPut("title", title);

		esfCreateForm = new ESFCreateForm(ureq, wControl, "esfCreateForm", translator, user);
		listenTo(esfCreateForm);
		vcMain.put("esfCreateForm", esfCreateForm.getInitialComponent());

		putInitialPanel(vcMain);
	}

	@Override
	protected void doDispose() {
		removeAsListenerAndDispose(esfCreateForm);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == esfCreateForm) {
			if (event == Form.EVNT_FORM_CANCELLED) {
				// user canceled dialog
				fireEvent(ureq, Event.CANCELLED_EVENT);
			} else if (event == Form.EVNT_VALIDATION_OK) {
				if (action.equals(ESFLaunchController.CHANGE_ESF)) {
					String oldFirstName = user.getProperty(UserConstants.FIRSTNAME, ureq.getLocale());
					String oldLastName = user.getProperty(UserConstants.LASTNAME, ureq.getLocale());
					String oldStudyPath = user.getProperty(UserConstants.STUDYSUBJECT, ureq.getLocale());
					
					updateUserInformation(user);

					String newFirstName = esfCreateForm.getFirstName();
					String newLastName = esfCreateForm.getLastName();
					String newStudyPath = esfCreateForm.getStudyPath();

					// load the esf for the user
					ElectronicStudentFile esf = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(ureq.getIdentity());

					// create a comment to record the changed user data in esf
					String[] data = { new Date().toString(), oldLastName, oldFirstName, oldStudyPath, newLastName, newFirstName, newStudyPath };
					CommentManager.getInstance().createCommentInEsf(esf, translate("ESFCreateController.changed", data), ureq.getIdentity());
					
					// save the changed esf
					ElectronicStudentFileManager.getInstance().updateElectronicStundentFile(esf);

					// inform listeners of changed esf
					fireEvent(ureq, new Event(ESFCreateController.CHANGE_EVENT));
				} else if (action.equals(ESFLaunchController.CREATE_ESF)) {
					updateUserInformation(user);

					// create new esf
					ElectronicStudentFile esf = ElectronicStudentFileManager.getInstance().createElectronicStudentFileForStudent(ureq.getIdentity());

					// try to persist the esf
					try {
						ElectronicStudentFileManager.getInstance().persistElectronicStudentFile(esf);
						
						String firstName = esfCreateForm.getFirstName();
						String lastName = esfCreateForm.getLastName();
						String studyPath = esfCreateForm.getStudyPath();
						
						// create first comment with initial personal data chosen
						String[] data = { new Date().toString(), firstName, lastName, studyPath };
						CommentManager.getInstance().createCommentInEsf(esf, translate("ESFCreateController.created", data), ureq.getIdentity());
						
						// save the changed esf
						ElectronicStudentFileManager.getInstance().updateElectronicStundentFile(esf);
					} catch (DuplicateObjectException e) {
						// this should not happen!
						showWarning("ESFCreateController.ESF.already.available");
					}
					
					// inform listeners of new esf
					fireEvent(ureq, new Event(ESFCreateController.VALIDATE_EVENT));
				}
			}
		}
	}

	private void updateUserInformation(User user) {
		User updateUser = UserManager.getInstance().loadUserByKey(user.getKey());
		
		// update the user's information in his profile
		updateUser.setProperty(UserConstants.FIRSTNAME, esfCreateForm.getFirstName());
		updateUser.setProperty(UserConstants.LASTNAME, esfCreateForm.getLastName());
		updateUser.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, esfCreateForm.getInstitutionalIdentifier());
		updateUser.setProperty(UserConstants.INSTITUTIONALEMAIL, esfCreateForm.getInstitutionalEmail());
		updateUser.setProperty(UserConstants.STUDYSUBJECT, esfCreateForm.getStudyPath());
		
		// save changes
		UserManager.getInstance().updateUser(updateUser);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to do
	}
}
