package com.alexgilleran.hiitme.programrunner;

import java.util.ArrayList;
import java.util.List;

import roboguice.service.RoboIntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.data.ProgramDao;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.model.ProgramNodeObserver;
import com.alexgilleran.hiitme.programrunner.ExerciseCountDown.CountDownObserver;
import com.google.inject.Inject;

public class ProgramRunService extends RoboIntentService {

	@Inject
	private ProgramDao programDao;
	private Program program;
	private ProgramNode programNode;

	private ExerciseCountDown exerciseCountDown;
	private ExerciseCountDown programCountDown;

	boolean isRunning = false;

	private Notification notification;

	private final List<CountDownObserver> exerciseObservers = new ArrayList<CountDownObserver>();
	private final List<CountDownObserver> programObservers = new ArrayList<CountDownObserver>();

	private WakeLock wakeLock;

	public ProgramRunService() {
		super("HIIT Me");
	}

	@Override
	public void onCreate() {
		super.onCreate();

		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"MyWakeLock");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (programCountDown != null) {
			programCountDown.cancel();
		}

		if (exerciseCountDown != null) {
			exerciseCountDown.cancel();
		}
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
		programNode = program.getAssociatedNode();
		programNode.reset();
		programNode.registerObserver(programObserver);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new ProgramBinder();
	}

	private void next() {
		programNode.next();
		if (!programNode.isFinished()) {
			newCountDown();
		}
	}

	private void newCountDown() {
		exerciseCountDown = new ExerciseCountDown(programNode
				.getCurrentExercise().getDuration(), exerciseCountDownObs);
		exerciseCountDown.start();
	}

	private final ProgramNodeObserver programObserver = new ProgramNodeObserver() {
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

		@Override
		public void onReset(ProgramNode node) {

		}

		@Override
		public void onChange(ProgramNode node) {

		}
	};

	public class ProgramBinder extends Binder {
		boolean isPaused = false;

		public void start() {
			wakeLock.acquire();

			isRunning = true;

			if (isPaused) {
				exerciseCountDown.start();
				programCountDown.start();

			} else {
				startForeground(1, notification);
				programCountDown = new ExerciseCountDown(program
						.getAssociatedNode().getDuration(), programCountDownObs);
				programNode.start();

				newCountDown();
				programCountDown.start();
			}
		}

		public void stop() {
			isRunning = false;
			exerciseCountDown.cancel();
			programCountDown.cancel();

			wakeLock.release();
		}

		public void pause() {
			isPaused = true;
			isRunning = false;
			exerciseCountDown = exerciseCountDown.pause();
			programCountDown = programCountDown.pause();

			wakeLock.release();
		}

		public Program getProgram() {
			return program;
		}

		public boolean isRunning() {
			return isRunning;
		}

		public void regExerciseCountDownObs(CountDownObserver observer) {
			exerciseObservers.add(observer);
		}

		public void regProgCountDownObs(CountDownObserver observer) {
			programObservers.add(observer);
		}

		public ProgramNode getCurrentSuperset() {
			return programNode.getCurrentNode();
		}

		public Exercise getCurrentExercise() {
			return programNode.getCurrentExercise();
		}
	}

	private final CountDownObserver exerciseCountDownObs = new ObserverProxy(
			exerciseObservers) {
		@Override
		public void onFinish() {
			super.onFinish();

			next();
		}
	};

	private final CountDownObserver programCountDownObs = new ObserverProxy(
			programObservers);

	private class ObserverProxy implements CountDownObserver {
		private final List<CountDownObserver> observers;

		public ObserverProxy(List<CountDownObserver> observers) {
			this.observers = observers;
		}

		@Override
		public void onTick(long msecondsRemaining) {
			for (CountDownObserver observer : observers) {
				observer.onTick(msecondsRemaining);
			}
		}

		@Override
		public void onFinish() {
			for (CountDownObserver observer : observers) {
				observer.onFinish();
			}
		}
	}
}
