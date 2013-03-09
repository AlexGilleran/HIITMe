package com.alexgilleran.hiitme.programrunner;

import java.util.ArrayList;
import java.util.List;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.ExerciseGroup;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.programrunner.ProgramTracker.ProgramTrackerObserver;

public class ProgramTracker2 implements IProgramTracker {
	private Program program;
	private ExerciseGroup currentExerciseGroup;
	private List<ProgramTrackerObserver> observers = new ArrayList<ProgramTrackerObserver>();

	int currentRepCount;
	int currentExerciseGroupIndex;

	public ProgramTracker2(Program program) {
		this.program = program;
	}

	@Override
	public ExerciseGroup getCurrentSuperset() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Exercise getCurrentExercise() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Program getProgram() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFinished() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getRepCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void start() {
		ExerciseGroup exerciseGroup = program;

		while (exerciseGroup.getExercise() == null) {
			exerciseGroup = exerciseGroup.getExerciseGroups().get(0);
		}

		currentExerciseGroup = exerciseGroup;

		this.broadcastNextExercise(getCurrentExercise());
	}

	@Override
	public void next() {
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alexgilleran.hiitme.programrunner.IProgramTracker#registerObserver
	 * (com
	 * .alexgilleran.hiitme.programrunner.ProgramTracker.ProgramTrackerObserver)
	 */
	@Override
	public void registerObserver(ProgramTrackerObserver observer) {
		observers.add(observer);
	}

	private void broadcastNextExercise(Exercise newExercise) {
		for (ProgramTrackerObserver observer : observers) {
			observer.onNextExercise(newExercise);
		}
	}

	private void broadcastFinish() {
		for (ProgramTrackerObserver observer : observers) {
			observer.onFinish();
		}
	}

	private void broadcastRepFinish(ExerciseGroup superset, int remainingReps) {
		for (ProgramTrackerObserver observer : observers) {
			observer.onRepFinish(superset, remainingReps);
		}
	}
}
