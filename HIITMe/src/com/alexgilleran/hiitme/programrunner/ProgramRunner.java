package com.alexgilleran.hiitme.programrunner;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Node;

public interface ProgramRunner {
	void start();

	void stop();

	void pause();

	boolean isRunning();

	boolean isPaused();

	boolean isStopped();

	int getProgramMsRemaining();

	int getExerciseMsRemaining();

	Exercise getCurrentExercise();

	Exercise getNextExercise();

	Node getCurrentNode();
}