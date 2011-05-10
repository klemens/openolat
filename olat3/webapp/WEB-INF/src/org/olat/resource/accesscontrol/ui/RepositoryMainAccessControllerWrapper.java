package org.olat.resource.accesscontrol.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.accesscontrol.ACUIFactory;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.manager.ACFrontendManager;

/**
 * 
 * Description:<br>
 *  It's a wrapper to manage the acces to repository entries
 * 
 * <P>
 * Initial Date:  9 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RepositoryMainAccessControllerWrapper extends MainLayoutBasicController {

	private final Panel contentP;
	private VelocityContainer mainVC;
	private Controller accessController;
	private final MainLayoutController resController;

	public RepositoryMainAccessControllerWrapper(UserRequest ureq, WindowControl wControl, OLATResourceable res, MainLayoutController resController) {
		super(ureq, wControl);
		
		contentP = new Panel("wrapperPanel");
		this.resController = resController;

		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(res, false);
		
		ACFrontendManager acFrontendManager = (ACFrontendManager)CoreSpringFactory.getBean("acFrontendManager");
		AccessResult acResult = acFrontendManager.isAccessible(re, getIdentity(), false);
		if(acResult.isAccessible()) {
			contentP.setContent(resController.getInitialComponent());
		} else if (re != null && acResult.getAvailableMethods().size() > 0) {
			accessController = ACUIFactory.createAccessController(ureq, getWindowControl(), acResult.getAvailableMethods());
			listenTo(accessController);
			mainVC = createVelocityContainer("access_wrapper");
			mainVC.put("accessPanel", accessController.getInitialComponent());
			contentP.setContent(mainVC);
		} else {
			wControl.setWarning(translate("course.closed"));
		}
		putInitialPanel(contentP);
	}
	
	protected void openContent(UserRequest ureq) {
		contentP.setContent(resController.getInitialComponent());
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == resController) {
			fireEvent(ureq, event);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == accessController) {
			if(event.equals(AccessEvent.ACCESS_OK_EVENT)) {
				openContent(ureq);
				removeAsListenerAndDispose(accessController);
				accessController = null;
			} else if(event.equals(AccessEvent.ACCESS_FAILED_EVENT)) {
				String msg = ((AccessEvent)event).getMessage();
				if(StringHelper.containsNonWhitespace(msg)) {
					getWindowControl().setError(msg);
				} else {
					showError("error.accesscontrol");
				}
			}
		}
	}
}