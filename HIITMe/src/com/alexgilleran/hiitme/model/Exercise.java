package com.alexgilleran.hiitme.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "exercise")
public class Exercise extends Model {
	@Column(name = "name")
	private String name;
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
		this.name = name;
		this.duration = duration;
		this.effortLevel = effortLevel;
		this.exerciseGroup = node;
	}

	public String getName() {
		return name;
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

	/** A simple representation of the effort level of the set */
	public enum EffortLevel {
		HARD, EASY, REST
	}

	@Override
	public String toString() {
		return getName();
	}
}
