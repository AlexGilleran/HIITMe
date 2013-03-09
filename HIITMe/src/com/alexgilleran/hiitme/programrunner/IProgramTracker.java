package com.alexgilleran.hiitme.programrunner;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.ExerciseGroup;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.programrunner.ProgramTracker.ProgramTrackerObserver;

public interface IProgramTracker {

	public abstract ExerciseGroup getCurrentSuperset();

	public abstract Exercise getCurrentExercise();

	public abstract Program getProgram();

	public abstract boolean isFinished();

	public abstract int getRepCount();

	public abstract void start();

	public abstract void next();

	public abstract void registerObserver(ProgramTrackerObserver observer);

}