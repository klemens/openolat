package de.htwk.autolat.Connector.xmlrpc.parse;

public class DoubleParser implements Parser<Double>
{
	private static final DoubleParser inst = new DoubleParser();

	public static Parser<Double> getInstance()
	{
		return inst;
	}
	
	public Double parse(Object val) throws ParseErrorBase
	{
		if (val == null)
			throw new ParseError("Expected (XmlRpc)Double but got null.");
		if (! (val instanceof Double))
			throw new ParseError("Expected (XmlRpc)Double but got " + val.getClass() + ".");
		return (Double) val;
	}
}
