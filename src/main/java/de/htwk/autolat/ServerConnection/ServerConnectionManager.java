/*
 * 
 */
package de.htwk.autolat.ServerConnection;

import java.util.Date;
import java.util.List;

/**
 * Description:<br>
 * The abstract server connection manager.
 * 
 * <P>
 * Initial Date:  20.11.2009 <br>
 * @author Tom
 */
public abstract class ServerConnectionManager {
	
	/**
	 * Creates a server connection and persists it.
	 * 
	 * @param lastContact The last contact
	 * @param name The name
	 * @param path The path
	 * @param isActive The is active state
	 * @return The new server connection
	 */
	public abstract ServerConnection createAndPersistServerConnection(Date lastContact, String name, String path, boolean isActive);
	
	/**
	 * Creates a server connection.
	 * 
	 * @param lastContact the last contact
	 * @param name the name
	 * @param path the path
	 * @param isActive the is active
	 * @return the server connection
	 */
	public abstract ServerConnection createServerConnection(Date lastContact, String name, String path,
			boolean isActive);
	
	/**
	 * Gets a random server connection.
	 * 
	 * @return A random server connection
	 */
	public abstract ServerConnection getRandomServerConnection();
	
	/**
	 * Gets all server connections.
	 * 
	 * @return All server connections
	 */
	public abstract List<ServerConnection> getAllServerConnections();
	
	/**
	 * Loads server connection by id.
	 * 
	 * @param ID The id
	 * @return The server connection to be loaded by id
	 */
	public abstract ServerConnection loadServerConnectionByID(long ID);
		
	/**
	 * Saves a server connection.
	 * 
	 * @param serverConn The server connection to be saved
	 */
	public abstract void saveServerConnection(ServerConnection serverConn);
	
	/**
	 * Updates a server connection.
	 * 
	 * @param serverConn The server connection to be updated
	 */
	public abstract void updateServerConnection(ServerConnection serverConn);
	
	/**
	 * Deletes a server connection.
	 * 
	 * @param serverConn The server connection to be deleted
	 * @return True, if successful
	 */
	public abstract boolean deleteServerConnection(ServerConnection serverConn);
	
	/**
	 * Gets all active server connections.
	 * 
	 * @return All active server connections
	 */
	public abstract List<ServerConnection> getAllActiveServerConnections();
	
	/**
	 * Gets the server connection by key.
	 * 
	 * @param key The key
	 * @return The server connection by key
	 */
	public abstract ServerConnection getServerConnectionByKey(long key);
	
	/**
	 * Sets the last contact.
	 * 
	 * @param serverConnection The server connection to be updated
	 */
	public abstract void setLastContact(ServerConnection serverConnection);
	
	/**
	 * Sets the active state in a connection object.
	 * 
	 * @param serverConnection The server connection to be modified
	 * @param state The state to set
	 */
	public abstract void setActive (ServerConnection serverConnection, boolean state);
	
	/**
	 * Gets one server connection by url.
	 * 
	 * @param URL The path of the server
	 * @return One server connection by url
	 */
	public abstract ServerConnection getOneServerConnectionByURL(String URL);
	
	public abstract void saveOrUpdateServerConnection(ServerConnection serverConnection);
	
	/**
	 * Gets all used connections.
	 * 
	 * @return All used connections
	 */
	public abstract List<ServerConnection> getAllUsedConnections();
}
