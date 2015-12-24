/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.bc;

import java.io.File;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.BCCourseNode;

/**
 * Initial Date: Apr 28, 2004
 *
 * @author gnaegi
 */
public class BCCourseNodeEditForm extends FormBasicController implements ControllerEventListener{

	private SingleSelection folderTargetChoose;
	private FormLink chooseFolder;
	private StaticTextElement subPath;
	private BCCourseNode node;
	private ICourse course;
	private CloseableModalController cmc;
	private FormLink createFolder;
	private BCCourseNodeEditCreateFolderForm createFolderForm;
	private FormItem sharedFolderWarning, sharedFolderInfo;
	private BCCourseNodeEditChooseFolderForm chooseForm;

	public BCCourseNodeEditForm(UserRequest ureq, WindowControl wControl, BCCourseNode bcNode, ICourse course) {
		super(ureq, wControl);
		node = bcNode;
		this.course = course;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		String[] keys = {"autoPath", "pathChoose"};
		String[] values= {translate("pathChoose.auto"), translate("pathChoose.custom")};
		folderTargetChoose = uifactory.addRadiosVertical("pathChoose", formLayout, keys, values);

		folderTargetChoose.addActionListener(FormEvent.ONCLICK);
		subPath = uifactory.addStaticTextElement("subPathLab.label", translate("subPathLab.dummy"), formLayout);

		sharedFolderInfo = uifactory.addStaticExampleText("warning","", "<div class=\"o_important\">"+translate("info.sharedfolder")+"</div>",formLayout);
		sharedFolderWarning = uifactory.createSimpleErrorText("warning", translate("warning.no.sharedfolder"));
		formLayout.add(sharedFolderWarning);

		boolean isAuto = node.getModuleConfiguration().getBooleanSafe(BCCourseNodeEditController.CONFIG_AUTO_FOLDER);

		if(isAuto){
			folderTargetChoose.select("autoPath", true);
			subPath.setVisible(false);
			sharedFolderWarning.setVisible(false);
		}else{
			folderTargetChoose.select("pathChoose", false);
			String subpath = node.getModuleConfiguration().getStringValue(BCCourseNodeEditController.CONFIG_SUBPATH);

			if(subpath != ""){
				subPath.setValue(subpath);
			}
			subPath.setVisible(true);

			if(isSharedfolderNotPresent()){
				sharedFolderWarning.setVisible(true);
			}else{
				sharedFolderWarning.setVisible(false);
			}


		}
		if(node.isSharedFolder()){
			sharedFolderInfo.setVisible(true);
		}else{
			sharedFolderInfo.setVisible(false);
		}

		FormLayoutContainer buttons2Cont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttons2Cont);

		chooseFolder = uifactory.addFormLink("chooseFolder", buttons2Cont, Link.BUTTON);
		chooseFolder.setVisible(folderTargetChoose.isSelected(1));

		createFolder = uifactory.addFormLink("createFolder", buttons2Cont, Link.BUTTON);
		createFolder.setVisible(folderTargetChoose.isSelected(1));
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(folderTargetChoose.isSelected(0)){
			sharedFolderWarning.setVisible(false);
		}else{
			if(isSharedfolderNotPresent()){
				sharedFolderWarning.setVisible(true);
			}else{
				sharedFolderWarning.setVisible(false);
			}
			if(node.isSharedFolder()){
				sharedFolderInfo.setVisible(true);
			}else{
				sharedFolderInfo.setVisible(false);
			}
		}
		if(source == folderTargetChoose){
			subPath.setVisible(folderTargetChoose.isSelected(1));
			chooseFolder.setVisible(folderTargetChoose.isSelected(1));
			createFolder.setVisible(folderTargetChoose.isSelected(1));
			if(folderTargetChoose.isSelected(1)){
				node.getModuleConfiguration().setBooleanEntry(BCCourseNodeEditController.CONFIG_AUTO_FOLDER, false);
				String path = node.getModuleConfiguration().getStringValue(BCCourseNodeEditController.CONFIG_SUBPATH);
				if(StringHelper.containsNonWhitespace(path)){
					subPath.setValue(path);
				}else{
					subPath.setValue(translate("subPathLab.dummy"));
				}
			}else{
				node.getModuleConfiguration().setBooleanEntry(BCCourseNodeEditController.CONFIG_AUTO_FOLDER, true);
				node.getModuleConfiguration().setStringValue(BCCourseNodeEditController.CONFIG_SUBPATH, "");
			}
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			createFolder.setVisible(folderTargetChoose.isSelected(1));
		}
		if(source == createFolder){
			createFolderForm = new BCCourseNodeEditCreateFolderForm(ureq, getWindowControl(), course, node);
			listenTo(createFolderForm);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), createFolderForm.getInitialComponent());
			cmc.activate();
		}
		if (source == chooseFolder){
			VFSContainer namedContainer = course.getCourseFolderContainer();

			chooseForm = new BCCourseNodeEditChooseFolderForm(ureq, getWindowControl(), namedContainer);
			listenTo(chooseForm);

			cmc = new CloseableModalController(getWindowControl(), translate("close"),chooseForm.getInitialComponent());
			cmc.activate();
			return;
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == createFolderForm){
			cmc.deactivate();
			String subpath = event.getCommand();
			VFSContainer selectedContainer = (VFSContainer) course.getCourseFolderContainer().resolve(subpath);
			updatePublisher(selectedContainer);
			node.getModuleConfiguration().setStringValue(BCCourseNodeEditController.CONFIG_SUBPATH, subpath);
			subPath.setValue(event.getCommand());
			if(node.isSharedFolder()){
				sharedFolderInfo.setVisible(true);
			}else{
				sharedFolderInfo.setVisible(false);
			}
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		}

		if(source == chooseForm){
			if(event.getCommand().equals("cancel")){
				cmc.deactivate();
			}else{
				cmc.deactivate();
				String subpath = event.getCommand();
				subPath.setValue(subpath);

				VFSContainer selectedContainer = (VFSContainer) course.getCourseFolderContainer().resolve(subpath);
				updatePublisher(selectedContainer);
				node.getModuleConfiguration().setStringValue(BCCourseNodeEditController.CONFIG_SUBPATH, subpath);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
			if(node.isSharedFolder()){
				sharedFolderInfo.setVisible(true);
			}else{
				sharedFolderInfo.setVisible(false);
			}

		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {

		super.event(ureq, source, event);
	}

	private void updatePublisher(VFSContainer container){
		File realFile = VFSManager.getRealFile(container);
		String relPath = new File(FolderConfig.getCanonicalRoot()).toPath().relativize(realFile.toPath()).toString();

		NotificationsManager notifManager = NotificationsManager.getInstance();
		SubscriptionContext nodefolderSubContext = CourseModule.createSubscriptionContext(course.getCourseEnvironment(), node);

		Publisher publisher = notifManager.getPublisher(nodefolderSubContext);
		if (publisher != null) {
			String businessPath = getWindowControl().getBusinessControl().getAsString();
			String data = "/"+relPath;
			PublisherData pdata = new PublisherData(OresHelper.calculateTypeName(BCCourseNode.class), data, businessPath);
			notifManager.updatePublisherData(nodefolderSubContext, pdata);
		}
	}

	@Override
	protected void doDispose() {

	}

	private boolean isSharedfolderNotPresent(){
		if(node.getModuleConfiguration().getStringValue(BCCourseNodeEditController.CONFIG_SUBPATH).startsWith("/_sharedfolder")){
			if(course.getCourseEnvironment().getCourseFolderContainer().resolve("/_sharedfolder/") == null){
				return true;
			}
		}
		return false;
	}

}
