package de.htwk.autolat.Connector.xmlrpc.serialize;

import redstone.xmlrpc.XmlRpcArray;
import de.htwk.autolat.Connector.types.Pair;

@SuppressWarnings("unchecked")
public class PairSerializer<A, B> implements Serializer<Pair<A, B>>
{
	private final Serializer<A> aSerializer;
	private final Serializer<B> bSerializer;

	public PairSerializer(Serializer<A> aSerializer, Serializer<B> bSerializer)
	{
		this.aSerializer = aSerializer;
		this.bSerializer = bSerializer;
	}
	
	public Object serialize(Pair<A, B> val)
	{
		XmlRpcArray result = new XmlRpcArray();
		result.add(aSerializer.serialize(val.getFirst()));
		result.add(bSerializer.serialize(val.getSecond()));
		return result;
	}
}
