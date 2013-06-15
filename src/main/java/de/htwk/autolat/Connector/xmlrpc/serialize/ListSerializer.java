package de.htwk.autolat.Connector.xmlrpc.serialize;

import java.util.List;

import redstone.xmlrpc.XmlRpcArray;

@SuppressWarnings("unchecked")
public class ListSerializer<A> implements Serializer<List<A>>
{
	private final Serializer<A> aSerializer;
	
	public ListSerializer(final Serializer<A> aSerializer)
	{
		this.aSerializer = aSerializer;
	}
	
	public Object serialize(List<A> val)
	{
		XmlRpcArray result = new XmlRpcArray();
		for (int i = 0; i < result.size(); i++)
			result.add(aSerializer.serialize(val.get(i)));
		return result;
	}

}
