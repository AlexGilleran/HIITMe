package com.alexgilleran.hiitme.model;

import com.alexgilleran.hiitme.model.impl.ProgramImpl.ProgramObserver;

/**
 * Represents an entire HIIT program... e.g. the Tabata protocol is an instance
 * of {@link Program}.
 * 
 * @author Alex Gilleran
 * 
 */
public interface Program extends ProgramNode {
	public static final String PROGRAM_ID_NAME = "programid";

	/** The id of this program */
	long getId();

	/** Gets the name of the program */
	String getName();

	/** Gets a description of the program */
	String getDescription();


	int getTotalReps();

	void start();

	void registerObserver(ProgramObserver observer);
}
