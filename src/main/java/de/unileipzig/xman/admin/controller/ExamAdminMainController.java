package de.unileipzig.xman.admin.controller;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.layout.MainLayoutController;

import de.unileipzig.xman.admin.ExamAdminSite;


/**
 * 
 * @author
 */
public class ExamAdminMainController extends MainLayoutBasicController implements MainLayoutController {

	private Translator translator;
	private Panel mainPanel;
	private VelocityContainer vcPage;
	private MenuTree menuTree;
	private LayoutMain3ColsController layout3ColsCtr;
	
	/** this is used to instantiate new VelocityContrainers **/
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(ExamAdminSite.class);
	
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#DefaultController(WindowControl)
	 */
	public ExamAdminMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		// Uebersetzer erzeugen
		translator = Util.createPackageTranslator(ExamAdminSite.class, ureq.getLocale());
		
		// Erzeugen der Navigation (linke Seite)
		menuTree = new MenuTree("examAdminMenuTree");
		TreeModel tm = this.buildTreeModel(ureq);
		menuTree.setTreeModel(tm);
		menuTree.setSelectedNodeId(tm.getRootNode().getIdent());
		menuTree.addListener(this);
		
		vcPage = new VelocityContainer("ExamAdminMainVC", VELOCITY_ROOT + "/index.html", translator, this);
		
		mainPanel = new Panel("content");
		mainPanel.setContent(vcPage);
		
		layout3ColsCtr = new LayoutMain3ColsController(ureq, wControl, menuTree, null, mainPanel, "examAdminMain");
		
		this.putInitialPanel(layout3ColsCtr.getInitialComponent());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#dispose(boolean)
	 */
	protected void doDispose() {
		
		if (layout3ColsCtr != null){
		
			layout3ColsCtr.dispose();
			layout3ColsCtr = null;
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(UserRequest, Component, Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		
		if (source == menuTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeNode selTreeNode = menuTree.getSelectedNode();
				String cmd = (String) selTreeNode.getUserObject();
				mainPanel.setContent(initComponentFromMenuCommand(cmd, ureq));
			} else { // the action was not allowed anymore
				mainPanel.setContent(null); // display an empty field (empty panel)
			}
		} else {
			Tracing.logWarn("Unhandled olatMenuTree event: " + event.getCommand(), ExamAdminMainController.class);
		}
	}
	
	/*
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		
	}
	
	private Component initComponentFromMenuCommand(String uobject, UserRequest ureq) {
		
		if ( uobject.equals("examAdmin") ) {
			layout3ColsCtr.setCol2(null);
			return vcPage;
		}
		else if ( uobject.equals("esf") ) {
			ExamAdminESFController esfCtr = new ExamAdminESFController(ureq, getWindowControl());
			esfCtr.addControllerListener(this);
			layout3ColsCtr.setCol2(esfCtr.getToolController().getInitialComponent());
			return esfCtr.getInitialComponent();
		}
		else if ( uobject.equals("studyPath") ) {
			
			// only non validated esf will be shown
			ExamAdminStudyPathController studyPathCtr = new ExamAdminStudyPathController(ureq, getWindowControl());
			studyPathCtr.addControllerListener(this);
			layout3ColsCtr.setCol2(studyPathCtr.getToolController().getInitialComponent());
			return studyPathCtr.getInitialComponent();
		}
		
		return null;
	}
	
	/**
	 * generates the treemodel
	 * @param ureq the UserRequest
	 * @return the treemodel
	 */
	private TreeModel buildTreeModel(UserRequest ureq){
		
		GenericTreeNode admin, ESF, studyPath;
				
		GenericTreeModel gtm = new GenericTreeModel();
		admin = new GenericTreeNode();		
		admin.setTitle(translator.translate("ExamAdminMainController.menu.examAdmin"));
		admin.setUserObject("examAdmin");
		admin.setAltText(translator.translate("ExamAdminMainController.menu.examAdmin.alt"));
		gtm.setRootNode(admin);

		studyPath = new GenericTreeNode();
		studyPath.setTitle(translator.translate("ExamAdminMainController.menu.esf.studyPath"));
		studyPath.setUserObject("studyPath");
		studyPath.setAltText(translator.translate("ExamAdminMainController.menu.esf.studyPath.alt"));
		admin.addChild(studyPath);
		
		ESF = new GenericTreeNode();
		ESF.setTitle(translator.translate("ExamAdminMainController.menu.esf"));
		ESF.setUserObject("esf");
		ESF.setAltText(translator.translate("ExamAdminMainController.menu.esf.alt"));
		admin.addChild(ESF);
		
		return gtm;
	}
}
