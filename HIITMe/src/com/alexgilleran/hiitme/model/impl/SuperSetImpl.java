package com.alexgilleran.hiitme.model.impl;

import java.util.List;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Superset;

public class SuperSetImpl implements Superset {
	private int repCount;
	private List<Exercise> reps;

	public SuperSetImpl(int repCount, List<Exercise> reps) {
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
