package de.htwk.autolat.Connector.xmlrpc.serialize;

public class StringSerializer implements Serializer<String>
{
	private static final StringSerializer inst = new StringSerializer();

	public static Serializer<String> getInstance()
	{
		return inst;
	}
	
	public Object serialize(String val)
	{
		return val;
	}
}
