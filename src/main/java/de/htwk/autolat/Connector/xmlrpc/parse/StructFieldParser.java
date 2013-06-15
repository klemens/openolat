/**
 * 
 */
package de.htwk.autolat.Connector.xmlrpc.parse;

import redstone.xmlrpc.XmlRpcStruct;

public class StructFieldParser<A> implements Parser<A>
{
	final String    name;
	final Parser<A> inner;
	
	public StructFieldParser(String _name, Parser<A> _inner)
	{
		name = _name;
		inner = _inner;
	}
	
	public A parse(Object val) throws ParseErrorBase
	{
		if (val == null)
			throw new ParseError("Expected XmlRpcStruct but got null.");
		if (!(val instanceof XmlRpcStruct))
			throw new ParseError("Expected XmlRpcStruct but got " + val.getClass() + ".");
		XmlRpcStruct fields = (XmlRpcStruct) val;
		Object value = fields.get(name);
		if (value == null)
			throw new ParseError("Expected XmlRpcStruct containing a field '" + name + "'.");
		try {
			return inner.parse(value);
		}
		catch (ParseErrorBase e) {
			throw new NestedParseError("while parsing XmlRpcStruct field '" + name + "'", e);
		}
	}
}