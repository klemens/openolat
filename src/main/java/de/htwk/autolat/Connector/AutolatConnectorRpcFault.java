package de.htwk.autolat.Connector;

/**
 * This exception is thrown if an RPC request fails, for example due to invalid
 * arguments or an error on the server side.
 *  
 * @author Bertram Felgenhauer
 */
public class AutolatConnectorRpcFault extends AutolatConnectorException
{
	private static final long serialVersionUID = 6623295073707724118L;

	public AutolatConnectorRpcFault(String msg)
	{
		super(msg);
	}

	public AutolatConnectorRpcFault(String msg, Exception cause)
	{
		super(msg, cause);
	}
}
