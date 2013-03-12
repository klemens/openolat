package de.unileipzig.xman.module;

import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * @author
 */
public class ModuleImpl extends PersistentObject implements Module {
	
	/**
	 * unique name, but can change
	 */
	private String name;
	
	/**
	 * last Modified
	 */
	private Date lastModified;
	
	/**
	 * additional text added to a module
	 */
	private String description;
	
	/**
	 * the person in charge of this module
	 */
	private Identity personInCharge;
	
	/**
	 * 
	 */
	private String moduleNumber;
	
	/**
	 * empty, for hibernate
	 *
	 */
	public ModuleImpl() {}
	
	/*------------------------getter/setter-----------------------*/
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * sets the description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * sets the name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @see de.xman.module.Module#getPersonInCharge()
	 */
	public Identity getPersonInCharge() {
	
		return this.personInCharge;
	}

	/**
	 * @see de.xman.module.Module#setPersonInCharge(org.olat.core.id.Identity)
	 */
	public void setPersonInCharge(Identity identity) {
		
		this.personInCharge = identity;
	}
	
	/**
	 * @see PersistentObject#getKey()
	 */
	public Long getResourceableId() {
		
		return this.getKey();
	}

	/**
	 * @return the olat resourceable type name
	 */
	public String getResourceableTypeName() {
		
		return ORES_TYPE_NAME;
	}
	
	/**
	 * @see de.xman.module.Module#getModuleNumber()
	 */
	public String getModuleNumber() {
		
		return this.moduleNumber;
	}
	
	/**
	 * @see de.xman.module.Module#setModuleNumber(java.lang.String)
	 */
	public void setModuleNumber(String number) {
		
		this.moduleNumber = number;
	}

	public Date getLastModified() {
		
		return this.lastModified;
	}

	public void setLastModified(Date lastModified) {
		
		this.lastModified = lastModified;
	}


}
