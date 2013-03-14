package com.alexgilleran.hiitme.model.impl;

import java.util.ArrayList;
import java.util.List;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.ProgramNode;

public class ProgramImpl extends ProgramNodeImpl implements Program {
	private List<ProgramObserver> observers = new ArrayList<ProgramObserver>();
	private long id;

	/** Name of the program */
	private String name;
	/** Description **/
	private String description;

	public ProgramImpl(long id, String name, String description) {
		super(1, null);

		this.id = id;
		this.name = name;
		this.description = description;
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

	public void registerObserver(ProgramObserver observer) {
		observers.add(observer);
	}

	private void broadcastNextExercise(Exercise newExercise) {
		for (ProgramObserver observer : observers) {
			observer.onNextExercise(newExercise);
		}
	}

	private void broadcastFinish() {
		for (ProgramObserver observer : observers) {
			observer.onFinish();
		}
	}

	private void broadcastRepFinish(ProgramNode superset, int remainingReps) {
		for (ProgramObserver observer : observers) {
			observer.onRepFinish(superset, remainingReps);
		}
	}

	public interface ProgramObserver {
		void onNextExercise(Exercise newExercise);

		void onRepFinish(ProgramNode superset, int remainingReps);

		void onFinish();
	}

	@Override
	public void start() {
		
	}
}
