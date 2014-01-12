package com.alexgilleran.hiitme.model;

public class Exercise extends DatabaseModel {
	private String name;
	private int duration;
	private EffortLevel effortLevel;
	private Node exerciseGroup;

	public Exercise() {
		super();
	}

	public Exercise(int duration, EffortLevel effortLevel, Node node) {
		this.duration = duration;
		this.effortLevel = effortLevel;
		this.exerciseGroup = node;
	}

	public Exercise(String name, int duration, EffortLevel effortLevel, Node node) {
		this(duration, effortLevel, node);

		this.name = name;
	}

	public int getDuration() {
		return duration;
	}

	public EffortLevel getEffortLevel() {
		return effortLevel;
	}

	public Node getParentNode() {
		return exerciseGroup;
	}

	public void setExerciseGroup(Node exerciseGroup) {
		this.exerciseGroup = exerciseGroup;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setEffortLevel(EffortLevel effortLevel) {
		this.effortLevel = effortLevel;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
