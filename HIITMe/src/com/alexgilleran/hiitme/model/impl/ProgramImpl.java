package com.alexgilleran.hiitme.model.impl;

import com.alexgilleran.hiitme.data.ProgramDao;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Exercise.EffortLevel;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.model.ProgramNodeObserver;

import java.util.List;

public class ProgramImpl implements Program, ProgramNode {
	private static final long DEFAULT_ID = -1;
	private ProgramDao dao;
	private ProgramNode node;

	private long id;
	/**
	 * Name of the program
	 */
	private String name;
	/**
	 * Description *
	 */
	private String description;

	public ProgramImpl(ProgramDao dao, String name, String description,
			int repCount) {
		this(dao, DEFAULT_ID, name, description);

		// If no proper id provided, this isn't from the database so it
		// needs a ProgramNode behind it.
		node = new ProgramNodeImpl(dao, repCount);
	}

	public ProgramImpl(ProgramDao dao, long id, String name, String description) {
		this.dao = dao;
		this.name = name;
		this.description = description;
		this.id = id;
	}

	private ProgramNode getAssociatedProgramNode() {
		if (node == null)
			node = dao.getProgramNode(id);

		return node;
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
	public int getCompletedReps() {
		return getAssociatedProgramNode().getCompletedReps();
	}

	@Override
	public List<ProgramNode> getChildren() {
		return getAssociatedProgramNode().getChildren();
	}

	@Override
	public boolean isFinished() {
		return getAssociatedProgramNode().isFinished();
	}

	@Override
	public void reset() {
		getAssociatedProgramNode().reset();
	}

	@Override
	public void next() {
		getAssociatedProgramNode().next();
	}

	@Override
	public ProgramNode getCurrentNode() {
		return getAssociatedProgramNode().getCurrentNode();
	}

	@Override
	public Exercise getCurrentExercise() {
		return getAssociatedProgramNode().getCurrentExercise();
	}

	@Override
	public Exercise getAttachedExercise() {
		return getAssociatedProgramNode().getAttachedExercise();
	}

	@Override
	public boolean hasChildren() {
		return getAssociatedProgramNode().hasChildren();
	}

	@Override
	public ProgramNode addChildNode(int repCount) {
		return getAssociatedProgramNode().addChildNode(repCount);
	}

	@Override
	public void registerObserver(ProgramNodeObserver observer) {
		getAssociatedProgramNode().registerObserver(observer);
	}

	@Override
	public Exercise addChildExercise(String name, int duration,
			EffortLevel effortLevel, int repCount) {
		return getAssociatedProgramNode().addChildExercise(name, duration,
				effortLevel, repCount);
	}

	@Override
	public void triggerExerciseBroadcast() {
		getAssociatedProgramNode().triggerExerciseBroadcast();
	}

	@Override
	public void start() {
		getAssociatedProgramNode().start();
	}

	@Override
	public int getDuration() {
		return getAssociatedProgramNode().getDuration();
	}

	@Override
	public int getTotalReps() {
		return getAssociatedProgramNode().getDuration();
	}
}