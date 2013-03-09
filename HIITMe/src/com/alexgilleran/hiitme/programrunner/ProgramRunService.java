package com.alexgilleran.hiitme.programrunner;

import java.util.ArrayList;
import java.util.List;

import roboguice.service.RoboIntentService;
import android.app.Notification;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.data.ProgramDAO;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.ExerciseGroup;
import com.alexgilleran.hiitme.programrunner.ExerciseCountDown.CountDownObserver;
import com.alexgilleran.hiitme.programrunner.ProgramTracker.ProgramTrackerObserver;
import com.google.inject.Inject;

public class ProgramRunService extends RoboIntentService {

	@Inject
	private ProgramDAO programDao;

	private IProgramTracker tracker;

	private ExerciseCountDown currentCountDown;
	boolean isRunning = false;

	private Notification notification;

	private List<ProgramRunObserver> observers = new ArrayList<ProgramRunObserver>();

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

		tracker = new ProgramTracker(program, programObserver);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new ProgramBinder();
	}

	private void nextCountDown() {
		currentCountDown = new ExerciseCountDown(tracker.getCurrentExercise()
				.getDuration(), countDownObserver);
		currentCountDown.start();
	}

	private ProgramTrackerObserver programObserver = new ProgramTrackerObserver() {
		@Override
		public void onNextExercise(Exercise newExercise) {
		}

		@Override
		public void onRepFinish(ExerciseGroup superset, int remainingReps) {
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
				startForeground(1, notification);
				tracker.start();
				nextCountDown();
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

		public void registerObserver(ProgramRunObserver observer) {
			observers.add(observer);
			tracker.registerObserver(observer);
		}

		public ExerciseGroup getCurrentSuperset() {
			return tracker.getCurrentSuperset();
		}

		public Exercise getCurrentExercise() {
			return tracker.getCurrentExercise();
		}
	}

	private CountDownObserver countDownObserver = new CountDownObserver() {
		@Override
		public void onTick(long msecondsRemaining) {
			for (CountDownObserver observer : observers) {
				observer.onTick(msecondsRemaining);
			}
		}

		@Override
		public void onFinish() {
			tracker.next();

			if (!tracker.isFinished()) {
				nextCountDown();
			}
		}
	};

	public interface ProgramRunObserver extends ProgramTrackerObserver,
			CountDownObserver {

	}
}
