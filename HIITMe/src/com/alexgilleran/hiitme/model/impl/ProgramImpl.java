package com.alexgilleran.hiitme.model.impl;

import com.alexgilleran.hiitme.model.Program;

public class ProgramImpl extends ProgramNodeImpl implements Program {
	private long id;

	/** Name of the program */
	private String name;
	/** Description **/
	private String description;

	public ProgramImpl(long id, String name, String description, int repCount) {
		super(repCount);

		this.id = id;
		this.name = name;
		this.description = description;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public void start() {
		// this.broadcastNextExercise(getCurrentExercise());
	}
}
