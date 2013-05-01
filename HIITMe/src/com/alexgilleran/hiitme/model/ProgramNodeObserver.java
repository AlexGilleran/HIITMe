package com.alexgilleran.hiitme.model;

public interface ProgramNodeObserver {
	void onNextExercise(Exercise newExercise);

	void onRepFinish(ProgramNode node, int completedReps);

	void onFinish(ProgramNode node);
	
	void onReset(ProgramNode node);
}
