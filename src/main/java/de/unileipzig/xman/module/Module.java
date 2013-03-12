package de.unileipzig.xman.module;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Persistable;
import org.olat.core.util.resource.OresHelper;

/**
 * 
 * @author team.uni-leipzig
 */
public interface Module extends OLATResourceable, CreateInfo, Persistable, ModifiedInfo {
	
	//Repository types
	public static String ORES_TYPE_NAME = OresHelper.calculateTypeName(Module.class);
	
	/**
	 * @return the description of the module
	 */
	public String getDescription();
	
	/**
	 * @return the name of the module
	 */
	public String getName();

	/**
	 * Sets the description of the module.
	 * @param description - the description which is to be set
	 */
	public void setDescription(String description);
	
	/**
	 * Sets the name of the module. 
	 * @param name - the name which is to be set
	 */
	public void setName(String name);
	
	/**
	 * Sets the identity of the person who is responsible for all question
	 * around this module.
	 * 
	 * @param identity - the identity of the person in charge
	 */
	public void setPersonInCharge(Identity identity);
	
	/**
	 * @return the identity of the person in charge of this module
	 */
	public Identity getPersonInCharge();
	
	/**
	 * @return the module number
	 */
	public String getModuleNumber();
	
	/**
	 * @param number the number of the module
	 */
	public void setModuleNumber(String number);
}