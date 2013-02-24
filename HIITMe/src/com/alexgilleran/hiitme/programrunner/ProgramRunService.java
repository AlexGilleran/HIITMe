package com.alexgilleran.hiitme.programrunner;

import roboguice.service.RoboIntentService;
import android.app.Notification;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.data.ProgramDAO;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.Superset;
import com.alexgilleran.hiitme.programrunner.ProgramTracker.ProgramObserver;
import com.google.inject.Inject;

public class ProgramRunService extends RoboIntentService {

	@Inject
	private ProgramDAO programDao;

	private ProgramTracker tracker;

	private ExerciseCountDown currentCountDown;
	boolean isRunning = false;

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
		tracker.registerObserver(programObserver);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new ProgramBinder();
	}

	private void nextCountdown() {
		currentCountDown = new ExerciseCountDown(tracker);
		currentCountDown.start();
	}

	private ProgramObserver programObserver = new ProgramObserver() {
		@Override
		public void onTick(long msecondsRemaining) {
		}

		@Override
		public void onNextExercise(Exercise newExercise) {
			if (!tracker.isFinished()) {
				nextCountdown();
			}
		}

		@Override
		public void onRepFinish(Superset superset, int remainingReps) {
		}

		@Override
		public void onFinish() {
			stopForeground(true);
			isRunning = false;
		}
	};

	public class ProgramBinder extends Binder {
		boolean isPaused = false;

		public void start() {
			isRunning = true;

			if (isPaused) {
				currentCountDown.start();
			} else {
				nextCountdown();
				startForeground(1, notification);
			}
		}

		public void stop() {
			isRunning = false;
			currentCountDown.cancel();
		}

		public void pause() {
			isPaused = true;
			isRunning = false;
			currentCountDown = currentCountDown.pause();
		}

		public Program getProgram() {
			return tracker.getProgram();
		}

		public boolean isRunning() {
			return isRunning;
		}

		public void registerObserver(ProgramObserver observer) {
			tracker.registerObserver(observer);
		}

		public Superset getCurrentSuperset() {
			return tracker.getCurrentSuperset();
		}

		public Exercise getCurrentExercise() {
			return tracker.getCurrentExercise();
		}
	}
}
