package de.htwk.autolat.Connector;

/**
 * This exception is thrown if an RPC request runs into a timeout.
 *  
 * @author Bertram Felgenhauer
 */
public class AutolatConnectorTimeoutException extends AutolatConnectorException
{
	private static final long serialVersionUID = 791989037120997794L;

	public AutolatConnectorTimeoutException(String msg) {
		super(msg);
	}
}
