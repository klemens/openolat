package de.htwk.autolat.Connector.xmlrpc.parse;

import java.util.ArrayList;
import java.util.List;

import redstone.xmlrpc.XmlRpcArray;

public class ListParser<A> implements Parser<List<A>>
{
	private final Parser<A> aParser;
	
	public ListParser(Parser<A> aParser)
	{
		this.aParser = aParser;
	}
	
	public List<A> parse(Object val) throws ParseErrorBase
	{
		if (val == null)
			throw new ParseError("Expected XmlRpcArray but got null.");
		if (!(val instanceof XmlRpcArray))
			throw new ParseError("Expected XmlRpcArray but got " + val.getClass() + ".");
		XmlRpcArray elems = (XmlRpcArray) val;
		
		ArrayList<A> result = new ArrayList<A>(elems.size());
		
		try {
			for (int i = 0; i < elems.size(); i++) {
				result.add(aParser.parse(elems.get(i)));
			}
			return result;
		}
		catch (ParseErrorBase e) {
			throw new NestedParseError("while parsing a list", e);
		}
	}

}
