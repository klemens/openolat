package de.htwk.autolat.Connector.xmlrpc.serialize;

public interface Serializer<A>
{
	public Object serialize(A val);
}
