package de.htwk.autolat.Connector.types;

public interface Either<A, B>
{
	public boolean isLeft();
	public boolean isRight();
	public A getLeft();
	public B getRight();
}
