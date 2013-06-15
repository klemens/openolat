package de.htwk.autolat.Connector.xmlrpc.parse;

import de.htwk.autolat.Connector.types.*;

public class EitherParser<A, B> implements Parser<Either<A, B>>
{
	private final Parser<Either<A, B>> parser;
	
	public EitherParser(final Parser<A> aParser, final Parser<B> bParser)
	{
		this.parser = new AlternativeParser<Either<A, B>>(
			new StructFieldParser<Either<A, B>>("Left", new Parser<Either<A, B>>() {
				public Either<A, B> parse(Object val) throws ParseErrorBase {
					return new Left<A, B>(aParser.parse(val));
				}
			}),
			new StructFieldParser<Either<A, B>>("Right", new Parser<Either<A, B>>() {
				public Either<A, B> parse(Object val) throws ParseErrorBase {
					return new Right<A, B>(bParser.parse(val));
				}
			})
		);
	}

	public Either<A, B> parse(Object val) throws ParseErrorBase
	{
		return parser.parse(val);
	}
}
