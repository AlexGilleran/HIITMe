package com.alexgilleran.hiitme.programrunner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import roboguice.service.RoboIntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.data.ProgramDAO;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.programrunner.ProgramBinder.ProgramCallback;
import com.alexgilleran.hiitme.programrunner.ProgramRunnerImpl.CountDownObserver;
import com.google.inject.Inject;

public class ProgramRunService extends RoboIntentService {

	@Inject
	private ProgramDAO programDao;

	private Program program;

	private ProgramRunner programRunner;

	private Notification notification;

	private final List<CountDownObserver> observers = new ArrayList<CountDownObserver>();

	private WakeLock wakeLock;

	private Queue<ProgramCallback> programCallbacks = new LinkedList<ProgramCallback>();

	public ProgramRunService() {
		super("HIIT Me");
	}

	@Override
	public void onCreate() {
		super.onCreate();

		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		stopRun();
	}

	private void stopRun() {
		if (programRunner != null && !programRunner.isStopped()) {
			programRunner.stop();
		}

		stopForeground(true);

		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Notification.Builder builder = new Notification.Builder(this.getBaseContext());
		builder.setContentTitle("HIIT Me");
		builder.setSmallIcon(R.drawable.ic_launcher);
		notification = builder.getNotification();
		long programId = intent.getLongExtra(Program.PROGRAM_ID_NAME, -1);

		program = programDao.getProgram(programId);

		while (!programCallbacks.isEmpty()) {
			programCallbacks.poll().onProgramReady(program);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new ProgramBinderImpl();
	}

	public class ProgramBinderImpl extends Binder implements ProgramBinder {
		@Override
		public void start() {
			wakeLock.acquire();

			if (programRunner == null || programRunner.isStopped()) {
				startForeground(1, notification);

				program.getAssociatedNode().reset();
				programRunner = new ProgramRunnerImpl(program, observerProxy);

				programRunner.start();
			} else if (programRunner.isPaused()) {
				programRunner.start();
			} else if (programRunner.isRunning()) {
				Log.wtf(getPackageName(),
						"Trying to start a run when one is already running, this is STRICTLY VERBOTEN");
			}
		}

		@Override
		public void stop() {
			stopRun();
		}

		@Override
		public void pause() {
			programRunner.pause();

			wakeLock.release();
		}

		@Override
		public void getProgram(ProgramCallback callback) {
			if (program != null) {
				callback.onProgramReady(program);
			} else {
				programCallbacks.add(callback);
			}
		}

		@Override
		public boolean isRunning() {
			return programRunner != null ? programRunner.isRunning() : false;
		}

		@Override
		public void registerCountDownObserver(CountDownObserver observer) {
			observers.add(observer);
		}

		@Override
		public ProgramNode getCurrentNode() {
			return program.getAssociatedNode().getCurrentNode();
		}

		@Override
		public Exercise getCurrentExercise() {
			return program.getAssociatedNode().getCurrentExercise();
		}

		@Override
		public boolean isActive() {
			return programRunner != null && (programRunner.isRunning() || programRunner.isPaused());
		}

		@Override
		public boolean isStopped() {
			return programRunner.isStopped();
		}

		@Override
		public boolean isPaused() {
			return programRunner.isPaused();
		}

		@Override
		public int getProgramMsRemaining() {
			return programRunner.getProgramMsRemaining();
		}

		@Override
		public int getExerciseMsRemaining() {
			return programRunner.getExerciseMsRemaining();
		}

		@Override
		public Exercise getNextExercise() {
			return null;
		}
	}

	private final CountDownObserver observerProxy = new MasterCountDownObserver(observers);

	/**
	 * Listens for count down events and proxies them to a number of other
	 * {@link CountDownObserver}s.
	 */
	private class MasterCountDownObserver implements CountDownObserver {
		private final List<CountDownObserver> observers;

		public MasterCountDownObserver(List<CountDownObserver> observers) {
			this.observers = observers;
		}

		@Override
		public void onTick(long exerciseMsRemaining, long programMsRemaining) {
			for (CountDownObserver observer : observers) {
				observer.onTick(exerciseMsRemaining, programMsRemaining);
			}
		}

		@Override
		public void onExerciseFinish() {
			for (CountDownObserver observer : observers) {
				observer.onExerciseFinish();
			}
		}

		@Override
		public void onProgramFinish() {
			for (CountDownObserver observer : observers) {
				observer.onProgramFinish();
			}
			stopRun();
		}

		@Override
		public void onStart() {
			for (CountDownObserver observer : observers) {
				observer.onStart();
			}
		}
	}
}
