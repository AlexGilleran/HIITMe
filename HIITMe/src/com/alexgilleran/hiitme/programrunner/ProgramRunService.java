package com.alexgilleran.hiitme.programrunner;

import java.util.ArrayList;
import java.util.List;

import roboguice.service.RoboIntentService;
import android.app.Notification;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.data.ProgramDAO;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;
import com.google.inject.Inject;

public class ProgramRunService extends RoboIntentService {

	@Inject
	private ProgramDAO programDao;

	private ProgramTracker tracker;
	private List<ProgramObserver> observers = new ArrayList<ProgramObserver>();
	private ExerciseCountDown currentCountDown;

	private Notification notification;

	public ProgramRunService() {
		super("HIIT Me");
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onHandleIntent(Intent intent) {
		Notification.Builder builder = new Notification.Builder(
				this.getBaseContext());
		builder.setContentTitle("HIIT Me");
		builder.setSmallIcon(R.drawable.ic_launcher);
		notification = builder.getNotification();

		long programId = intent.getLongExtra(Program.PROGRAM_ID_NAME, -1);
		Program program = programDao.getProgram(programId);
		tracker = new ProgramTracker(program);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new ProgramBinder();
	}

	private void nextCountdown() {
		currentCountDown = new ExerciseCountDown(tracker.getCurrentExercise());
		currentCountDown.start();
	}

	private void tick(long msecondsRemaining) {
		for (ProgramObserver observer : observers) {
			observer.onTick(msecondsRemaining);
		}
	}

	private void nextExercise(Exercise newExercise) {
		for (ProgramObserver observer : observers) {
			observer.onNextExercise(newExercise);
		}
	}

	private void finish() {
		for (ProgramObserver observer : observers) {
			observer.onFinish();
		}
	}

	public class ProgramBinder extends Binder {
		public void start() {
			startForeground(1, notification);
			nextExercise(tracker.getCurrentExercise());
			nextCountdown();
		}

		public void stop() {
			currentCountDown.cancel();
		}

		public void registerObserver(ProgramObserver observer) {
			observers.add(observer);
		}

		public Program getProgram() {
			return tracker.getProgram();
		}
	}

	public interface ProgramObserver {
		void onTick(long msecondsRemaining);

		void onNextExercise(Exercise newExercise);

		void onFinish();
	}

	private class ExerciseCountDown extends CountDownTimer {
		private static final long TICK_RATE = 100;

		public ExerciseCountDown(Exercise rep) {
			super(rep.getDuration(), TICK_RATE);
		}

		@Override
		public void onFinish() {
			nextExercise(tracker.next());

			if (!tracker.isFinished()) {
				nextCountdown();
			} else {
				finish();
			}
		}

		@Override
		public void onTick(long millisUntilFinished) {
			tick(millisUntilFinished);
		}
	}
}
