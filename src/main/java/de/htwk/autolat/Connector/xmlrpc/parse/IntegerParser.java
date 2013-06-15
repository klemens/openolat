package de.htwk.autolat.Connector.xmlrpc.parse;

public class IntegerParser implements Parser<Integer>
{
	private static final IntegerParser inst = new IntegerParser();
	
	public static Parser<Integer> getInstance()
	{
		return inst;
	}
	
	public Integer parse(Object val) throws ParseErrorBase
	{
		String sval = StringParser.getInstance().parse(val);
		try {
			return Integer.valueOf(sval);
		}
		catch (NumberFormatException e) {
			throw new NestedParseError("Expected (XmlRpc)String with Integer value.", e);
		}
	}
}
