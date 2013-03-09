package com.alexgilleran.hiitme.model.impl;

import java.util.List;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.ExerciseGroup;

public class ExerciseGroupImpl implements ExerciseGroup {
	private int repCount;
	private List<ExerciseGroup> children;
	private Exercise exercise;
	private ExerciseGroup parent;

	private ExerciseGroupImpl(int repCount, ExerciseGroup parent) {
		this.repCount = repCount;
		this.parent = parent;
	}

	public ExerciseGroupImpl(int repCount, ExerciseGroup parent,
			List<ExerciseGroup> children) {
		this(repCount, parent);

		this.children = children;

	}

	public ExerciseGroupImpl(int repCount, ExerciseGroup parent,
			Exercise exercise) {
		this(repCount, parent);

		this.exercise = exercise;
	}

	@Override
	public int getRepCount() {
		return repCount;
	}

	@Override
	public Exercise getExercise() {
		return exercise;
	}

	@Override
	public List<ExerciseGroup> getExerciseGroups() {
		return children;
	}

	@Override
	public ExerciseGroup getParent() {
		return parent;
	}

	@Override
	public Exercise getFirstExercise() {
		if (exercise != null) {
			return exercise;
		}

		return children.get(0).getFirstExercise();
	}
}
