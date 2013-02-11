package com.alexgilleran.hiitme.model;

import java.util.List;

/**
 * A group of one or more sets, used to encapsulate reps.
 * 
 * @author Alex Gilleran
 * 
 */
public interface RepGroup {

	/** Get how many times this {@link RepGroup} is to be repeated */
	int getRepCount();

	/** Get all the sets in the {@link RepGroup}, in order */
	List<Rep> getReps();
}
