package com.alexgilleran.hiitme.model;

import java.util.List;

/**
 * Represents an entire HIIT program... e.g. the Tabata protocol is an instance
 * of {@link Program}.
 * 
 * @author Alex Gilleran
 * 
 */
public interface Program extends ExerciseGroup {
	public static final String PROGRAM_ID_NAME = "programid";

	/** The id of this program */
	long getId();

	/** Gets the name of the program */
	String getName();

	/** Gets a description of the program */
	String getDescription();

	/** Gets the rep groups of the program, in order */
	List<ExerciseGroup> getSupersets();

	/** Gets the warmup rep */
	Exercise getWarmUp();
}
