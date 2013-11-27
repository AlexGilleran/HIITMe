package com.alexgilleran.hiitme.programrunner;

public interface ProgramRunner {
	void start();

	void stop();

	void pause();

	boolean isRunning();

	boolean isPaused();

	boolean isStopped();
}