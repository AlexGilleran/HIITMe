package com.alexgilleran.hiitme.model;

import java.util.List;

/**
 * A group of one or more sets, used to encapsulate reps.
 * 
 * @author Alex Gilleran
 * 
 */
public interface ExerciseGroup {

	/** Get how many times this {@link ExerciseGroup} is to be repeated */
	int getRepCount();

	List<ExerciseGroup> getExerciseGroups();

	/** Get all the sets in the {@link ExerciseGroup}, in order */
	Exercise getExercise();
	
	ExerciseGroup getParent();
	
	Exercise getFirstExercise();
}
