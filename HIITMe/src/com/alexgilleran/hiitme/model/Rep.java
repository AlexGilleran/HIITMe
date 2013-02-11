package com.alexgilleran.hiitme.model;

/**
 * A single part of activity - e.g. "work for 20 seconds" or
 * "rest for 10 seconds"
 * 
 * @author Alex Gilleran
 */
public interface Rep {
	/** Get the displayable name of the rep */
	String getName();
	
	/** Get the duration of the rep, in milliseconds */
	int getDuration();
	
	/** Get the type of the rep */
	EffortLevel getEffortLevel();
	
	/** A simple representation of the effort level of the set */
	enum EffortLevel {
		HARD,
		EASY,
		REST
	}
}
