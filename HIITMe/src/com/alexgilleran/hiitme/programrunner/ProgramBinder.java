package com.alexgilleran.hiitme.programrunner;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.programrunner.ProgramRunnerImpl.CountDownObserver;

public interface ProgramBinder {

	void start();

	void stop();

	void pause();

	void getProgram(ProgramCallback callback);

	boolean isRunning();

	boolean isActive();

	boolean isStopped();

	boolean isPaused();

	void registerCountDownObserver(CountDownObserver observer);

	ProgramNode getCurrentNode();

	Exercise getCurrentExercise();

	int getProgramMsRemaining();

	int getExerciseMsRemaining();

	interface ProgramCallback {
		void onProgramReady(Program program);
	}
}