package de.htwk.autolat.Connector.xmlrpc.serialize;

public class IntegerSerializer implements Serializer <Integer>
{
	private static final IntegerSerializer inst = new IntegerSerializer();

	public static Serializer<Integer> getInstance()
	{
		return inst;
	}

	public Object serialize(Integer val)
	{
		return val.toString();
	}
}
