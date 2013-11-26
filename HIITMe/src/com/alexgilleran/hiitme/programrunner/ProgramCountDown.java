package com.alexgilleran.hiitme.programrunner;

import android.os.CountDownTimer;

import com.alexgilleran.hiitme.model.Program;

public class ProgramCountDown extends CountDownTimer {
	private static final long TICK_RATE = 100;

	private Program program;
	private CountDownObserver observer;
	private int exerciseMsRemaining;
	private int programMsRemaining;

	public ProgramCountDown(Program program, CountDownObserver observer) {
		this(program, observer, program.getAssociatedNode().getCurrentExercise().getDuration(), program
				.getAssociatedNode().getDuration());
	}

	private ProgramCountDown(Program program, CountDownObserver observer, int exerciseMsRemaining,
			int programMsRemaining) {
		super(programMsRemaining, TICK_RATE);

		this.program = program;
		this.observer = observer;
		this.exerciseMsRemaining = exerciseMsRemaining;
		this.programMsRemaining = programMsRemaining;
	}

	@Override
	public void onFinish() {
		observer.onProgramFinish();
	}

	public ProgramCountDown pause() {
		this.cancel();

		return new ProgramCountDown(program, observer, exerciseMsRemaining, programMsRemaining);
	}

	@Override
	public void onTick(long millisUntilFinished) {
		programMsRemaining = (int) millisUntilFinished;
		exerciseMsRemaining -= TICK_RATE;
		if (exerciseMsRemaining <= 0) {
			observer.onExerciseFinish();
			program.getAssociatedNode().next();
			exerciseMsRemaining = program.getAssociatedNode().getCurrentExercise().getDuration();
		}
		observer.onTick(exerciseMsRemaining, millisUntilFinished);
	}

	public interface CountDownObserver {
		void onTick(long exerciseMsRemaining, long programMsRemaining);

		void onExerciseFinish();

		void onProgramFinish();
	}
}
