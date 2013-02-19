package com.alexgilleran.hiitme.model.impl;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Superset;

public class ExerciseImpl implements Exercise {
	private String name;
	private int duration;
	private EffortLevel effortLevel;
	private Superset superset;

	public ExerciseImpl(String name, int duration, EffortLevel effortLevel,
			Superset superset) {
		super();
		this.name = name;
		this.duration = duration;
		this.effortLevel = effortLevel;
		this.superset = superset;
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
	public Superset getSuperset() {
		return superset;
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
}
