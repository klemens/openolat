package de.unileipzig.xman.exam;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.modules.glossary.GlossaryMainController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.CourseFactory;
import org.olat.course.repository.CreateNewCourseController;
import org.olat.course.repository.ImportCourseController;
import org.olat.modules.glossary.GlossaryManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.IAddController;
import org.olat.repository.controllers.RepositoryAddCallback;
import org.olat.repository.controllers.WizardCloseResourceController;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.references.ReferenceImpl;
import org.olat.resource.references.ReferenceManager;

import de.unileipzig.xman.admin.mail.MailManager;
import de.unileipzig.xman.appointment.AppointmentManager;
import de.unileipzig.xman.calendar.CalendarManager;
import de.unileipzig.xman.exam.controllers.ExamCreateController;
import de.unileipzig.xman.exam.controllers.ExamEditorController;
import de.unileipzig.xman.exam.controllers.ExamLaunchController;
import de.unileipzig.xman.protocol.Protocol;
import de.unileipzig.xman.protocol.ProtocolManager;

public class ExamHandler implements RepositoryHandler {

	private static final String PACKAGE = Util.getPackageName(RepositoryManager.class);

	public static final String PROCESS_CREATENEW = "new";
	
	private final boolean LAUNCHEABLE = true;
	private final boolean DOWNLOADEABLE = false;
	private final boolean EDITABLE = true;
	private static final List<Object> supportedTypes;
	
	public ExamHandler() {
		// singleton
	}
	
