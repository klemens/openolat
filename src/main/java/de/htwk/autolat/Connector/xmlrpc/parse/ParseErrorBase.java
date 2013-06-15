package de.htwk.autolat.Connector.xmlrpc.parse;

public class ParseErrorBase extends Exception
{
	private static final long serialVersionUID = 74601528884153980L;

	protected ParseErrorBase(String msg)
	{
		super(msg);
	}
	
	protected ParseErrorBase(String msg, Exception cause)
	{
		super(msg, cause);
	}
}
