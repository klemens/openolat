package de.htwk.autolat.Connector.xmlrpc.parse;

public class AlternativeParser<A> implements Parser<A>
{
	private final Parser<A> alt1;
	private final Parser<A> alt2;
	
	public AlternativeParser(Parser<A> alt1, Parser<A> alt2)
	{
		this.alt1 = alt1;
		this.alt2 = alt2;
	}

	public A parse(Object val) throws ParseErrorBase
	{
		try {
			return alt1.parse(val);
		}
		catch (ParseErrorBase e1) {
			try {
			    return alt2.parse(val);
			}
			catch (ParseErrorBase e2) {
				throw new ParseError("AlternativeParser: No alternative matched.");
			}
		}
	}
}
