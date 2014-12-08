package com.alexgilleran.hiitme.programrunner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.data.ProgramDAOSqlite;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Node;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.programrunner.ProgramBinder.ProgramCallback;
import com.alexgilleran.hiitme.programrunner.ProgramRunnerImpl.CountDownObserver;
import com.alexgilleran.hiitme.sound.SoundPlayer;
import com.alexgilleran.hiitme.sound.TextToSpeechPlayer;

public class ProgramRunService extends IntentService {
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
		stopRun();

		super.onDestroy();
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
		notification = builder.build();
		long programId = intent.getLongExtra(Program.PROGRAM_ID_NAME, -1);

		program = ProgramDAOSqlite.getInstance(getApplicationContext()).getProgram(programId);

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

				programRunner = new ProgramRunnerImpl(program, new MasterCountDownObserver(observers,
						new TextToSpeechPlayer(getApplicationContext(), (AudioManager) getApplicationContext()
								.getSystemService(Context.AUDIO_SERVICE))));

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
		public Node getCurrentNode() {
			return programRunner.getCurrentNode();
		}

		@Override
		public Exercise getCurrentExercise() {
			return programRunner.getCurrentExercise();
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
			return programRunner.getNextExercise();
		}
	}

	/**
	 * Listens for count down events and proxies them to a number of other {@link CountDownObserver}s.
	 */
	private class MasterCountDownObserver implements CountDownObserver {
		private final List<CountDownObserver> observers;
		private final SoundPlayer soundPlayer;

		public MasterCountDownObserver(List<CountDownObserver> observers, SoundPlayer soundPlayer) {
			this.observers = observers;
			this.soundPlayer = soundPlayer;
		}

		@Override
		public void onTick(long exerciseMsRemaining, long programMsRemaining) {
			for (CountDownObserver observer : observers) {
				observer.onTick(exerciseMsRemaining, programMsRemaining);
			}
		}

		@Override
		public void onExerciseStart() {
			soundPlayer.playExerciseStart(programRunner.getCurrentExercise());

			for (CountDownObserver observer : observers) {
				observer.onExerciseStart();
			}
		}

		@Override
		public void onProgramFinish() {
			soundPlayer.playEnd();

			for (CountDownObserver observer : observers) {
				observer.onProgramFinish();
			}

			stopRun();
		}

		@Override
		public void onStart() {
			soundPlayer.playExerciseStart(programRunner.getCurrentExercise());

			for (CountDownObserver observer : observers) {
				observer.onStart();
			}
		}
	}
}
