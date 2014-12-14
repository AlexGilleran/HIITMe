package com.alexgilleran.hiitme.model;

import com.alexgilleran.hiitme.programrunner.ProgramNodeState;
import com.alexgilleran.hiitme.util.PeekaheadLinkedList;
import com.alexgilleran.hiitme.util.PeekaheadQueue;

public class Program extends ProgramMetaData {

	private Node programNode;

	public Program(String name) {
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