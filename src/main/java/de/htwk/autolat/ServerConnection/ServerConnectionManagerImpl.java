package de.htwk.autolat.ServerConnection;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;

/**
 * Description:<br>
 * The manager implementation for the server connection objects.
 * 
 * <P>
 * Initial Date:  20.11.2009 <br>
 * @author Tom
 */
public class ServerConnectionManagerImpl extends ServerConnectionManager {
	
	/** The Constant INSTANCE. */
	private static final ServerConnectionManagerImpl INSTANCE = new ServerConnectionManagerImpl();
	
	/**
	 * Instantiates a new server connection manager impl.
	 */
	private ServerConnectionManagerImpl() {
		//nothing to do here
	}

	/**
	 * Creates a server connection.
	 * 
	 * @param lastContact the last contact
	 * @param name the name
	 * @param path the path
	 * @param isActive the is active
	 * @return the server connection
	 * @see de.htwk.autolat.ServerConnection.ServerConnectionManager#createServerConnection(java.util.Date,
	 *      java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public ServerConnection createServerConnection(Date lastContact, String name, String path, boolean isActive) {
		ServerConnection ser = new ServerConnectionImpl();
		ser.setIsActive(isActive);
		ser.setLastContact(lastContact);
		ser.setName(name);
		ser.setPath(path);
		return ser;
	}
	
	/**
	 * Creates a and persist server connection.
	 * 
	 * @param lastContact the last contact
	 * @param name the name
	 * @param path the path
	 * @param isActive the is active
	 * @return the server connection
	 * @see de.htwk.autolat.ServerConnection.ServerConnectionManager#createAndPersistServerConnection(java.util.Date,
	 *      java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public ServerConnection createAndPersistServerConnection(Date lastContact, String name, String path, boolean isActive) {
		ServerConnection ser = createServerConnection(lastContact, name, path, isActive);
		saveServerConnection(ser);
		return ser;
	}
	
	/**
	 * Gets the random server connection.
	 * 
	 * @return the random server connection
	 * @see de.htwk.autolat.ServerConnection.ServerConnectionManager#getRandomServerConnection()
	 */
	@Override
	public ServerConnection getRandomServerConnection() {
		
		ArrayList<ServerConnection> result = (ArrayList<ServerConnection>) getAllActiveServerConnections();
		ServerConnection chosenConnection = null;
		if(result.size()>0) {
			int maxNumber = result.size();
			do {
				Random randomizer = new Random(new Date().getTime());
				int connectionNumber = randomizer.nextInt(maxNumber);
				chosenConnection = result.get(connectionNumber);
			} while (!chosenConnection.getIsActive());
		}
		return chosenConnection;
	}
	
	/**
	 * Gets the all server connections.
	 * 
	 * @return the all server connections
	 * @see de.htwk.autolat.ServerConnection.ServerConnectionManager#getAllServerConnections()
	 */
	@Override
	public List<ServerConnection> getAllServerConnections() {
		String query = "SELECT connection FROM ServerConnectionImpl AS connection";
		DBQuery dbq = DBFactory.getInstance().createQuery(query);
		ArrayList<ServerConnection> result = (ArrayList<ServerConnection>) dbq.list();
		return result;
	}
	
	/**
	 * Gets the server connection by key.
	 * 
	 * @param key the key
	 * @return the server connection by key
	 * @see de.htwk.autolat.ServerConnection.ServerConnectionManager#getServerConnectionByKey(long)
	 */
	@Override
	public ServerConnection getServerConnectionByKey(long key) {
		String query = "SELECT connection FROM ServerConnectionImpl AS connection WHERE connection.key = :key";
		DBQuery dbq = DBFactory.getInstance().createQuery(query);
		dbq.setLong("key", key);
		ArrayList<ServerConnection> result = (ArrayList<ServerConnection>) dbq.list();
		return result.get(0);
	}
	
	/**
	 * Sets the last contact.
	 * 
	 * @param serverConnection the new last contact
	 * @see de.htwk.autolat.ServerConnection.ServerConnectionManager#setLastContact(de.htwk.autolat.ServerConnection.ServerConnection)
	 */
	@Override
	public void setLastContact(ServerConnection serverConnection) {
		serverConnection.setLastContact(new Date());
	  updateServerConnection(serverConnection);
	}
	
