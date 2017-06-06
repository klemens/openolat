package de.unileipzig.xman.admin.controller;

import java.util.List;

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
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeHelper;
import org.olat.util.logging.activity.LoggingResourceable;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;

import de.unileipzig.xman.admin.ExamAdminSite;

public class ExamAdminMainController extends MainLayoutBasicController implements Activateable2 {
	private MenuTree menu;
	private LayoutMain3ColsController mainLayout;

	private ExamAdminESFMainController esfCtr;
	private ExamAdminStudyPathController studyPathCtr;
	private AdminArchiveController archiveCtr;

	public ExamAdminMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		setTranslator(Util.createPackageTranslator(ExamAdminSite.class, getLocale()));

		menu = new MenuTree(null, "examAdminMenuTree", this);
		menu.setTreeModel(buildTreeModel(ureq));
		menu.setRootVisible(false);

		mainLayout = new LayoutMain3ColsController(ureq, getWindowControl(), menu, null, "examAdminMain");
		listenTo(mainLayout);
		putInitialPanel(mainLayout.getInitialComponent());

		activateSite("studyPath", ureq);
	}

	@Override
	protected void doDispose() {
		// auto-dispose for listenTo()
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == menu) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeNode selTreeNode = menu.getSelectedNode();
				String cmd = (String) selTreeNode.getUserObject();
				activateSite(cmd, ureq);
			}
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			activateSite("StudyPath", ureq);
		} else {
			String cmd = entries.get(0).getOLATResourceable().getResourceableTypeName();
			activateSite(cmd, ureq);
		}
	}

	private void activateSite(String cmd, UserRequest ureq) {
		Controller selectedController = null;

		if(cmd.equals("Esf")) {
			if(esfCtr == null) {
				OLATResourceable ores = OresHelper.createOLATResourceableInstance("Esf", 0l);
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
				WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());

				esfCtr = new ExamAdminESFMainController(ureq, bwControl);
				listenTo(esfCtr);
			}
			selectedController = esfCtr;
		} else if(cmd.equals("Archive")) {
			if(archiveCtr == null) {
				OLATResourceable ores = OresHelper.createOLATResourceableInstance("Archive", 0l);
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
				WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());

				archiveCtr = new AdminArchiveController(ureq, bwControl);
				listenTo(archiveCtr);
			}
			selectedController = archiveCtr;
		} else { // default: StudyPath
			if(studyPathCtr == null) {
				OLATResourceable ores = OresHelper.createOLATResourceableInstance("StudyPath", 0l);
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
				WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());

				studyPathCtr = new ExamAdminStudyPathController(ureq, bwControl);
				listenTo(studyPathCtr);
			}
			selectedController = studyPathCtr;
		}

		TreeNode selectedTreeNode = TreeHelper.findNodeByUserObject(cmd, menu.getTreeModel().getRootNode());
		if (selectedTreeNode != null && !selectedTreeNode.getIdent().equals(menu.getSelectedNodeId())) {
			menu.setSelectedNodeId(selectedTreeNode.getIdent());
		}

		mainLayout.setCol3(selectedController.getInitialComponent());
		addToHistory(ureq, selectedController);
	}

	private TreeModel buildTreeModel(UserRequest ureq){
		GenericTreeNode root, node;

		GenericTreeModel treeModel = new GenericTreeModel();
		root = new GenericTreeNode();
		treeModel.setRootNode(root);

		node = new GenericTreeNode();
		node.setTitle(translate("ExamAdminMainController.menu.esf.studyPath"));
		node.setUserObject("StudyPath");
		node.setAltText(translate("ExamAdminMainController.menu.esf.studyPath.alt"));
		root.addChild(node);

		node = new GenericTreeNode();
		node.setTitle(translate("ExamAdminMainController.menu.esf"));
		node.setUserObject("Esf");
		node.setAltText(translate("ExamAdminMainController.menu.esf.alt"));
		root.addChild(node);

		node = new GenericTreeNode();
		node.setTitle(translate("ExamAdminMainController.menu.archive"));
		node.setUserObject("Archive");
		node.setAltText(translate("ExamAdminMainController.menu.archive.alt"));
		root.addChild(node);

		return treeModel;
	}
}
