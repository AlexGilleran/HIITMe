/*
 * HIIT Me, a High Intensity Interval Training app for Android
 * Copyright (C) 2015 Alex Gilleran
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alexgilleran.hiitme.programrunner;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.activity.MainActivity;
import com.alexgilleran.hiitme.data.ProgramDAOSqlite;
import com.alexgilleran.hiitme.model.EffortLevel;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Node;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.ProgramMetaData;
import com.alexgilleran.hiitme.programrunner.CountDownObserver.ProgramError;
import com.alexgilleran.hiitme.sound.SoundPlayer;
import com.alexgilleran.hiitme.sound.TextToSpeechPlayer;
import com.alexgilleran.hiitme.util.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class ProgramRunService extends Service {
	/**
	 * We will only update the notification once for every TICK_RATE_DIVISOR ticks - should result
	 * in being notified once per second.
	 */
	private static final int TICK_RATE_DIVISOR = 1000 / ProgramRunner.TICK_RATE;
	private static final int NOTIFICATION_ID = 2;
	private static final IntentFilter noisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

	private final MasterCountDownObserver masterObserver = new MasterCountDownObserver();

	private String notificationTitle;
	private NotificationManager notificationManager;
	private WakeLock wakeLock;
	private Program program;
	private ProgramRunner programRunner;
	private SoundPlayer soundPlayer;
	private int duration;

	private final List<CountDownObserver> observers = new ArrayList<CountDownObserver>();

	@Override
	public void onCreate() {
		super.onCreate();

		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");

		AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		soundPlayer = new TextToSpeechPlayer(getApplicationContext(), audioManager);

		observers.add(soundObserver);
		observers.add(notificationObserver);

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	public void onDestroy() {
		stop();
		soundPlayer.cleanUp();

		super.onDestroy();
	}

	private void pause() {
		unregisterReceiver(headphonesUnpluggedReceiver);
		programRunner.pause();
	}

	private void start() {
		registerReceiver(headphonesUnpluggedReceiver, noisyIntentFilter);

		if (duration <= 0) {
			masterObserver.onError(ProgramError.ZERO_DURATION);
			stop();
			return;
		}

		if (programRunner == null || programRunner.isStopped()) {
			wakeLock.acquire();

			startForeground(NOTIFICATION_ID, buildNotification());

			programRunner = new ProgramRunnerImpl(program, masterObserver);

			programRunner.start();
		} else if (programRunner.isPaused()) {
			programRunner.start();
		} else if (programRunner.isRunning()) {
			Log.wtf(getPackageName(),
					"Trying to start a run when one is already running, this is STRICTLY VERBOTEN");
		}
	}

	private void stop() {
		try {
			unregisterReceiver(headphonesUnpluggedReceiver);
		} catch (IllegalArgumentException e) {
			// Already unregistered, oh well.
		}

		if (programRunner != null && !programRunner.isStopped()) {
			programRunner.stop();
			programRunner = null;
		}

		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
		}

		stopForeground(true);

		stopSelf();
	}

	private Notification buildNotification() {
		Intent continueIntent = new Intent(getApplicationContext(), MainActivity.class);
		continueIntent.setAction(MainActivity.ACTION_CONTINUE_RUN);
		continueIntent.putExtra(MainActivity.ARG_PROGRAM_ID, program.getId());
		continueIntent.putExtra(MainActivity.ARG_PROGRAM_NAME, program.getName());

		Notification.Builder builder = new Notification.Builder(getBaseContext());
		builder.setContentTitle(notificationTitle);

		if (programRunner != null) {
			builder.setContentText(getNotificationText());
			builder.setProgress(duration, duration - programRunner.getProgramMsRemaining(), false);
		}

		builder.setSmallIcon(R.drawable.notification_icon);
		builder.setOngoing(true);
		builder.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, continueIntent, 0));

		return builder.build();
	}

	private String getNotificationText() {
		StringBuilder builder = new StringBuilder();

		if (programRunner.getCurrentExercise().getName() != null) {
			builder.append(programRunner.getCurrentExercise().getName()).append(" ");
		}

		if (programRunner.getCurrentExercise().getEffortLevel() != EffortLevel.NONE) {
			builder.append(programRunner.getCurrentExercise().getEffortLevel().getString(getApplicationContext())).append(" ");
		}

		builder.append(ViewUtils.getTimeText(programRunner.getExerciseMsRemaining()));

		return builder.toString();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		long programId = intent.getLongExtra(ProgramMetaData.PROGRAM_ID_NAME, -1);

		program = ProgramDAOSqlite.getInstance(getApplicationContext()).getProgram(programId, false);
		duration = program.getAssociatedNode().getDuration();
		notificationTitle = getString(R.string.app_name) + ": " + program.getName();

		start();

		return START_REDELIVER_INTENT;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new ProgramBinderImpl();
	}

	private void updateNotification() {
		notificationManager.notify(NOTIFICATION_ID, buildNotification());
	}

	private BroadcastReceiver headphonesUnpluggedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
				ProgramRunService.this.pause();
			}
		}
	};

	public class ProgramBinderImpl extends Binder implements ProgramBinder {
		@Override
		public void start() {
			ProgramRunService.this.start();
		}

		@Override
		public void stop() {
			ProgramRunService.this.stop();
		}

		@Override
		public void pause() {
			ProgramRunService.this.pause();
		}

		@Override
		public boolean isRunning() {
			return programRunner != null ? programRunner.isRunning() : false;
		}

		@Override
		public void registerCountDownObserver(CountDownObserver observer) {
			// This is a hack but fragments won't always unregister themselves so this is what happens.
			if (observer.isExclusive()) {
				List<CountDownObserver> toRemove = new ArrayList<>();

				for (CountDownObserver otherObserver : observers) {
					if (otherObserver.isExclusive()) {
						toRemove.add(otherObserver);
					}
				}

				for (CountDownObserver obsToRemove : toRemove) {
					observers.remove(obsToRemove);
				}
			}

			observers.add(observer);
		}

		@Override
		public void unregisterCountDownObserver(CountDownObserver observer) {
			observers.remove(observer);
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
			return programRunner != null && programRunner.isStopped();
		}

		@Override
		public boolean isPaused() {
			return programRunner != null && programRunner.isPaused();
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

	private CountDownObserver notificationObserver = new CountDownObserver() {
		private int tickCount = 0;

		@Override
		public void onStart() {
		}

		@Override
		public void onTick(long exerciseMsRemaining, long programMsRemaining) {
			tickCount++;

			// Only update the notification every second.
			if (tickCount >= TICK_RATE_DIVISOR) {
				tickCount = 0;
				updateNotification();
			}
		}

		@Override
		public void onExerciseStart() {
			updateNotification();
		}

		@Override
		public void onProgramFinish() {
		}

		@Override
		public void onPause() {

		}

		@Override
		public void onError(ProgramError error) {
		}

		@Override
		public boolean isExclusive() {
			return false;
		}
	};

	private CountDownObserver soundObserver = new CountDownObserver() {
		@Override
		public void onStart() {
			soundPlayer.playExerciseStart(programRunner.getCurrentExercise());
		}

		@Override
		public void onTick(long exerciseMsRemaining, long programMsRemaining) {
		}

		@Override
		public void onExerciseStart() {
			soundPlayer.playExerciseStart(programRunner.getCurrentExercise());
		}

		@Override
		public void onProgramFinish() {
			soundPlayer.playEnd();
		}

		@Override
		public void onPause() {

		}

		@Override
		public void onError(ProgramError error) {
		}

		@Override
		public boolean isExclusive() {
			return false;
		}
	};

	/**
	 * Listens for count down events and proxies them to a number of other {@link CountDownObserver}s.
	 */
	private class MasterCountDownObserver implements CountDownObserver {
		@Override
		public void onTick(long exerciseMsRemaining, long programMsRemaining) {
			for (CountDownObserver observer : observers) {
				observer.onTick(exerciseMsRemaining, programMsRemaining);
			}
		}

		@Override
		public void onExerciseStart() {
			for (CountDownObserver observer : observers) {
				observer.onExerciseStart();
			}
		}

		@Override
		public void onProgramFinish() {
			for (CountDownObserver observer : observers) {
				observer.onProgramFinish();
			}

			stop();
		}

		@Override
		public void onPause() {
			for (CountDownObserver observer : observers) {
				observer.onPause();
			}
		}

		@Override
		public void onStart() {
			for (CountDownObserver observer : observers) {
				observer.onStart();
			}
		}

		@Override
		public void onError(ProgramError error) {
			for (CountDownObserver observer : observers) {
				observer.onError(error);
			}
		}

		@Override
		public boolean isExclusive() {
			return false;
		}
	}
}
