package de.htwk.autolat.BBautOLAT;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.JDOMException;
import org.olat.core.commons.modules.bc.FileUploadController;
import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
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
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.ICourse;


import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Configuration.ConfigurationManagerImpl;
import de.htwk.autolat.Connector.AutolatConnectorException;
import de.htwk.autolat.Connector.BBautOLATConnector;
import de.htwk.autolat.ServerConnection.ServerConnectionManagerImpl;
import de.htwk.autolat.TaskConfiguration.TaskConfigurationManagerImpl;
import de.htwk.autolat.TaskInstance.TaskInstanceManagerImpl;
import de.htwk.autolat.TaskType.TaskTypeManagerImpl;
import de.htwk.autolat.tools.ImportExport.AutOlatNodeExporter;
import de.htwk.autolat.tools.ImportExport.AutOlatNodeImporter;

/**
 * Controller to edit an auto tool task
 * 
 * <P>
 * @author werjo
 *
 */
public class EditTaskController extends BasicController {

	private static final String PACKAGE = Util.getPackageName(EditTaskController.class);
	
	private CloseableModalController fileUploadCMCCtr;
	private FileUploadController fileUploadCtr;
	private LocalFolderImpl uploadContainer;
	private LocalFileImpl uploadedFile;
	
	private VFSLeaf exportFile;
	private VFSLeaf exportZip;
	
	private CMCTaskTypeSelectController CMCTaskTypeSelectInlay;
	private CloseableModalController CMCTaskTypeSelectCtr;
	private CMCSelectExistingTaskConfigurationController CMCSelectExistingTCInlay;
	private CloseableModalController CMCSelectExistingTCCtr;
	private TaskInstanceTestController taskInstanceTestController;	
	
	private Link selectTaskType;
	private Link selectExistingConfiguration;
	private Link importTask;
	private Link exportTask;
	
	private EditTaskForm editTaskForm;
		
	private boolean status = false;
	private long courseNodeID;
	private BBautOLATCourseNode courseNode;
	private ICourse course;
	private Configuration conf;
	
	private Panel main;
	private VelocityContainer mainvc;

	private DialogBoxController saveResultsDialog;
	/**
	 * constructor to initialize the controller. 
	 * @param ureq the user request object
	 * @param control the window control object
	 * @param courseNodeID the course node id to determine the configuration
	 * @param courseNode the course node itselfe
	 * @param course the course
	 */
	public EditTaskController(UserRequest ureq, WindowControl control, long courseNodeID, BBautOLATCourseNode courseNode,
			ICourse course) {
		super(ureq, control);
		
		this.courseNodeID = courseNodeID;
		this.courseNode = courseNode;
		this.course = course;
		
		conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(course.getResourceableId(), courseNodeID);
		status = (conf.getTaskConfiguration()!=null ? true : false);
		
		//get the upload container for the import of autolat contents
		uploadContainer = (LocalFolderImpl) course.getCourseFolderContainer().createChildContainer("autolatImport_"+courseNodeID);
		if(uploadContainer==null) {
			List<VFSItem> items = course.getCourseFolderContainer().getItems();
			Iterator<VFSItem> itemsIt = items.iterator();
			while(itemsIt.hasNext()) {
				VFSItem temp = itemsIt.next();
				if(temp.getName().equals("autolatImport_"+courseNodeID)) {
					uploadContainer = (LocalFolderImpl) temp;
					break;
				}
			}
		}
		
		PackageTranslator translator = new PackageTranslator(PACKAGE, this.getLocale());
		setTranslator(translator);
		
		main = new Panel("editTaskPanel");
		createOutput(ureq);
		putInitialPanel(main);
		
	}

