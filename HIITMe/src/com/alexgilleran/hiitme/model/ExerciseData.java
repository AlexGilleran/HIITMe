package com.alexgilleran.hiitme.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "exercise")
public class ExerciseData {

	@DatabaseField(generatedId = true, columnName = "_id")
	private long id;
	@DatabaseField
	private String name;
	@DatabaseField
	private int duration;
	@DatabaseField
	private EffortLevel effortLevel;
	@DatabaseField(foreign = true)
	private ProgramNode exerciseGroup;

	public ExerciseData() {

	}

	public ExerciseData(String name, int duration, EffortLevel effortLevel,
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
}