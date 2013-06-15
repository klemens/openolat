package de.htwk.autolat.ServerConnection;

import java.net.URL;
import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;

/**
 * Description:<br>
 * The implementation of a server connection obejct.
 * 
 * <P>
 * Initial Date:  20.11.2009 <br>
 * @author Tom
 */
public class ServerConnectionImpl extends PersistentObject implements ServerConnection {
	
	/** The last contact. */
	private Date lastContact;
	
	/** The name. */
	private String name;
	
	/** The path. */
	private String path;
	
	/** The is active state of the connection. */
	private boolean isActive;
	
	/**
	 * @see de.htwk.autolat.ServerConnection.ServerConnection#getLastContact()
	 */
	public Date getLastContact() {
		return lastContact;
	}

	/**
	 * @see de.htwk.autolat.ServerConnection.ServerConnection#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see de.htwk.autolat.ServerConnection.ServerConnection#getUrl()
	 */
	public URL getUrl() {
		
		try {
			URL url = new URL(getPath());
			return url;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @see de.htwk.autolat.ServerConnection.ServerConnection#getIsActive()
	 */
	public boolean getIsActive() {
		return isActive;
	}

	/**
	 * @see de.htwk.autolat.ServerConnection.ServerConnection#setIsActive(boolean)
	 */
	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}

	/**
	 * @see de.htwk.autolat.ServerConnection.ServerConnection#setLastContact(java.util.Date)
	 */
	public void setLastContact(Date lastContact) {
		this.lastContact = lastContact;
	}

	/**
	 * @see de.htwk.autolat.ServerConnection.ServerConnection#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @see de.htwk.autolat.ServerConnection.ServerConnection#getPath()
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @see de.htwk.autolat.ServerConnection.ServerConnection#setPath(java.lang.String)
	 */
	public void setPath(String path) {
		this.path = path;
	}

}
