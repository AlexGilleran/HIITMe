package com.alexgilleran.hiitme.programrunner;

import java.util.ArrayList;
import java.util.List;

import android.os.CountDownTimer;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;

public class ProgramRunner {
	private ProgramTracker tracker;
	private List<ProgramObserver> observers = new ArrayList<ProgramObserver>();
	private ExerciseCountDown currentCountDown;

	public ProgramRunner(Program program) {
		this.tracker = new ProgramTracker(program);
	}

	public void start() {
		nextCountdown();
	}

	public void stop() {
		currentCountDown.cancel();
	}

	public void registerObserver(ProgramObserver observer) {
		observers.add(observer);
	}

	private void nextCountdown() {
		currentCountDown = new ExerciseCountDown(tracker.getCurrentExercise());
		currentCountDown.start();
	}

	private void tick(long msecondsRemaining) {
		for (ProgramObserver observer : observers) {
			observer.onTick(msecondsRemaining);
		}
	}

	private void nextExercise(Exercise newExercise) {
		for (ProgramObserver observer : observers) {
			observer.onNextExercise(newExercise);
		}
	}

	private void finish() {
		for (ProgramObserver observer : observers) {
			observer.onFinish();
		}
	}

	public Program getProgram() {
		return tracker.getProgram();
	}

	public interface ProgramObserver {
		void onTick(long msecondsRemaining);

		void onNextExercise(Exercise newExercise);

		void onFinish();
	}

	private class ExerciseCountDown extends CountDownTimer {
		private static final int TICK_RATE = 10;

		public ExerciseCountDown(Exercise rep) {
			super(rep.getDuration(), TICK_RATE);
		}

		@Override
		public void onFinish() {
			nextExercise(tracker.next());

			if (!tracker.isFinished()) {
				nextCountdown();
			} else {
				finish();
			}
		}

		@Override
		public void onTick(long millisUntilFinished) {
			tick(millisUntilFinished);
		}
	}
}