	@Override
	protected void doDispose() {
	
		if(exportFile != null) exportFile.delete();
		if(exportZip != null) exportZip.delete();

	}
	protected void event(UserRequest ureq, Controller ctr, Event evnt) {
		if(ctr == CMCTaskTypeSelectInlay) {
			CMCTaskTypeSelectCtr.deactivate();
			conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(course.getResourceableId(), courseNodeID);
			status = true;
			createOutput(ureq);
			fireEvent(ureq, new Event("EDITTYPE_OK"));
		}
		if(ctr == editTaskForm) {
			if(evnt.equals(FormEvent.DONE_EVENT)) {
	
				if(conf.getTaskInstanceList().size()>0 && editTaskForm.isConfTextChanged()) {
					chooseSolutionsHandling(ureq);
				}
				else {
					//persist the changed task configuration
					editTaskForm.persistCurrentTaskConfiguration();
					createOutput(ureq);
					fireEvent(ureq, new Event("EDITTASK_OK"));
				}
			}
		}
		
		if(ctr == CMCSelectExistingTCInlay) {
			CMCSelectExistingTCCtr.deactivate();
			if(conf.getTaskInstanceList().size()>0) {
				chooseSolutionsHandling(ureq);
			}
			else {
				//giving the user some feedback
				CMCSelectExistingTCInlay.editTaskForm.persistCurrentTaskConfiguration();
				CMCSelectExistingTCCtr = null;
				CMCSelectExistingTCInlay = null;
				createOutput(ureq);
				showInfo("info.form.edittask.taskconfigurationsaved");
				fireEvent(ureq, new Event("EDITTASK_OK"));
			}
		}
	
		if(ctr == CMCSelectExistingTCCtr) {
			CMCSelectExistingTCCtr = null;
			CMCSelectExistingTCInlay = null;
			createOutput(ureq);
		}
	
		if(ctr == saveResultsDialog) {
			if(DialogBoxUIFactory.isYesEvent(evnt)) {
				
				TaskInstanceManagerImpl.getInstance().deleteLivingInstancesInTaskInstances(conf.getTaskInstanceList(), true);
				TaskInstanceManagerImpl.getInstance().createNewLivingTaskInstancesInTaskInstances(conf.getTaskInstanceList(), conf);
				//persist the changed task configuration
				if(CMCSelectExistingTCInlay == null) {
					editTaskForm.persistCurrentTaskConfiguration();
				} else {
					CMCSelectExistingTCInlay.editTaskForm.persistCurrentTaskConfiguration();
					CMCSelectExistingTCCtr = null;
					CMCSelectExistingTCInlay = null;
				}
				//giving the user some feedback
				createOutput(ureq);
				showInfo("info.form.edittask.taskconfigurationsaved");
				fireEvent(ureq, new Event("EDITTASK_OK"));
			}
			else if(DialogBoxUIFactory.isClosedEvent(evnt)) {
				//just clean the CMCs up
				if(CMCSelectExistingTCInlay!=null) {
					CMCSelectExistingTCCtr = null;
					CMCSelectExistingTCInlay = null;
				}
			}
			else { //it is the "no"-event
				
				TaskInstanceManagerImpl.getInstance().deleteLivingInstancesInTaskInstances(conf.getTaskInstanceList(), false);
				TaskInstanceManagerImpl.getInstance().createNewLivingTaskInstancesInTaskInstances(conf.getTaskInstanceList(), conf);
				//persist the changed task configuration
				if(CMCSelectExistingTCInlay == null) {
					editTaskForm.persistCurrentTaskConfiguration();
				} else {
					CMCSelectExistingTCInlay.editTaskForm.persistCurrentTaskConfiguration(); 
					CMCSelectExistingTCCtr = null;
					CMCSelectExistingTCInlay = null;
				}
				//giving the user some feedback
				createOutput(ureq);
				showInfo("info.form.edittask.taskconfigurationsaved");
				fireEvent(ureq, new Event("EDITTASK_OK"));
			}
		}
				
		if(ctr == fileUploadCtr) {
			
			if (evnt instanceof FolderEvent && evnt.getCommand().equals(FolderEvent.UPLOAD_EVENT)) {
				
				fileUploadCMCCtr.deactivate();
				
				FolderEvent folderEvent = (FolderEvent) evnt;
				// Get file from temp folder location
				String uploadedFileName = folderEvent.getFilename();
				VFSItem file = uploadContainer.resolve(uploadedFileName);
				if (file != null) {
					// remove old files first from a previous upload
					if (uploadedFile != null) uploadedFile.delete();
					// We know it is a local file, cast is necessary to get file reference
					uploadedFile = (LocalFileImpl) file;
					if(uploadedFile.getName().endsWith(".zip"))
						ZipUtil.unzip((LocalFileImpl) file, uploadContainer);
					List<VFSItem> items = uploadContainer.getItems();
					Iterator<VFSItem> itemIt = items.iterator();
					while(itemIt.hasNext()) {
						VFSItem temp = itemIt.next();
						if(temp.getName().endsWith(".xml")) {
							LocalFileImpl importFile = (LocalFileImpl) temp;
							try {
								// reload configuration
								conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(course.getResourceableId(), courseNodeID);
								AutOlatNodeImporter importer = new AutOlatNodeImporter(conf);
								importer.importFromFile(importFile.getBasefile());
								//ServerConnectionManagerImpl.getInstance().saveOrUpdateServerConnection(conf.getServerConnection());
								TaskTypeManagerImpl.getInstance().saveOrUpdateTaskType(conf.getTaskConfiguration().getTaskType());
								TaskConfigurationManagerImpl.getInstance().saveOrUpdateTaskConfiguration(conf.getTaskConfiguration());
								ConfigurationManagerImpl.getInstance().updateConfiguration(conf);
								fileUploadCMCCtr.dispose();
								fileUploadCtr.dispose();
								//cleanup of files
								importFile.delete();
								uploadedFile.delete();
								status = true;
								fireEvent(ureq, new Event("CONNEDIT_OK"));
								fireEvent(ureq, new Event("EDITTYPE_OK"));
								fireEvent(ureq, new Event("EDITTASK_OK"));
								fireEvent(ureq, new Event("IMPORT_DONE"));
								createOutput(ureq);
								//inform the user about the progress
								showInfo("info.controller.edittask.importsucceeded");
							} catch (Exception e) {
								e.printStackTrace();
								showError("error.controller.edittask.importfailed");
							}
							break;
						}
					}
				} else {
					showError("error.controller.edittask.uploadfailed");
				}
			}
			else {
				if(evnt.equals(Event.CANCELLED_EVENT)) {
					fileUploadCMCCtr.deactivate();
					fileUploadCMCCtr.dispose();
					fileUploadCtr.dispose();
				}
			}
		}

	}