	static { // initialize supported types
		supportedTypes = new ArrayList<Object>(1);
		supportedTypes.add(Exam.ORES_TYPE_NAME);
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getSupportedTypes()
	 */
	@Override
	public List getSupportedTypes() {
		return supportedTypes;
	}
	
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#archive(java.lang.String, org.olat.repository.RepositoryEntry)
	 */
	@Override
	public String archive(Identity archiveOnBehalfOf, String archivFilePath, RepositoryEntry repoEntry) {
		return null;
	}

	/**
	 * (non-Javadoc)
	 * @see org.olat.repository.handlers.RepositoryHandler#cleanupOnDelete(org.olat.core.id.OLATResourceable, org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public boolean cleanupOnDelete(OLATResourceable res) {
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new OLATResourceableJustBeforeDeletedEvent(res), res);
		
		// delete OLATResourcable
		OLATResourceManager rm = OLATResourceManager.getInstance();
		OLATResource ores = rm.findResourceable(res);
		rm.deleteOLATResource(ores);
		
		Exam deleteableExam = ExamDBManager.getInstance().findExamByID(res.getResourceableId());
		List<Protocol> protoList = ProtocolManager.getInstance().findAllProtocolsByExam(deleteableExam);
		
		// delete Protocols and calendar events
		for ( Protocol p : protoList ) {
			
			CalendarManager.getInstance().deleteKalendarEventForExam(deleteableExam, p.getIdentity());
			
			Locale userLocale = new Locale(p.getIdentity().getUser().getPreferences().getLanguage());
			Translator tmpTranslator = new PackageTranslator(Util.getPackageName(Exam.class), userLocale);
			// Email DeleteExam
			MailManager.getInstance().sendEmail(
				tmpTranslator.translate("ExamHandler.DeleteExam.Subject", new String[] { deleteableExam.getName() }),
				
				tmpTranslator.translate("ExamHandler.DeleteExam.Body", new String[] { deleteableExam.getName() }),
					
				p.getIdentity()
			);
			ProtocolManager.getInstance().deleteProtocol(p);
		}
		
		protoList = null;
		
		// delete all appointments
		AppointmentManager.getInstance().deleteAllAppointmentsByExam(deleteableExam);
		
		// delete the exam
		ExamDBManager.getInstance().deleteExam(deleteableExam);
		
		deleteableExam = null;
		
		return true;
	}

	/**
	 * (non-Javadoc)
	 * @see org.olat.repository.handlers.RepositoryHandler#createCopy(org.olat.core.id.OLATResourceable, org.olat.core.gui.UserRequest)
	 */
	@Override
	public OLATResourceable createCopy(OLATResourceable res, UserRequest ureq) {
		
		Exam oldExam = ExamDBManager.getInstance().findExamByID(res.getResourceableId());
		Exam newExam = new ExamImpl();
		
		newExam.setComments(oldExam.getComments());
		newExam.setEarmarkedEnabled(oldExam.getEarmarkedEnabled());
		newExam.setIsOral(oldExam.getIsOral());
		newExam.setName(oldExam.getName());
		newExam.setIdentity(ureq.getIdentity()); // set authorship to the copying user
		
		ExamDBManager.getInstance().saveExam(newExam);
		
		return ExamDBManager.getInstance().findExamByID(newExam.getKey());
	}

	/**
	 * (non-Javadoc)
	 * @see org.olat.repository.handlers.RepositoryHandler#getAsMediaResource(org.olat.core.id.OLATResourceable)
	 */
	@Override
	public MediaResource getAsMediaResource(OLATResourceable res, boolean backwardsCompatible) {
		throw new AssertException("getAsMediaResource not implemented for Exam");
	}

	/**
	 * (non-Javadoc)
	 * @see org.olat.repository.handlers.RepositoryHandler#readyToDelete(org.olat.core.id.OLATResourceable, org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public boolean readyToDelete(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		
		ReferenceManager refM = ReferenceManager.getInstance();
		String referencesSummary = refM.getReferencesToSummary(res, ureq.getLocale());
		if (referencesSummary != null) {
			Translator translator = new PackageTranslator(PACKAGE, ureq.getLocale());
			wControl.setError(translator.translate("details.delete.error.references",
					new String[] { referencesSummary }));
			return false;
		}
		return true;
	}

	/**
	 * (non-Javadoc)
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsDownload()
	 */
	@Override
	public boolean supportsDownload(RepositoryEntry repoEntry) {
		
		return this.DOWNLOADEABLE;
	}

	/**
	 * (non-Javadoc)
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsEdit()
	 */
	@Override
	public boolean supportsEdit(RepositoryEntry repoEntry) {
		
		return this.EDITABLE;
	}

	/**
	 * (non-Javadoc)
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsLaunch()
	 */
	@Override
	public boolean supportsLaunch(RepositoryEntry repoEntry) {
		
		return this.LAUNCHEABLE;
	}
	
	@Override
	public boolean supportsWizard(RepositoryEntry repoEntry) {
		return false;
	}
	
	@Override
	public LockResult acquireLock(OLATResourceable ores, Identity identity) {
		return null;
	}
	
	@Override
	public void releaseLock(LockResult lockResult) {}

	@Override
	public MainLayoutController createLaunchController(OLATResourceable res, UserRequest ureq, WindowControl wControl) {

		// check roles
		boolean isOLATAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
		boolean isOLATUser = !ureq.getUserSession().getRoles().isGuestOnly();
		boolean isInstitutionalResourceManager = ureq.getUserSession().getRoles().isInstitutionalResourceManager();
		boolean isResourceOwner = false;
		if ( isOLATAdmin || isInstitutionalResourceManager  ) isResourceOwner = true;
		else {
			RepositoryManager repoMgr = RepositoryManager.getInstance();
			isResourceOwner = repoMgr.isOwnerOfRepositoryEntry(ureq.getIdentity(), repoMgr.lookupRepositoryEntry(res, true));
		}
		Exam exam = ExamDBManager.getInstance().findExamByID(res.getResourceableId());
		ExamLaunchController examLaunchCtr = new ExamLaunchController(ureq, wControl, exam, isResourceOwner, isOLATUser);
		RepositoryManager.getInstance().incrementLaunchCounter(RepositoryManager.getInstance().lookupRepositoryEntry(res, false));
		return examLaunchCtr;
		/**
		final boolean isResourceOwner = ureq.getUserSession().getRoles().isInstitutionalResourceManager();		// später eventuell ändern falls die Rolle nicht passt
		final boolean isOLATUser = !ureq.getUserSession().getRoles().isGuestOnly();								// hier auch nochmal schaun ;D

		final ExamLaunchController elc = new ExamLaunchController(ureq, wControl, null, isResourceOwner, isOLATUser);
		final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, null, null, elc.getInitialComponent(), null);
		layoutCtr.addDisposableChildController(elc);
		return layoutCtr;
		**/
	}

	@Override
	public Controller createEditorController(OLATResourceable res, UserRequest ureq, WindowControl wControl) {

		
		ExamEditorController examEditorCtr = new ExamEditorController(ureq, wControl, res);
		return examEditorCtr;
		/**
		final ExamEditorController elc = new ExamEditorController(ureq, wControl, res);
		final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, null, null, elc.getInitialComponent(), null);
		layoutCtr.addDisposableChildController(elc);
		return layoutCtr;
		**/
	}

	@Override
	public Controller createWizardController(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		throw new AssertException("createWizardController not implemented for Exam");
	}

	@Override
	public WizardCloseResourceController createCloseResourceController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		throw new AssertException("createCloseResourceController not implemented for Exam");
	}

	@Override
	public IAddController createAddController(RepositoryAddCallback callback, Object userObject, UserRequest ureq, WindowControl wControl) {
		if (userObject == null || userObject.equals(PROCESS_CREATENEW)) {
			return new ExamCreateController(callback, ureq, wControl);
		} 
		else 
			throw new AssertException("Command " + userObject + " not supported by ExamHandler.");
		
	}

	@Override
	public Controller createDetailsForm(UserRequest ureq, WindowControl wControl, OLATResourceable res) {
		return null;
		// throw new AssertException("createDetailsForm not implemented for Exam");
	}

	@Override
	public boolean isLocked(OLATResourceable ores) {
		return false;
	}

	
	/** veraltet / unused
	 * 
	 * (non-Javadoc)
	 * @see org.olat.repository.handlers.RepositoryHandler#getDetailsComponent(org.olat.core.id.OLATResourceable, org.olat.core.gui.UserRequest)
	
	public Component getDetailsComponent(OLATResourceable res, UserRequest ureq) {

		return null;
	}
	**/
	
}
