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

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.Node;

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

	void unregisterCountDownObserver(CountDownObserver observer);

	Node getCurrentNode();

	Exercise getCurrentExercise();

	Exercise getNextExercise();

	int getProgramMsRemaining();

	int getExerciseMsRemaining();

	interface ProgramCallback {
		void onProgramReady(Program program);
	}
}