	@Override
	protected void event(UserRequest ureq, Component comp, Event evnt) {
			
		if(comp == selectTaskType) {
			CMCTaskTypeSelectInlay = new CMCTaskTypeSelectController(ureq, this.getWindowControl(), course.getResourceableId(), courseNodeID);
			CMCTaskTypeSelectInlay.addControllerListener(this);
			CMCTaskTypeSelectCtr = new CloseableModalController(this.getWindowControl(),
					translate("label.controller.cmcedittaskmodule.close"), CMCTaskTypeSelectInlay.getInitialComponent());
			CMCTaskTypeSelectCtr.addControllerListener(this);
			CMCTaskTypeSelectCtr.activate();
		}
		if(comp == selectExistingConfiguration) {
			CMCSelectExistingTCInlay = new CMCSelectExistingTaskConfigurationController(ureq, getWindowControl(),
					course.getResourceableId(), courseNodeID);
			CMCSelectExistingTCInlay.addControllerListener(this);
			CMCSelectExistingTCCtr = new CloseableModalController(getWindowControl(), 
					translate("label.controller.cmcedittaskmodule.close"), CMCSelectExistingTCInlay.getInitialComponent());
			CMCSelectExistingTCCtr.addControllerListener(this);
			CMCSelectExistingTCCtr.activate();
		}
		
		if(comp == exportTask) {
			
			if(exportFile != null) exportFile.delete();
			if(exportZip != null) exportZip.delete();
			
			try {
				
				String exportFileName = courseNode.getShortTitle()+".xml";
				String exportZipName = courseNode.getShortTitle()+".zip";
				String exportContainer = "autolatExport_"+courseNodeID;
				//delete the old export container
				List<VFSItem> VFSItems = course.getCourseFolderContainer().getItems();
				Iterator<VFSItem> itemsIt = VFSItems.iterator();
				while(itemsIt.hasNext()) {
					VFSItem temp = itemsIt.next();
					if(temp.getName().equals(exportContainer)) temp.delete();
				}
				//export the node contents
				VFSContainer exportCon = course.getCourseFolderContainer().createChildContainer(exportContainer);
				exportFile = exportCon.createChildLeaf(exportFileName);
				exportZip = exportCon.createChildLeaf(exportZipName);
				OutputStream exportStream = exportFile.getOutputStream(true);
				AutOlatNodeExporter exporter = new AutOlatNodeExporter(ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(course.getResourceableId(), courseNodeID));
				exporter.exportNode(exportStream);
				List zipList = new ArrayList();
				zipList.add(exportFile);
				ZipUtil.zip(zipList, exportZip, true);
				FileUtils.closeSafely(exportStream);
				ureq.getDispatchResult().setResultingMediaResource(new VFSMediaResource(exportZip));
				
			} catch (Exception e) {
				e.printStackTrace();
				showError("error.controller.edittask.exportfailed");
			}
		}
		
		if(comp == importTask) {
			
			removeAsListenerAndDispose(fileUploadCtr);
			removeAsListenerAndDispose(fileUploadCMCCtr);
			fileUploadCtr = new FileUploadController(getWindowControl(), uploadContainer, ureq, 500, Quota.UNLIMITED, null, false);
			listenTo(fileUploadCtr);
			fileUploadCMCCtr = new CloseableModalController(getWindowControl(), 
					translate("label.controller.uploadfilecmc.close"), fileUploadCtr.getInitialComponent());
			//listenTo(fileUploadCMCCtr); probably not needed
			fileUploadCMCCtr.activate();
		}
	}
	/**
	 * build the output even from outside
	 * @param ureq
	 */
	public void createOutput(UserRequest ureq) { 
		
		mainvc = null;
		
		mainvc = this.createVelocityContainer("editTaskController");
		
		String taskType = "-";
		
		conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(course.getResourceableId(), courseNodeID);
		
		if(conf.getTaskConfiguration()!=null) { 
			BBautOLATConnector conn = null;
			try {
				conn = new BBautOLATConnector(conf);
				taskType = conn.getTaskTypeHierarchyBreadcrumb(conf.getTaskConfiguration().getTaskType());
				conn = null;
			} catch (JDOMException e) {
				showInfo("error.form.editconnection.XMLerror");
			} catch (IOException e) {
				showInfo("error.form.editconnection.IOerror");
			} catch (AutolatConnectorException e) {
				showInfo("error.form.editconnection.Servererror");
			}
			
		}
		
		importTask = LinkFactory.createButton("label.controller.edittask.importtask", mainvc, this);
		mainvc.put("importTask", importTask);
		exportTask = LinkFactory.createButton("label.controller.edittask.exporttask", mainvc, this);
		exportTask.setEnabled(status);
		mainvc.put("exportTask", exportTask);
		
		mainvc.contextPut("tasktype", taskType);
		
		selectTaskType = LinkFactory.createButton("label.controller.edittask.selecttype", mainvc, this);
		if(this.conf.getAutolatServer()==null) { 
			selectTaskType.setEnabled(false);
		} 
		mainvc.put("selectTaskType", selectTaskType);
		
		
		editTaskForm = new EditTaskForm(EditTaskForm.NAME, ureq, getWindowControl(), course.getResourceableId(), courseNodeID, null);
		editTaskForm.addControllerListener(this);
		editTaskForm.setElementAccessStatus(status);
		mainvc.put("editTask", editTaskForm.getInitialComponent());
		
		selectExistingConfiguration = LinkFactory.createButton("label.controller.edittask.existingconfiguration", mainvc, this);
		selectExistingConfiguration.setEnabled(status);
		mainvc.put("selectExistingConfiguration", selectExistingConfiguration);
		
		if(conf.getTaskConfiguration()!=null) {
			
			taskInstanceTestController = null;
			taskInstanceTestController = new TaskInstanceTestController
				.Builder(ureq, this.getWindowControl(), course.getResourceableId(), courseNodeID)
				.showSeedInputField()
				.showTaskText()
				.showSolutionForm()
				.build();
			mainvc.put("taskInstanceTest", taskInstanceTestController.getInitialComponent());
			mainvc.contextPut("hasTaskInstanceTest", true);
		}
		
		//disable edit functions if task instances are existing
		if(conf.getTaskInstanceList().size()!=0) {
			//editTypeForm.setEnable(false);
			editTaskForm.setElementAccessStatus(false);
			selectTaskType.setEnabled(false);
			importTask.setEnabled(false);
			selectExistingConfiguration.setEnabled(false);
			//editPropertiesForm.setEnable(false);

		}
		
		main.setContent(mainvc);
	}
	
	@Deprecated
	private void chooseSolutionsHandling(UserRequest ureq) {
		saveResultsDialog = DialogBoxUIFactory.createYesNoDialog(ureq, this.getWindowControl(), 
				translate("label.dialog.savesolutions.title"), translate("label.dialog.savesolutions.text"));
		saveResultsDialog.addControllerListener(this);
		saveResultsDialog.activate();
	}

}
