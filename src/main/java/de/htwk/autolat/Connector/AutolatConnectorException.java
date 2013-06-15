package de.htwk.autolat.Connector;

/**
 * Base class for errors thrown by the AutolatConnector* classes.
 * 
 * Also used for uncategorized errors. Clients may inspect the cause of the exception
 * for further information. Its content depends on the client implementation.
 * 
 * @author Bertram Felgenhauer
 */
public class AutolatConnectorException extends Exception
{
	private static final long serialVersionUID = 4222756769842094745L;

	public AutolatConnectorException(String msg)
	{
		super(msg);
	}
	
	public AutolatConnectorException(String msg, Exception cause)
	{
		super(msg, cause);
	}
}
