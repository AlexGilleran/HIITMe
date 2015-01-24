/*
 * HIIT Me, a High Intensity Interval Training app for Android
 * Copyright (C) 2015 Alex Gilleran
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alexgilleran.hiitme.programrunner;

import android.os.CountDownTimer;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Node;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.util.PeekaheadQueue;

public class ProgramRunnerImpl implements ProgramRunner {
	private static final int DEFAULT_TICK_RATE = 25;
	private final int tickRate;

	private CountDownTimer countDown;

	private PeekaheadQueue<Node> nodeQueue;
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

		observer.onExerciseStart();
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
	public Node getCurrentNode() {
		return nodeQueue.peek();
	}

	@Override
	public Exercise getNextExercise() {
		Node next = nodeQueue.peek(1);
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
				nodeQueue.remove();

				// Adding rather than assigning the next exercise duration means
				// that any time leftover from the first exercise is subtracted
				// from the next one.
				exerciseMsRemaining += getCurrentExercise().getDuration();

				observer.onExerciseStart();
			}

			observer.onTick(exerciseMsRemaining, millisUntilFinished);
		}
	}

}
