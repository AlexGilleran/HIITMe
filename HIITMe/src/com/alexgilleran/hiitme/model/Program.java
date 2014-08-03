package com.alexgilleran.hiitme.model;

import com.alexgilleran.hiitme.programrunner.ProgramNodeState;
import com.alexgilleran.hiitme.util.PeekaheadLinkedList;
import com.alexgilleran.hiitme.util.PeekaheadQueue;

public class Program extends DatabaseModel {

	public static final String PROGRAM_ID_NAME = "PROGRAM_ID";

	/**
	 * Name of the program
	 */
	private String name;

	private Node programNode;

	public Program(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Node getAssociatedNode() {
		if (programNode == null) {
			programNode = new Node();
		}

		return programNode;
	}

	public void setAssociatedNode(Node programNode) {
		this.programNode = programNode;
	}

	@Override
	public String toString() {
		return getName();
	}

	public PeekaheadQueue<Node> asQueue() {
		PeekaheadQueue<Node> result = new PeekaheadLinkedList<Node>();

		ProgramNodeState state = new ProgramNodeState(getAssociatedNode());

		while (!state.isFinished()) {
			result.add(state.getCurrentExercise().getParentNode());
			state.next();
		}

		return result;
	}
}