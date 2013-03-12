package de.unileipzig.xman.admin.controller;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.gui.control.generic.tool.ToolFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.*;

import de.unileipzig.xman.admin.ExamAdminSite;
import de.unileipzig.xman.admin.forms.CreateAndEditModuleForm;
import de.unileipzig.xman.module.*;
import de.unileipzig.xman.module.table.ModuleTableModel;

/**
 * 
 * @author
 * 
 */
public class ExamAdminModulesController extends DefaultController {

	private static final String VELOCITY_ROOT = Util
			.getPackageVelocityRoot(ExamAdminSite.class);

	private ToolController toolCtr;
	private Translator translator;
	private VelocityContainer mainVC;
	private TableController moduleTableCtr;
	private ModuleTableModel moduleTableMdl;

	private CloseableModalController cmcModuleCtr;
	private CreateAndEditModuleForm createForm;
	private CreateAndEditModuleForm updateForm;

	private Module curModule;

	/**
	 * @see org.olat.core.gui.control.DefaultController#DefaultController(WindowControl)
	 */
	public ExamAdminModulesController(UserRequest ureq, WindowControl wControl) {
		super(wControl);

		translator = Util.createPackageTranslator(ExamAdminSite.class, ureq.getLocale());

		mainVC = new VelocityContainer("examModules", VELOCITY_ROOT
				+ "/modules.html", translator, this);

		toolCtr = ToolFactory.createToolController(wControl);
		toolCtr.addControllerListener(this);
		toolCtr.addHeader(translator
				.translate("ExamAdminModulesController.tool.header"));
		toolCtr.addLink("action.add", translator
				.translate("ExamAdminModulesController.tool.add"));

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		moduleTableCtr = new TableController(tableConfig, ureq, wControl,
				translator);
		moduleTableMdl = new ModuleTableModel(translator, ModuleManager
				.getInstance().findAllModules(), true);
		moduleTableMdl.setTable(moduleTableCtr);
		moduleTableCtr.setTableDataModel(moduleTableMdl);
		moduleTableCtr.setSortColumn(0, true);

		// NEU
		moduleTableCtr.addControllerListener(this);

		mainVC.put("modulesTable", moduleTableCtr.getInitialComponent());

		this.setInitialComponent(mainVC);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#dispose(boolean)
	 */
	protected void doDispose() {

		// nothing to dispose
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(UserRequest,
	 *      Controller, Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {

		if (source == createForm) {

			if (event == Form.EVNT_VALIDATION_OK) {

				String description = createForm.getDescription();
				Module module = ModuleManager.getInstance().createModule();
				module.setName(createForm.getName());
				module.setDescription(description);
				module.setPersonInCharge(createForm.getPersonInCharge());
				module.setModuleNumber(createForm.getModuleNumber());
				ModuleManager.getInstance().saveModule(module);
				cmcModuleCtr.deactivate();
				if (module != null) {
					moduleTableMdl.setEntries(ModuleManager.getInstance()
							.findAllModules());
					moduleTableCtr.modelChanged();
				}
			} else if (event == Form.EVNT_FORM_CANCELLED) {
				cmcModuleCtr.deactivate();
			}
		}
		if (source == updateForm) {
			if (event == Form.EVNT_VALIDATION_OK) {
				curModule = ModuleManager.getInstance().findModuleByName(
						curModule.getName());
				curModule.setName(updateForm.getName());
				curModule.setDescription(updateForm.getDescription());
				curModule.setPersonInCharge(updateForm.getPersonInCharge());
				curModule.setModuleNumber(updateForm.getModuleNumber());
				ModuleManager.getInstance().updateModule(curModule);
				moduleTableMdl.setEntries(ModuleManager.getInstance()
						.findAllModules());
				moduleTableCtr.modelChanged();
				cmcModuleCtr.deactivate();
			} else if (event == Form.EVNT_FORM_CANCELLED) {
				cmcModuleCtr.deactivate();
			}
		}
		// ToolController
		if (source == toolCtr) {
			if (event.getCommand().equals("action.add")) {
				createForm = new CreateAndEditModuleForm(ureq,
						getWindowControl(), "createModuleForm", null);
				createForm.addControllerListener(this);
				cmcModuleCtr = new CloseableModalController(this
						.getWindowControl(), translator
						.translate("ExamAdminModulesController.command.close"),
						createForm.getInitialComponent());
				cmcModuleCtr.addControllerListener(this);
				cmcModuleCtr.activate();
			}
		}
		if (source == moduleTableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				int rowid = te.getRowId();
				curModule = moduleTableMdl.getEntryAt(rowid);
				if (actionid.equals(ModuleTableModel.ENTRY_SELECTED)) {
					updateForm = new CreateAndEditModuleForm(ureq,
							getWindowControl(), "updateModuleForm", curModule);
					updateForm.addControllerListener(this);
					cmcModuleCtr = new CloseableModalController(
							this.getWindowControl(),
							translator
									.translate("ExamAdminModulesController.command.close"),
							updateForm.getInitialComponent());
					cmcModuleCtr.addControllerListener(this);
					cmcModuleCtr.activate();
				}
			}
		}
	}

	/**
	 * @return the toolcontroller
	 */
	public ToolController getToolController() {
		return toolCtr;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// TODO Auto-generated method stub

	}
}
