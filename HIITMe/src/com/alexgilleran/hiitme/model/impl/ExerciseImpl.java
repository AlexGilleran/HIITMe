package com.alexgilleran.hiitme.model.impl;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.ProgramNode;

public class ExerciseImpl implements Exercise {
	private String name;
	private int duration;
	private EffortLevel effortLevel;
	private ProgramNode exerciseGroup;

	protected ExerciseImpl(String name, int duration, EffortLevel effortLevel,
			ProgramNode superset) {
		this.name = name;
		this.duration = duration;
		this.effortLevel = effortLevel;
		this.exerciseGroup = superset;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getDuration() {
		return duration;
	}

	@Override
	public EffortLevel getEffortLevel() {
		return effortLevel;
	}

	@Override
	public ProgramNode getParentNode() {
		return exerciseGroup;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + duration;
		result = prime * result
				+ ((effortLevel == null) ? 0 : effortLevel.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExerciseImpl other = (ExerciseImpl) obj;
		if (duration != other.duration)
			return false;
		if (effortLevel != other.effortLevel)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return name;
	}
}
