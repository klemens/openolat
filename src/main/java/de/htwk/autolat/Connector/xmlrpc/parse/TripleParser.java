package de.htwk.autolat.Connector.xmlrpc.parse;

import redstone.xmlrpc.XmlRpcArray;
import de.htwk.autolat.Connector.types.Triple;

public class TripleParser<A, B, C> implements Parser<Triple<A, B, C>>
{
	private final Parser<A> aParser;
	private final Parser<B> bParser;
	private final Parser<C> cParser;

	public TripleParser(Parser<A> aParser, Parser<B> bParser, Parser<C> cParser)
	{
		this.aParser = aParser;
		this.bParser = bParser;
		this.cParser = cParser;
	}

	public Triple<A, B, C> parse(Object val) throws ParseErrorBase
	{
		if (val == null)
			throw new ParseError("Expected XmlRpcArray but got null.");
		if (!(val instanceof XmlRpcArray))
			throw new ParseError("Expected XmlRpcArray but got " + val.getClass() + ".");
		XmlRpcArray elems = (XmlRpcArray) val;
		if (elems.size() != 3)
			throw new ParseError("Expected XmlRpcArray of size 3.");
		try {
			return new Triple<A, B, C>(
				aParser.parse(elems.get(0)),
				bParser.parse(elems.get(1)),
				cParser.parse(elems.get(2)));
		}
		catch (ParseErrorBase e) {
			throw new NestedParseError("while parsing triple", e);
		}
	}
}
