package com.alexgilleran.hiitme.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "program")
public class ProgramData {

	@DatabaseField(generatedId = true, columnName = "_id")
	private long id;

	/**
	 * Name of the program
	 */
	@DatabaseField
	private String name;
	/**
	 * Description *
	 */
	@DatabaseField
	private String description;

	@DatabaseField(canBeNull = false, foreign = true, foreignAutoCreate = true)
	private ProgramNode programNode;

	public ProgramData() {

	}

	protected ProgramData(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public ProgramNode getAssociatedNode() {
		if (programNode == null) {
			programNode = new ProgramNode();
		}

		return programNode;
	}
}