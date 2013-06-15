package de.htwk.autolat.Connector.types;

public class Pair<A, B>
{
	private final A first;
	private final B second;
	
	public Pair(A first, B second)
	{
		this.first = first;
		this.second = second;
	}
	
	public A getFirst()
	{
		return first;
	}
	
	public B getSecond()
	{
		return second;
	}
	
	@Override
	public String toString()
	{
		return "Pair(" + first + "," + second + ")";
	}

	@Override
	public boolean equals(Object other)
	{
		if (! (other instanceof Pair<?,?>))
			return false;
		Pair<?, ?> oPair = (Pair<?, ?>) other;
		return first.equals(oPair.getFirst()) && second.equals(oPair.getSecond());
	}
	
	@Override
	public int hashCode()
	{
		return first.hashCode() + 37 * second.hashCode();
	}
}
