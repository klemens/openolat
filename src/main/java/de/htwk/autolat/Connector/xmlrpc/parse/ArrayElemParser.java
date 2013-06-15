/**
 * 
 */
package de.htwk.autolat.Connector.xmlrpc.parse;

import redstone.xmlrpc.XmlRpcArray;

public class ArrayElemParser<A> implements Parser<A>
{
	final int       index;
	final Parser<A> inner;
	
	public ArrayElemParser(int _index, Parser<A> _inner)
	{
		index = _index;
		inner = _inner;
	}
	
	public A parse(Object val) throws ParseErrorBase
	{
		if (val == null)
			throw new ParseError("Expected XmlRpcArray but got null.");
		if (!(val instanceof XmlRpcArray))
			throw new ParseError("Expected XmlRpcArray but got " + val.getClass() + ".");
		XmlRpcArray elems = (XmlRpcArray) val;
		Object value = elems.get(index);
		if (value == null)
			throw new ParseError("Expected XmlRpcArray containing at least " + (index + 1) + " elements.");
		try {
			return inner.parse(value);
		}
		catch (ParseErrorBase e) {
			throw new NestedParseError("while parsing XmlRpcArray elem " + index, e);
		}
	}
}