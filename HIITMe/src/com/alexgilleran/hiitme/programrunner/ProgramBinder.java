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

	void registerCountDownObserver(CountDownObserver observer);

	ProgramNode getCurrentNode();

	Exercise getCurrentExercise();

	interface ProgramCallback {
		void onProgramReady(Program program);
	}
}