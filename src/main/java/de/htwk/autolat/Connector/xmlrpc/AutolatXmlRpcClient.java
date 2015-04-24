package de.htwk.autolat.Connector.xmlrpc;

import java.net.URL;

import de.htwk.autolat.Connector.AutolatConnectorException;
import de.htwk.autolat.Connector.AutolatConnectorTimeoutException;

import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcFault;

/**
 * Adapter for XmlRpcClient class, adding timeout handling.
 *
 * @author Bertram Felgenhauer
 */
public class AutolatXmlRpcClient
{
	private final URL url;
	private final boolean stream;
	private int timeout;
	
	public AutolatXmlRpcClient(URL url, boolean stream, int timeout)
	{
		this.url = url;
		this.stream = stream;
		this.timeout = timeout;
	}

	public AutolatXmlRpcClient(URL url, boolean stream)
	{
		this.url = url;
		this.stream = stream;
		this.timeout = 20000; // 20 seconds
	}

	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}
	
	public Object invoke(final String function, final Object[] args)
		throws XmlRpcFault, AutolatConnectorException
	{
		final InvocationResult result = new InvocationResult();

		Thread worker = new Thread() {
			public void run() {
				XmlRpcClient client = new XmlRpcClient(url, stream);

				// TODO: This is just a workaround to make the signature check working
				// it would be better to calculate the signature ignoring whitespace at the beginning
				client.setTrimCharData(false);

				try {
					result.result = client.invoke(function, args);
					result.hasResult = true;
				} catch (XmlRpcFault fault) {
					System.out.println("Function: " + function);
					for(Object aO : args) {
						System.out.println("arg: " + aO.toString());
					}
					fault.printStackTrace();
					result.fault = fault;
				}
				catch (RuntimeException exc) {
					// e.g. XmlRpcException
					result.exc = exc;
				}
			}
		};
		worker.start();
		try {
			worker.join(timeout);
		}
		catch (InterruptedException e) {
			worker.interrupt();
		    Thread.currentThread().interrupt();
		}
		worker.interrupt();
		if (result.hasResult)
			return result.result;
		if (result.fault != null)
			throw result.fault;
		if (result.exc != null)
			throw result.exc;
		throw new AutolatConnectorTimeoutException("timeout in '" + function + "' request");
	}
	
	private class InvocationResult {
		public volatile boolean     hasResult = false;
		public volatile Object      result;
		public volatile XmlRpcFault fault;
		public volatile RuntimeException exc;
	}
}
