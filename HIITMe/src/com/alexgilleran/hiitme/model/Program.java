package com.alexgilleran.hiitme.model;


public class Program extends ProgramData {

	public static final String PROGRAM_ID_NAME = "PROGRAM_ID";

	public Program() {

	}

	public Program(String name, String description, int reps) {
		super(name, description);

		getAssociatedNode().setTotalReps(reps);
	}

	@Override
	public String toString() {
		return getName();
	}

}