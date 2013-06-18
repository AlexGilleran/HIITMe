package com.alexgilleran.hiitme.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "exercise")
public class Exercise extends Model {
	@Column(name = "duration")
	private int duration;
	@Column(name = "effort_level")
	private EffortLevel effortLevel;
	@Column(name = "exercise_group")
	private ProgramNode exerciseGroup;

	public Exercise() {
		super();
	}

	public Exercise(String name, int duration, EffortLevel effortLevel,
			ProgramNode node) {
		this.duration = duration;
		this.effortLevel = effortLevel;
		this.exerciseGroup = node;
	}

	public int getDuration() {
		return duration;
	}

	public EffortLevel getEffortLevel() {
		return effortLevel;
	}

	public ProgramNode getParentNode() {
		return exerciseGroup;
	}

	public ProgramNode getExerciseGroup() {
		return exerciseGroup;
	}

	public void setExerciseGroup(ProgramNode exerciseGroup) {
		this.exerciseGroup = exerciseGroup;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setEffortLevel(EffortLevel effortLevel) {
		this.effortLevel = effortLevel;
	}

	@Override
	public void save() {
		super.save();

		getParentNode().broadcastChanged();
	}
}
