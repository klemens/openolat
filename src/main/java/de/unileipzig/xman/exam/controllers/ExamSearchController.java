package de.unileipzig.xman.exam.controllers;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryManager;

import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.exam.ExamTableModel;

/**
 * 
 * @author
 */
public class ExamSearchController extends DefaultController {

	private static final String PACKAGE = Util.getPackageName(Exam.class);
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(Exam.class);
	
	private Translator translator;
	private VelocityContainer vcMain;
	
	private ExamTableModel examTableMdl;
	private TableController examTableCtr;
	
	/**
	 * creates the controller for the exam search
	 * @param ureq UserRequest object
	 * @param wControl WindowControl object
	 */
	public ExamSearchController(String selectButtonLabel, UserRequest ureq, WindowControl wControl, boolean enableDirectLaunch) {
		
		super(wControl);
		this.init(selectButtonLabel, ureq, enableDirectLaunch);
	}
	
	/**
	 * Initializes all components and sets the initial component.
	 * 
	 * @param ureq UserRequest object
	 */
	private void init(String selectButtonLabel, UserRequest ureq, boolean enableDirectLaunch) {
		
		translator = new PackageTranslator(PACKAGE, ureq.getLocale());
		
		vcMain = new VelocityContainer("examSearch", VELOCITY_ROOT + "/examSearch.html", translator, this);
		
		TableGuiConfiguration examTableCfg = new TableGuiConfiguration();
		examTableCfg.setPreferencesOffered(true, "examTablePreferences");
		examTableCtr = new TableController(examTableCfg, ureq, this.getWindowControl(), translator);
		examTableMdl = new ExamTableModel(translator);
		examTableMdl.setTable(examTableCtr, selectButtonLabel, enableDirectLaunch);
		examTableCtr.setTableDataModel(examTableMdl);
		examTableCtr.setSortColumn(1, true);
		vcMain.put("examTable", examTableCtr.getInitialComponent());
		
		this.setInitialComponent(vcMain);
	}

	/**
	 * @see DefaultController#dispose(boolean)
	 */
	protected void doDispose() {
		
		// nothing to do here
	}

	/**
	 * @see DefaultController#event(UserRequest, Component, Event)
	 */
	public void event(UserRequest arg0, Component arg1, Event arg2) {
		
		// TODO Auto-generated method stub
	}
	
	/**
	 * search for exam with limit access
	 * @param type the type of the search objects
	 * @param ureq the user request
	 */
	public void doSearchByTypeLimitAccess(String type, UserRequest ureq) {
		
		RepositoryManager rm = RepositoryManager.getInstance();
		//TODO: Das Identity-Argument ist optional, aber ...
		List entries = rm.queryByTypeLimitAccess(null,type, ureq.getUserSession().getRoles());
		examTableMdl.setEntries(entries);
		examTableCtr.modelChanged();
		
		// TODO: test methode - muss angepasst werden!
		/*
		RepositoryManager rm = RepositoryManager.getInstance();
		RepositoryEntry entry = rm.lookupRepositoryEntry(new Long(id));
		List<RepositoryEntry> entries = new ArrayList<RepositoryEntry>(1);
		if (entry != null) entries.add(entry);
		examTableModel.setEntries(entries);
		examTableCtr.modelChanged();
		displaySearchResults(null);
		*/
		
	}
}
