package de.htwk.autolat.Connector.types;

public class Right<A, B> implements Either<A, B>
{
	private final B right;

	public Right(B _right)
	{
		right = _right;
	}

	public String toString()
	{
		return "Right(" + right + ")";
	}

	public A getLeft()
	{
		return null;
	}

	public B getRight()
	{
		return right;
	}

	public boolean isLeft()
	{
		return false;
	}

	public boolean isRight()
	{
		return true;
	}

	@Override
	public boolean equals(Object other)
	{
		if (! (other instanceof Right<?, ?>))
			return false;
		Right<?, ?> oRight = (Right<?, ?>) other;
		return right.equals(oRight);
	}

	@Override
	public int hashCode()
	{
		return right.hashCode() * 37;
	}
}
