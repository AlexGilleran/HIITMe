package com.alexgilleran.hiitme.model;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "program")
public class Program extends Model {

	public static final String PROGRAM_ID_NAME = "PROGRAM_ID";

	/**
	 * Name of the program
	 */
	@Column(name = "name")
	private String name;
	/**
	 * Description *
	 */
	@Column(name = "description")
	private String description;

	@Column(name = "associated_node")
	private ProgramNode programNode;

	public Program() {

	}

	public Program(String name, String description, int reps) {
		this.name = name;
		this.description = description;

		getAssociatedNode().setTotalReps(reps);
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

	@Override
	public void save() {
		ActiveAndroid.beginTransaction();
		try {
			programNode.save();
			super.save();
			ActiveAndroid.setTransactionSuccessful();
		} finally {
			ActiveAndroid.endTransaction();
		}
	}

	@Override
	public String toString() {
		return getName();
	}

}