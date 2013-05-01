package de.unileipzig.xman.module;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.hibernate.Hibernate;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;

// wtf ... wozu wird das importiert
// import de.xman.exam.ExamDBManager;

/**
 * 
 * @author
 */
public class ModuleManager {

	/**
	 * for singleton
	 */
	private static ModuleManager INSTANCE = new ModuleManager();
	/**
	 * for singleton
	 *
	 */
	private ModuleManager() {
		// singleton
	}
	/**
	 * for singleton
	 * @return the one and only instance of this manager
	 */
	public static ModuleManager getInstance(){
		return INSTANCE;
	}
	
	
	/*------------------------------- module -------------------------*/
	
	
	/**
	 * Creates a new module, persists it in the database and returns it.
	 * @return the new created module
	 */
	public Module createModule() {
		
		return new ModuleImpl();
	}
	
	/**
	 * Saves the given module.
	 * @param cat - the module, which is to be saved
	 */
	public void saveModule(Module cat) {
		
		DBFactory.getInstance().saveObject(cat);
		Tracing.createLoggerFor(ModuleManager.class).info("New module with id " + cat.getKey() + " was created.");
	}
	
	/**
	 * Updates an existing module.
	 * @param cat - the module, which is to be updated
	 */
	public void updateModule(Module cat){
		
		DBFactory.getInstance().updateObject(cat); 
	}
	
	/**
	 * Deletes the given module.
	 * @param cat - the module, which is to be deleted
	 */
	public void deleteModule(Module module) {
		
		if ( module != null ) DBFactory.getInstance().deleteObject(module);
	}
		
	/**
	 * @return a List of all existing modules, or null if there are no modules
	 */
	public List<Module> findAllModules() {
		
		List<Module> modulList = new Vector<Module>();
		String query = "from de.unileipzig.xman.module.ModuleImpl";
		List<Module> searchList = DBFactory.getInstance().find(query);
		for(Module modul:searchList){
			modulList.add(modul);
		}
		return modulList;
	}
	
	/**
	 * Get a module by a unique name.
	 * @param name the name of the module
	 * @return the specified module, or null if the specified module does not exist
	 */
	public Module findModuleByName(String name){
		
		String query = "from de.unileipzig.xman.module.ModuleImpl as module where module.name = ?";
		List modules = DBFactory.getInstance().find(query, new Object[]{name}, new Type[]{StandardBasicTypes.STRING});
		int size = modules.size();
		if (size == 0) return null;
		if (size != 1) throw new AssertException("non unique key with: " + name);
		
		return (Module) modules.get(0);
	}
}
