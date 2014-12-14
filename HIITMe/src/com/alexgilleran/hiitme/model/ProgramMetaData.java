package com.alexgilleran.hiitme.model;

/**
 * Basic version of a program that includes just the metadata, intended for use in displaying lists.
 */
public class ProgramMetaData extends DatabaseModel {
	public static final String PROGRAM_ID_NAME = "PROGRAM_ID";
	protected String name;
	protected String description;

	public ProgramMetaData() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return getName();
	}
}