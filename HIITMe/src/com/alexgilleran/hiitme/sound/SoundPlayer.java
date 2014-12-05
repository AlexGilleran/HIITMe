package com.alexgilleran.hiitme.sound;

import com.alexgilleran.hiitme.model.Exercise;

public interface SoundPlayer {
	void playExerciseStart(Exercise exercise);
	void playEnd();
}
