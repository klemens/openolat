package de.htwk.autolat.Connector.types;

public class Left<A, B> implements Either<A, B>
{
	private final A left;

	public Left(A _left)
	{
		left = _left;
	}

	@Override
	public String toString()
	{
		return "Left(" + left + ")";
	}

	public A getLeft()
	{
		return left;
	}

	public B getRight()
	{
		return null;
	}

	public boolean isLeft()
	{
		return true;
	}

	public boolean isRight()
	{
		return false;
	}

	@Override
	public boolean equals(Object other)
	{
		if (! (other instanceof Left<?, ?>))
			return false;
		Left<?, ?> oLeft = (Left<?, ?>) other;
		return left.equals(oLeft);
	}
	
	@Override
	public int hashCode()
	{
		return left.hashCode();
	}
}
