package com.alexgilleran.hiitme.programrunner;

import android.os.CountDownTimer;

import com.alexgilleran.hiitme.model.Program;

public class ProgramRunnerImpl implements ProgramRunner {
	private static final int DEFAULT_TICK_RATE = 100;
	private final int tickRate = DEFAULT_TICK_RATE;

	private CountDownTimer countDown;

	private Program program;
	private CountDownObserver observer;
	private int exerciseMsRemaining;
	private int programMsRemaining;
	private boolean running = false;
	private boolean paused = false;
	private boolean stopped = false;

	public ProgramRunnerImpl(Program program, CountDownObserver observer) {
		this(program, observer, DEFAULT_TICK_RATE);
	}

	public ProgramRunnerImpl(Program program, CountDownObserver observer, int tickRate) {
		this.program = program;
		this.observer = observer;
		this.exerciseMsRemaining = program.getAssociatedNode().getCurrentExercise().getDuration();
		this.programMsRemaining = program.getAssociatedNode().getDuration();

		countDown = new ProgramCountDown(programMsRemaining, tickRate);
	}

	@Override
	public void start() {
		running = true;

		observer.onStart();

		countDown.start();
	}

	@Override
	public void stop() {
		running = false;
		stopped = true;

		countDown.cancel();
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public boolean isPaused() {
		return paused;
	}

	@Override
	public boolean isStopped() {
		return stopped;
	}

	@Override
	public void pause() {
		paused = true;
		running = false;

		countDown.cancel();

		countDown = new ProgramCountDown(programMsRemaining, tickRate);
	}

	private class ProgramCountDown extends CountDownTimer {
		public ProgramCountDown(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			running = false;
			stopped = true;
			observer.onProgramFinish();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			programMsRemaining = (int) millisUntilFinished;
			exerciseMsRemaining -= tickRate;
			if (exerciseMsRemaining <= 0) {
				observer.onExerciseFinish();
				program.getAssociatedNode().next();
				exerciseMsRemaining = program.getAssociatedNode().getCurrentExercise().getDuration();
			}
			observer.onTick(exerciseMsRemaining, millisUntilFinished);
		}
	}

	public interface CountDownObserver {
		void onStart();

		void onTick(long exerciseMsRemaining, long programMsRemaining);

		void onExerciseFinish();

		void onProgramFinish();
	}
}
