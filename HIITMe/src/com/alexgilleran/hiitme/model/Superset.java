package com.alexgilleran.hiitme.model;

import java.util.List;

/**
 * A group of one or more sets, used to encapsulate reps.
 * 
 * @author Alex Gilleran
 * 
 */
public interface Superset {

	/** Get how many times this {@link Superset} is to be repeated */
	int getRepCount();

	/** Get all the sets in the {@link Superset}, in order */
	List<Exercise> getExercises();
}
