package com.alexgilleran.hiitme.programrunner;

import android.os.CountDownTimer;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.util.PeekaheadQueue;

public class ProgramRunnerImpl implements ProgramRunner {
	private static final int DEFAULT_TICK_RATE = 25;
	private final int tickRate;

	private CountDownTimer countDown;

	private PeekaheadQueue<ProgramNode> nodeQueue;
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
		this.nodeQueue = program.asQueue();
		this.observer = observer;
		this.exerciseMsRemaining = getCurrentExercise().getDuration();
		this.programMsRemaining = program.getAssociatedNode().getDuration();
		this.tickRate = tickRate;

		countDown = new ProgramCountDown(programMsRemaining, tickRate);
	}

	@Override
	public void start() {
		running = true;
		paused = false;

		observer.onStart();

		countDown.start();
	}

	@Override
	public void stop() {
		running = false;
		stopped = true;
		paused = false;

		observer.onExerciseFinish();
		observer.onProgramFinish();

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
	public Exercise getCurrentExercise() {
		return getCurrentNode().getAttachedExercise();
	}

	@Override
	public ProgramNode getCurrentNode() {
		return nodeQueue.peek();
	}

	@Override
	public Exercise getNextExercise() {
		ProgramNode next = nodeQueue.peek(1);
		return next != null ? next.getAttachedExercise() : null;
	}

	@Override
	public void pause() {
		paused = true;
		running = false;

		countDown.cancel();

		countDown = new ProgramCountDown(programMsRemaining, tickRate);
	}

	@Override
	public int getProgramMsRemaining() {
		return programMsRemaining;
	}

	@Override
	public int getExerciseMsRemaining() {
		return exerciseMsRemaining;
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
			// onTick doesn't execute exactly on intervals of the tickRate, it
			// executes roughly close to the tickRate... so the interval within
			// ticks needs to be calculated.
			int msSinceLastTick = (int) (programMsRemaining - millisUntilFinished);
			programMsRemaining = (int) millisUntilFinished;

			// Remove the ms since last tick from the exercise ms remaining.
			exerciseMsRemaining -= msSinceLastTick;

			if (exerciseMsRemaining <= 0) {
				observer.onExerciseFinish();
				nodeQueue.remove();

				// Adding rather than assigning the next exercise duration means
				// that any time leftover from the first exercise is subtracted
				// from the next one.
				exerciseMsRemaining += getCurrentExercise().getDuration();
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
