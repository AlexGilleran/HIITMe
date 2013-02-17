package com.alexgilleran.hiitme.model.impl;

import java.util.List;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Superset;

public class RepGroupImpl implements Superset {
	private int repCount;
	private List<Exercise> reps;
	
	public RepGroupImpl(int repCount, List<Exercise> reps) {
		super();
		this.repCount = repCount;
		this.reps = reps;
	}

	@Override
	public int getRepCount() {
		return repCount;
	}

	@Override
	public List<Exercise> getExercises() {
		return reps;
	}
}
