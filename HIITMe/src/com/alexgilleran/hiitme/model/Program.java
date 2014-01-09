package com.alexgilleran.hiitme.model;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.alexgilleran.hiitme.programrunner.ProgramNodeState;
import com.alexgilleran.hiitme.util.PeekaheadQueue;
import com.alexgilleran.hiitme.util.PeekaheadLinkedList;

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

	public void setAssociatedNode(ProgramNode programNode) {
		this.programNode = programNode;
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

	public PeekaheadQueue<ProgramNode> asQueue() {
		PeekaheadQueue<ProgramNode> result = new PeekaheadLinkedList<ProgramNode>();

		ProgramNodeState state = new ProgramNodeState(getAssociatedNode());

		while (!state.isFinished()) {
			result.add(state.getCurrentExercise().getParentNode());
			state.next();
		}

		return result;
	}
}