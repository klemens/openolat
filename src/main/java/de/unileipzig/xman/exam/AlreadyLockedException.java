package de.unileipzig.xman.exam;

/**
 * Thrown when trying to acquire an already locked lock.
 * Contains the name of the owner of the lock.
 */
public class AlreadyLockedException extends Exception {
	private static final long serialVersionUID = 4750270312469323021L;
	
	public AlreadyLockedException(String name) {
		super(name);
	}
	
	public String getName() {
		return getMessage();
	}
}