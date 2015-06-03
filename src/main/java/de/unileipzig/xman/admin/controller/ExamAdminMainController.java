package de.unileipzig.xman.admin.controller;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.core.gui.control.controller.MainLayoutBasicController;

import de.unileipzig.xman.admin.ExamAdminSite;

public class ExamAdminMainController extends MainLayoutBasicController {
	private MenuTree menuTree;
	private LayoutMain3ColsController mainLayout;
	private Controller currentSite;

	public ExamAdminMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		setTranslator(Util.createPackageTranslator(ExamAdminSite.class, getLocale()));

		TreeModel tm = buildTreeModel(ureq);
		menuTree = new MenuTree("examAdminMenuTree");
		menuTree.setTreeModel(tm);
		menuTree.setRootVisible(false);
		menuTree.setSelectedNodeId(tm.getRootNode().getChildAt(0).getIdent());
		menuTree.addListener(this);

		mainLayout = new LayoutMain3ColsController(ureq, wControl, menuTree, null, null, "examAdminMain");
		putInitialPanel(mainLayout.getInitialComponent());

		activateSite("studyPath", ureq);
	}

	@Override
	protected void doDispose() {
		removeAsListenerAndDispose(currentSite);
		if(mainLayout != null){
			mainLayout.dispose();
			mainLayout = null;
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == menuTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeNode selTreeNode = menuTree.getSelectedNode();
				String cmd = (String) selTreeNode.getUserObject();
				activateSite(cmd, ureq);
			}
		}
	}

	private void activateSite(String cmd, UserRequest ureq) {
		if(cmd.equals("esf")) {
			ExamAdminESFController esfCtr = new ExamAdminESFController(ureq, getWindowControl());
			mainLayout.setCol3(esfCtr.getInitialComponent());

			removeAsListenerAndDispose(currentSite);
			currentSite = esfCtr;
			listenTo(currentSite);
		} else if(cmd.equals("studyPath")) {
			ExamAdminStudyPathController studyPathCtr = new ExamAdminStudyPathController(ureq, getWindowControl());
			mainLayout.setCol3(studyPathCtr.getInitialComponent());

			removeAsListenerAndDispose(currentSite);
			currentSite = studyPathCtr;
			listenTo(currentSite);
		} else if(cmd.equals("archive")) {
			AdminArchiveController archiveCtr = new AdminArchiveController(ureq, getWindowControl());
			mainLayout.setCol2(null);
			mainLayout.setCol3(archiveCtr.getInitialComponent());

			removeAsListenerAndDispose(currentSite);
			currentSite = archiveCtr;
			listenTo(archiveCtr);
		}
	}

	private TreeModel buildTreeModel(UserRequest ureq){
		GenericTreeNode root, node;

		GenericTreeModel treeModel = new GenericTreeModel();
		root = new GenericTreeNode();
		treeModel.setRootNode(root);

		node = new GenericTreeNode();
		node.setTitle(translate("ExamAdminMainController.menu.esf.studyPath"));
		node.setUserObject("studyPath");
		node.setAltText(translate("ExamAdminMainController.menu.esf.studyPath.alt"));
		root.addChild(node);

		node = new GenericTreeNode();
		node.setTitle(translate("ExamAdminMainController.menu.esf"));
		node.setUserObject("esf");
		node.setAltText(translate("ExamAdminMainController.menu.esf.alt"));
		root.addChild(node);

		node = new GenericTreeNode();
		node.setTitle(translate("ExamAdminMainController.menu.archive"));
		node.setUserObject("archive");
		node.setAltText(translate("ExamAdminMainController.menu.archive.alt"));
		root.addChild(node);

		return treeModel;
	}
}
