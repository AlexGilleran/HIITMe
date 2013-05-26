package com.alexgilleran.hiitme.model;

public class Exercise extends ExerciseData {

	public Exercise() {
		super();
	}

	public Exercise(String name, int duration, EffortLevel effortLevel,
			ProgramNode node) {
		super(name, duration, effortLevel, node);
	}

	@Override
	public String toString() {
		return getName();
	}
}
