package de.htwk.autolat.BBautOLAT;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Configuration.ConfigurationManagerImpl;

public class EditConnectionController extends BasicController {
	
	private static final String PACKAGE = Util.getPackageName(EditConnectionController.class);

	private Configuration conf;
	
	//GUI
	private EditConnectionForm editConnectionForm;
	private Panel main;
	private VelocityContainer mainvc;
	private Link editLink;
	
	private CloseableModalController CMCEditAllConnections;
	private CMCEditAllConnectionsController CMCInlayCtr;
	
	public EditConnectionController(UserRequest ureq, WindowControl control, long courseID, long courseNodeID) {
		super(ureq, control);
		
		conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, courseNodeID);
		
		//PackageTranslator translator = new PackageTranslator(PACKAGE, this.getLocale());
		//setTranslator(translator);
		main = new Panel("mainPanel");
		mainvc = this.createVelocityContainer("editConnectionController");
		
		/* -- autolat8 */
		//main = new Panel("editConnectionPanel");
		//editConnectionForm = new EditConnectionForm(EditConnectionForm.NAME, getTranslator(), false, courseNodeID, null);
		editConnectionForm = new EditConnectionForm(ureq, control, false, courseID, courseNodeID, null);
		listenTo(editConnectionForm);
		//mainvc = createVelocityContainer("editConnectionController");
		mainvc.put("editConnectionForm", editConnectionForm.getInitialComponent());
		//*/
		
		editLink = LinkFactory.createButton("label.controller.editconnection.edit", mainvc, this);
		mainvc.put("editConnectionsLink", editLink);
		main.setContent(mainvc);
		putInitialPanel(main);
	} 

	@Override
	protected void doDispose() {
	// TODO Auto-generated method stub

	}

	@Override
	protected void event(UserRequest ureq, Component comp, Event evnt) {
	
		if(comp == editLink) {
			CMCInlayCtr = new CMCEditAllConnectionsController(ureq, getWindowControl());
			CMCInlayCtr.addControllerListener(this);
			CMCEditAllConnections = new CloseableModalController(getWindowControl(), 
					translate("label.controller.cmceditallconnections.close"), CMCInlayCtr.getInitialComponent());
			CMCEditAllConnections.addControllerListener(this);
			CMCEditAllConnections.activate();
		}
		//*/

	}
	
	@Override
	protected void event(UserRequest ureq, Controller ctr, Event evnt) {
		
		if(ctr == editConnectionForm) {
			if(evnt.equals(FormEvent.DONE_EVENT)) {
				fireEvent(ureq, Event.DONE_EVENT);
			}			
		}

		if(ctr == CMCEditAllConnections) {			
			if(evnt.equals(CloseableModalController.CLOSE_MODAL_EVENT)) {
				//CMCEditAllConnections.deactivate();
			}
		}
	}

}
