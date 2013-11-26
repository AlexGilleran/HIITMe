package com.alexgilleran.hiitme.programrunner;

import android.os.CountDownTimer;

public class ExerciseCountDown extends CountDownTimer {
	private static final long TICK_RATE = 100;
	private long millisUntilFinished;

	private CountDownObserver observer;

	public ExerciseCountDown(long duration, CountDownObserver observer) {
		super(duration, TICK_RATE);

		this.observer = observer;
	}
	
	@Override
	public void onFinish() {
		onTick(0);
		observer.onFinish();
	}

	@Override
	public void onTick(long millisUntilFinished) {
		observer.onTick(millisUntilFinished);
		this.millisUntilFinished = millisUntilFinished;
	}

	public ExerciseCountDown pause() {
		this.cancel();

		return new ExerciseCountDown(this.millisUntilFinished, observer);
	}

	public interface CountDownObserver {
		void onTick(long msecondsRemaining);

		void onFinish();
	}
}
