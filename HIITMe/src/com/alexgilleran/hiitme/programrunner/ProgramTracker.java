package com.alexgilleran.hiitme.programrunner;

import java.util.ArrayList;
import java.util.List;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.Superset;

public class ProgramTracker {
	private static final int INITIAL_REP_COUNT = 1;
	private static final int FINISHED_FLAG = -1;
	private Program program;
	private int currentExerciseIndex;
	private int currentSupersetIndex;
	private int currentRepCount = INITIAL_REP_COUNT;
	private List<ProgramObserver> observers = new ArrayList<ProgramObserver>();

	public ProgramTracker(Program program) {
		this.program = program;
	}

	public Superset getCurrentSuperset() {
		if (currentSupersetIndex == FINISHED_FLAG) {
			return null;
		}

		return program.getSupersets().get(currentSupersetIndex);
	}

	public Exercise getCurrentExercise() {
		if (currentExerciseIndex == FINISHED_FLAG) {
			return null;
		}

		return this.getCurrentSuperset().getExercises()
				.get(currentExerciseIndex);
	}

	public Program getProgram() {
		return program;
	}

	public boolean isFinished() {
		return (currentExerciseIndex == FINISHED_FLAG
				|| currentSupersetIndex == FINISHED_FLAG || currentRepCount == FINISHED_FLAG);
	}

	public int getRepCount() {
		if (this.getCurrentSuperset() != null) {
			return this.currentRepCount;
		} else {
			return -1;
		}
	}

	public Exercise next() {
		if (currentExerciseIndex < getCurrentSuperset().getExercises().size() - 1) {
			// There's another exercise in this superset, move up to that
			currentExerciseIndex++;
		} else if (currentRepCount < getCurrentSuperset().getRepCount()) {
			// No more exercises this rep, go to the next one and start
			// exercises again.
			currentRepCount++;
			currentExerciseIndex = 0;
		} else if (currentSupersetIndex < program.getSupersets().size() - 1) {
			// No more reps remaining, go to next superset
			currentRepCount = INITIAL_REP_COUNT;
			currentExerciseIndex = 0;
			currentSupersetIndex++;
		} else {
			// No more exercises, reps or supersets - we are done!

			currentRepCount = FINISHED_FLAG;
			currentExerciseIndex = FINISHED_FLAG;
			currentSupersetIndex = FINISHED_FLAG;
		}

		return this.getCurrentExercise();
	}

	public void registerObserver(ProgramObserver observer) {
		observers.add(observer);
	}

	protected void broadcastTick(long msecondsRemaining) {
		for (ProgramObserver observer : observers) {
			observer.onTick(msecondsRemaining);
		}
	}

	protected void broadcastNextExercise(Exercise newExercise) {
		for (ProgramObserver observer : observers) {
			observer.onNextExercise(newExercise);
		}
	}

	protected void broadcastFinish() {
		for (ProgramObserver observer : observers) {
			observer.onFinish();
		}
	}

	protected void broadcastRepFinish(Superset superset, int remainingReps) {
		for (ProgramObserver observer : observers) {
			observer.onRepFinish(superset, remainingReps);
		}
	}

	public interface ProgramObserver {
		void onTick(long msecondsRemaining);

		void onNextExercise(Exercise newExercise);

		void onRepFinish(Superset superset, int remainingReps);

		void onFinish();
	}
}
