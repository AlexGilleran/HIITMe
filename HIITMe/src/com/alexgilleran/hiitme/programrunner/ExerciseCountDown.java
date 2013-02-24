package com.alexgilleran.hiitme.programrunner;

import android.os.CountDownTimer;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Superset;

public class ExerciseCountDown extends CountDownTimer {
	private static final long TICK_RATE = 100;
	private long millisUntilFinished;
	private ProgramTracker tracker;

	public ExerciseCountDown(ProgramTracker tracker) {
		this(tracker.getCurrentExercise().getDuration());

		this.tracker = tracker;
	}

	private ExerciseCountDown(long duration) {
		super(duration, TICK_RATE);
	}

	@Override
	public void onFinish() {
		int oldRepCount = tracker.getRepCount();
		Superset oldSuperset = tracker.getCurrentSuperset();

		Exercise nextExercise = tracker.next();

		if (nextExercise != null) {
			tracker.broadcastNextExercise(nextExercise);
		}

		if (tracker.getRepCount() > oldRepCount
				|| (!tracker.isFinished() && tracker.getCurrentSuperset() != oldSuperset)) {
			tracker.broadcastRepFinish(tracker.getCurrentSuperset(),
					tracker.getRepCount());
		}

		if (nextExercise == null) {
			tracker.broadcastFinish();
		}
	}

	@Override
	public void onTick(long millisUntilFinished) {
		tracker.broadcastTick(millisUntilFinished);
		this.millisUntilFinished = millisUntilFinished;
	}

	public ExerciseCountDown pause() {
		this.cancel();

		return new ExerciseCountDown(this.millisUntilFinished);
	}
}
