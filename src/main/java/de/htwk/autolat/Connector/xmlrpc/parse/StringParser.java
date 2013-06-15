package de.htwk.autolat.Connector.xmlrpc.parse;

public class StringParser implements Parser<String>
{
	private static final StringParser inst = new StringParser();

	public static Parser<String> getInstance()
	{
		return inst;
	}
	
	public String parse(Object val) throws ParseErrorBase
	{
		if (val == null)
			throw new ParseError("Expected (XmlRpc)String but got null.");
		if (! (val instanceof String))
			throw new ParseError("Expected (XmlRpc)String but got " + val.getClass() + ".");
		return (String) val;
	}
}
