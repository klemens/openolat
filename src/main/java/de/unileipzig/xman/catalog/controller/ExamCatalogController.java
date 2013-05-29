package de.unileipzig.xman.catalog.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

// alt org.olat.basesecurity.ManagerFactory
import org.olat.basesecurity.BaseSecurityManager;

import org.olat.catalog.CatalogEntry;
import org.olat.catalog.CatalogManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResourceManager;

import de.unileipzig.xman.catalog.table.CatalogEntryTableModel;
import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.studyPath.StudyPath;

public class ExamCatalogController extends BasicController {

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(Exam.class);
	private static final String COMMAND_CONFIRM = "confirm.entry";
	
	private VelocityContainer vcMain;

	private Exam exam;
	private TableController catalogTableCtr;
	private CatalogEntryTableModel catalogTableMdl;
	// represents the choosen path
	private List<CatalogEntry> entryList;
	
	private CatalogEntry choosenEntry;
	
	public ExamCatalogController(UserRequest ureq, WindowControl control, Exam exam) {
		super(ureq, control);
				
		setTranslator(Util.createPackageTranslator(Exam.class, ureq.getLocale()));
		
		vcMain = new VelocityContainer("vcAttr", VELOCITY_ROOT + "/chooseCatalogEntry.html", getTranslator(), this);
		
		// add root entry anyway
		entryList = new Vector<CatalogEntry>();
		entryList.add(CatalogManager.getInstance().getRootCatalogEntries().get(0));
		
		this.exam = exam;
		this.createCatalogTableModel(ureq, control, null);
		
		vcMain.contextPut("choise", translate("ExamCatalogController.yourChoise"));
		vcMain.contextPut("catalog", translate("ExamCatalogController.catalog"));
		vcMain.contextPut("usage", translate("ExamCatalogController.usage"));
		vcMain.contextPut("help", translate("ExamCatalogController.help"));
		
		putInitialPanel(vcMain);
	}	

	private void createCatalogTableModel(UserRequest ureq, WindowControl wControl, CatalogEntry ce) {
		removeAsListenerAndDispose(catalogTableCtr);
		
		TableGuiConfiguration catalogEntryTableConfig = new TableGuiConfiguration();
		catalogEntryTableConfig.setTableEmptyMessage(translate("ExamCatalogController.catalogEntryTable.emptyTableMessage"));
		
		catalogTableCtr = new TableController(catalogEntryTableConfig, ureq, wControl, getTranslator());
		// if no catalogEntry is choosen, find all children from root-entry 
		catalogTableMdl = new CatalogEntryTableModel(ureq.getLocale(), 
													 choosenEntry != null ?
														getChildrenOfExceptRepoEntries(choosenEntry) : 
														getChildrenOfExceptRepoEntries(CatalogManager.getInstance().getRootCatalogEntries().get(0)),
													 getTranslator());
		catalogTableMdl.setTable(catalogTableCtr);
		catalogTableCtr.setTableDataModel(catalogTableMdl);
		catalogTableCtr.setSortColumn(0, true);
		catalogTableCtr.setMultiSelect(true);
		catalogTableCtr.addMultiSelectAction("ExamCatalogController.catalogEntryTable.multiselect.confirm", COMMAND_CONFIRM);
		
		listenTo(catalogTableCtr);
		
		this.vcMain.put("catalogEntryTable", catalogTableCtr.getInitialComponent());
		this.vcMain.contextPut("path", this.createSelectedCatalogEntryPath());
	}

	@Override
	protected void doDispose() {
		removeAsListenerAndDispose(catalogTableCtr);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		
	}
	
