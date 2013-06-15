package de.htwk.autolat.Connector.xmlrpc.parse;

import redstone.xmlrpc.XmlRpcArray;
import de.htwk.autolat.Connector.types.Pair;

public class PairParser<A, B> implements Parser<Pair<A, B>>
{
	private final Parser<A> aParser;
	private final Parser<B> bParser;
	
	public PairParser(Parser<A> aParser, Parser<B> bParser)
	{
		this.aParser = aParser;
		this.bParser = bParser;
	}

	public Pair<A, B> parse(Object val) throws ParseErrorBase
	{
		if (val == null)
			throw new ParseError("Expected XmlRpcArray but got null.");
		if (!(val instanceof XmlRpcArray))
			throw new ParseError("Expected XmlRpcArray but got " + val.getClass() + ".");
		XmlRpcArray elems = (XmlRpcArray) val;
		if (elems.size() != 2)
			throw new ParseError("Expected XmlRpcArray of size 2.");
		try {
			return new Pair<A, B>(aParser.parse(elems.get(0)), bParser.parse(elems.get(1)));
		}
		catch (ParseErrorBase e) {
			throw new NestedParseError("while parsing pair", e);
		}
	}
}
