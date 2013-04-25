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
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.model.ProgramNodeObserver;
import com.alexgilleran.hiitme.programrunner.ExerciseCountDown.CountDownObserver;
import com.google.inject.Inject;

public class ProgramRunService extends RoboIntentService {

	@Inject
	private ProgramDAO programDao;

	private Program program;

	private ExerciseCountDown currentCountDown;
	boolean isRunning = false;

	private Notification notification;

	private List<CountDownObserver> observers = new ArrayList<CountDownObserver>();

	public ProgramRunService() {
		super("HIIT Me");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Notification.Builder builder = new Notification.Builder(
				this.getBaseContext());
		builder.setContentTitle("HIIT Me");
		builder.setSmallIcon(R.drawable.ic_launcher);
		notification = builder.getNotification();

		long programId = intent.getLongExtra(Program.PROGRAM_ID_NAME, -1);
		program = programDao.getProgram(programId);
		program.registerObserver(programObserver);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new ProgramBinder();
	}

	private void next() {
		program.next();
		if (!program.isFinished()) {
			newCountDown();
		}
	}
	
	private void newCountDown() {
		currentCountDown = new ExerciseCountDown(program
				.getCurrentExercise().getDuration(), countDownObserver);
		currentCountDown.start();
	}

	private ProgramNodeObserver programObserver = new ProgramNodeObserver() {
		@Override
		public void onNextExercise(Exercise newExercise) {
		}

		@Override
		public void onRepFinish(ProgramNode superset, int remainingReps) {
		}

		@Override
		public void onFinish(ProgramNode node) {
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
				program.start();
				newCountDown();
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
			return program;
		}

		public boolean isRunning() {
			return isRunning;
		}

		public void registerObserver(CountDownObserver observer) {
			observers.add(observer);
		}

		public ProgramNode getCurrentSuperset() {
			return program.getCurrentNode();
		}

		public Exercise getCurrentExercise() {
			return program.getCurrentExercise();
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
			next();
		}
	};
}