	protected void event(UserRequest ureq, Controller source, Event event) {

		if (source == catalogTableCtr) {

			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {

				TableEvent te = (TableEvent) event;
				String actionID = te.getActionId();

				// somebody wants to open an esf
				if (actionID.equals(CatalogEntryTableModel.COMMAND_DOWN)) {
					
					this.choosenEntry = catalogTableMdl.getEntryAt(te.getRowId());
					// it should be not possible to change in a category with no children
					if (getChildrenOfExceptRepoEntries(choosenEntry).size() != 0 ) {
						
						this.entryList.add(choosenEntry);
						this.createCatalogTableModel(ureq, this.getWindowControl(),	choosenEntry);
					}
					else {
						showInfo("ExamCatalogController.catalogTable.noChildren");
					}
				}

				if (actionID.equals(CatalogEntryTableModel.COMMAND_UP)) {

					CatalogEntry tempEntry = catalogTableMdl.getEntryAt(te.getRowId());

					// it's not possible to remove the root node from the choosen path
					CatalogEntry root = (CatalogEntry) CatalogManager.getInstance().getRootCatalogEntries().get(0);
					
					if (!tempEntry.getParent().getName().equals(root.getName())) {

						this.entryList.remove(entryList.size() - 1);
						// make 2 steps back in the hierachy to see the parent of the entry
						choosenEntry = tempEntry.getParent().getParent();
						this.createCatalogTableModel(ureq, this.getWindowControl(), choosenEntry);
						
					} else {
						showInfo("ExamCatalogController.tryToRemoveRoot");
					}
				}
			}
			
			if ( event.getCommand().equals(Table.COMMAND_MULTISELECT) ) {
				
				TableMultiSelectEvent tmse = (TableMultiSelectEvent)event;
				
				if ( tmse.getAction().equals(COMMAND_CONFIRM) ) {
					
					List<CatalogEntry> entries = (ArrayList<CatalogEntry>)catalogTableMdl.getObjects(tmse.getSelection());
					
					// a catalogEntry for the exam should be created and persisted
					if ( entries.size() == 1 ) {
					
						// create entrie and set values
						CatalogEntry newEntry = CatalogManager.getInstance().createCatalogEntry();
						
						OLATResourceable ores = OLATResourceManager.getInstance().findResourceable(exam.getResourceableId(), Exam.ORES_TYPE_NAME);
						RepositoryEntry entry = RepositoryManager.getInstance().lookupRepositoryEntry(ores, true);
						
						newEntry.setType(CatalogEntry.TYPE_LEAF);
						newEntry.setName(entry.getDisplayname());
		// ---------------------------------alt ManagerFactory.getManager().createAndPersistSecurityGroup()
						newEntry.setOwnerGroup(BaseSecurityManager.getInstance().createAndPersistSecurityGroup());
						newEntry.setRepositoryEntry(entry);
					
						// set the parent and save the new entry
						CatalogManager.getInstance().addCatalogEntry(entries.get(0), newEntry);
						
						String path = this.createSelectedCatalogEntryPath();
						
						// avoid something like: Catalogroot > > Informatik
						path = path.endsWith("> ") ? (path + (entries.get(0).getName())) : (path + " > " + entries.get(0).getName());
						
						// show the user the path where the link has been placed
						showInfo("ExamCatalogController.catalogEntrySuccessfullyCreated", path);
					}
					else {
						showInfo("ExamCatalogController.moreThenOneCatalogEntryChoosen");
					}
				}
			}
		}
	}
	
	private List<CatalogEntry> getChildrenOfExceptRepoEntries(CatalogEntry ce) {
		List<CatalogEntry> children = CatalogManager.getInstance().getChildrenOf(ce);
		
		ArrayList<CatalogEntry> folders = new ArrayList<CatalogEntry>();
		for(CatalogEntry ceNew : children) {
			if(ceNew.getRepositoryEntry() == null)
				folders.add(ceNew);
		}
		
		return folders;
	}
	
	private String createSelectedCatalogEntryPath() {
		
		String choosenEntry = "";
		for (int i = 0; i < entryList.size(); i++ ) {
			
			choosenEntry += entryList.get(i).getName();
			
			// only add the ">" if there are children for this entry
			if ( getChildrenOfExceptRepoEntries(entryList.get(i)).size() != 0 ) {
				
				choosenEntry += " > ";
			}
		}
		return choosenEntry;
	}
}
	
