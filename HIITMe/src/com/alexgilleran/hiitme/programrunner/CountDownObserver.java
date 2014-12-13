package com.alexgilleran.hiitme.programrunner;

public interface CountDownObserver {
	void onStart();

	void onTick(long exerciseMsRemaining, long programMsRemaining);

	void onExerciseStart();

	void onProgramFinish();

	void onError(CountDownObserver.ProgramError error);

	public enum ProgramError {
		ZERO_DURATION;
	}
}