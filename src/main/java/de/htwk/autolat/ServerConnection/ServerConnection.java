package de.htwk.autolat.ServerConnection;

import java.net.URL;
import java.util.Date;

import org.olat.core.id.Persistable;

/**
 * Description:<br>
 * The abstract object for a server connection.
 * 
 * <P>
 * Initial Date:  20.11.2009 <br>
 * @author Tom
 */
public interface ServerConnection extends Persistable {
	
	/**
	 * Sets the name.
	 * 
	 * @param name The new name
	 */
	public void setName(String name);
	
	/**
	 * Gets the name.
	 * 
	 * @return The name
	 */
	public String getName();
	
	/**
	 * Sets the is active state.
	 * 
	 * @param isActive The new is active state
	 */
	public void setIsActive(boolean isActive);
	
	/**
	 * Gets the is active state.
	 * 
	 * @return The is active state
	 */
	public boolean getIsActive();
	//public void setUrl(URL url);
	/**
	 * Gets the url.
	 * 
	 * @return The url of the connection
	 */
	public URL getUrl();
	
	/**
	 * Sets the last contact.
	 * 
	 * @param lastContact The new last contact entry for the connection
	 */
	public void setLastContact(Date lastContact);
	
	/**
	 * Gets the last contact.
	 * 
	 * @return The last contact of the connection
	 */
	public Date getLastContact();
	
	/**
	 * Gets the path.
	 * 
	 * @return The path of the server
	 */
	public String getPath();
	
	/**
	 * Sets the path.
	 * 
	 * @param path The new path for the connection
	 */
	public void setPath(String path);
}
