package com.alexgilleran.hiitme.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.alexgilleran.hiitme.programrunner.ProgramNodeState;

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

	public Queue<ProgramNode> asQueue() {
		Queue<ProgramNode> result = new LinkedList<ProgramNode>();

		ProgramNodeState state = new ProgramNodeState(getAssociatedNode());

		while (!state.isFinished()) {
			result.add(state.getCurrentNode());
			state.next();
		}

		return result;
	}
}