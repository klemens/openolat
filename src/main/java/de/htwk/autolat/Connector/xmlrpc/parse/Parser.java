package de.htwk.autolat.Connector.xmlrpc.parse;

public interface Parser<A>
{
	public A parse(Object val) throws ParseErrorBase;
}