	/**
	 * Sets the active.
	 * 
	 * @param serverConnection the server connection
	 * @param state the state
	 * @see de.htwk.autolat.ServerConnection.ServerConnectionManager#setActive(de.htwk.autolat.ServerConnection.ServerConnection,
	 *      boolean)
	 */
	@Override
	public void setActive (ServerConnection serverConnection, boolean state) {
		serverConnection.setIsActive(state);
		updateServerConnection(serverConnection);
	}

	/**
	 * Delete server connection.
	 * 
	 * @param serverConn the server conn
	 * @return true, if successful
	 * @see de.htwk.autolat.ServerConnection.ServerConnectionManager#deleteServerConnection(de.htwk.autolat.ServerConnection.ServerConnection)
	 */
	@Override
	public boolean deleteServerConnection(ServerConnection serverConn) {
		try {
			DBFactory.getInstance().deleteObject(serverConn);
			return true;
		}catch (Exception e) {
			return false;
		}
		
	}

	/**
	 * Load server connection by id.
	 * 
	 * @param ID the iD
	 * @return the server connection
	 * @see de.htwk.autolat.ServerConnection.ServerConnectionManager#loadServerConnectionByID(long)
	 */
	@Override
	public ServerConnection loadServerConnectionByID(long ID) {
		return (ServerConnection)DBFactory.getInstance().loadObject(ServerConnectionImpl.class, ID);
	}

	/**
	 * Save server connection.
	 * 
	 * @param serverConn the server conn
	 * @see de.htwk.autolat.ServerConnection.ServerConnectionManager#saveServerConnection(de.htwk.autolat.ServerConnection.ServerConnection)
	 */
	@Override
	public void saveServerConnection(ServerConnection serverConn) {
		DBFactory.getInstance().saveObject(serverConn);
	}

	/**
	 * Update server connection.
	 * 
	 * @param serverConn the server conn
	 * @see de.htwk.autolat.ServerConnection.ServerConnectionManager#updateServerConnection(de.htwk.autolat.ServerConnection.ServerConnection)
	 */
	@Override
	public void updateServerConnection(ServerConnection serverConn) {
		DBFactory.getInstance().updateObject(serverConn);
	}
	
	/**
	 * Gets the all active server connections.
	 * 
	 * @return the all active server connections
	 * @see de.htwk.autolat.ServerConnection.ServerConnectionManager#getAllActiveServerConnections()
	 */
	@Override
	public List<ServerConnection> getAllActiveServerConnections() {
		
		ArrayList<ServerConnection> result = (ArrayList<ServerConnection>) getAllServerConnections();
		Iterator<ServerConnection> connectionIterator = result.iterator();
		while(connectionIterator.hasNext()) {
			ServerConnection temp = connectionIterator.next();
			if(!temp.getIsActive()) connectionIterator.remove();
		}
		return result;		
	}
	
	/**
	 * Gets the all used connections.
	 * 
	 * @return the all used connections
	 * @see de.htwk.autolat.ServerConnection.ServerConnectionManager#getAllUsedConnections()
	 */
	@Override
	public List<ServerConnection> getAllUsedConnections() {
		String query = "SELECT con FROM ServerConnectionImpl AS con WHERE EXISTS(SELECT conf FROM ConfigurationImpl AS conf WHERE conf.serverConnection = con.key)";
		DBQuery dbq = DBFactory.getInstance().createQuery(query);
		List<ServerConnection> result = (List<ServerConnection>) dbq.list();
		return result;
	}
	
	/**
	 * @see de.htwk.autolat.ServerConnection.ServerConnectionManager#getOneServerConnectionByURL(java.lang.String)
	 */
	@Override
	public ServerConnection getOneServerConnectionByURL(String URL) {
		String query = "SELECT con FROM ServerConnectionImpl AS con WHERE path = '"+URL+"'";
		DBQuery dbq = DBFactory.getInstance().createQuery(query);
		List<ServerConnection> result = (List<ServerConnection>) dbq.list();
		return (result.size()>0 ? result.get(0) : null);
	}
	
	@Override
	public void saveOrUpdateServerConnection(ServerConnection serverConnection) {
		
		if(serverConnection.getKey() == null) {
			saveServerConnection(serverConnection);
		}
		else {
			updateServerConnection(serverConnection);
		}
	}
	
	/**
	 * Gets the single instance of ServerConnectionManagerImpl.
	 * 
	 * @return single instance of ServerConnectionManagerImpl
	 */
	public static ServerConnectionManagerImpl getInstance() {
		return INSTANCE;
	}

}
