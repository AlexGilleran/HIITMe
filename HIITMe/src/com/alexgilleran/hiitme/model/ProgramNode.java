package com.alexgilleran.hiitme.model;

import java.util.List;

/**
 * A group of one or more sets, used to encapsulate reps.
 * 
 * @author Alex Gilleran
 * 
 */
public interface ProgramNode {

	/** Get how many times this {@link ProgramNode} is to be repeated */
	int getTotalReps();

	int getCompletedReps();

	List<ProgramNode> getChildren();

	boolean isFinished();

	void reset();

	void next();

	ProgramNode getCurrentNode();

	Exercise getCurrentExercise();

	Exercise getAttachedExercise();

	boolean hasChildren();

	ProgramNode addChildNode(int repCount);

	void registerObserver(ProgramNodeObserver observer);

	Exercise addChildExercise(String name, int duration,
			Exercise.EffortLevel effortLevel, int repCount);

	void triggerExerciseBroadcast();

	void start();

	int getDuration();
}
