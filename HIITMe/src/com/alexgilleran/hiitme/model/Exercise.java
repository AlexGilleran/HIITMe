package com.alexgilleran.hiitme.model;

public class Exercise extends DatabaseModel implements Cloneable {
	private String name;
	private int duration;
	private EffortLevel effortLevel;
	private Node exerciseGroup;

	public Exercise() {
		super();

		this.duration = 0;
		this.effortLevel = EffortLevel.EASY;
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

	public void setNode(Node exerciseGroup) {
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

	/**
	 * Gets the number of minutes this exercise lasts, rounded down.
	 */
	public int getMinutes() {
		return duration / 1000 / 60;
	}

	/**
	 * Gets the number of seconds this exercise lasts, not including minutes.
	 */
	public int getSeconds() {
		return duration / 1000 % 60;
	}

	@Override
	public Exercise clone() {
		Exercise clone = new Exercise();
		clone.setDuration(duration);
		clone.setEffortLevel(effortLevel);
		clone.setName(name);

		return clone;
	}
}
