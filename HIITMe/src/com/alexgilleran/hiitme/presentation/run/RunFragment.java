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

package com.alexgilleran.hiitme.presentation.run;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.activity.MainActivity;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.ProgramMetaData;
import com.alexgilleran.hiitme.programrunner.CountDownObserver;
import com.alexgilleran.hiitme.programrunner.ProgramBinder;
import com.alexgilleran.hiitme.programrunner.ProgramBinder.ProgramCallback;
import com.alexgilleran.hiitme.programrunner.ProgramRunService;
import com.alexgilleran.hiitme.util.ViewUtils;
import com.todddavies.components.progressbar.ProgressWheel;

public class RunFragment extends Fragment {
	/**
	 * The number of degrees at which the exercise wheel starts - leaving this above 0 gives the illusion that the wheel
	 * is turning from one exercise to the next instead of stopping and starting
	 */
	private static final int EXERCISE_WHEEL_START_DEGREES = 5;
	private static final String FINISHED_KEY = "FINISHED";
	private static final String EXERCISE_WHEEL_COLOR_KEY = "EXERCISE_WHEEL_COLOR";

	private ProgressWheel programProgressBar;
	private ProgressWheel exerciseProgressBar;
	private ImageButton playButton;
	private ImageButton stopButton;
	private TextView exerciseName;
	private ImageView effortLevelIcon;
	private TextView effortLevelText;

	private Callbacks hostingActivity;
	private RunnerServiceConnection connection;
	private ProgramBinder programBinder;

	private int duration;
	private Intent serviceIntent;
	private boolean isFinished = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		connection = new RunnerServiceConnection();

		// Bind to LocalService
		serviceIntent = new Intent(getActivity(), ProgramRunService.class);
		serviceIntent.putExtra(ProgramMetaData.PROGRAM_ID_NAME, getArguments().getLong(MainActivity.ARG_PROGRAM_ID));
		getActivity().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
		getActivity().startService(serviceIntent);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		hostingActivity = (Callbacks) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		int layout = shouldShowLandscapeLayout() ? R.layout.fragment_run_land : R.layout.fragment_run_port;

		return inflater.inflate(layout, container, false);
	}

	private boolean shouldShowLandscapeLayout() {
		boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		boolean isLarge = ViewUtils.isLarge(getResources());

		// Should be landscape in a portrait tablet layout, or in a landscape phone layout.
		return (isLandscape && !isLarge) || (!isLandscape && isLarge);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		programProgressBar = (ProgressWheel) getView().findViewById(R.id.progressbar_program);
		exerciseProgressBar = (ProgressWheel) getView().findViewById(R.id.progressbar_exercise);
		playButton = (ImageButton) getView().findViewById(R.id.rep_button_play_pause);
		stopButton = (ImageButton) getView().findViewById(R.id.rep_button_play_stop);
		exerciseName = (TextView) getView().findViewById(R.id.textview_exercise_name);
		effortLevelIcon = (ImageView) getView().findViewById(R.id.imageview_effort_level);
		effortLevelText = (TextView) getView().findViewById(R.id.textview_effort_level);

		playButton.setOnClickListener(playButtonListener);
		stopButton.setOnClickListener(stopButtonListener);

		if (savedInstanceState != null && savedInstanceState.getBoolean(FINISHED_KEY)) {
			isFinished = true;

			exerciseProgressBar.setBarColor(savedInstanceState.getInt(EXERCISE_WHEEL_COLOR_KEY, R.color.primary));

			onRunFinish();
		} else {
			refreshPauseState();
		}
	}

	@Override
	public void onStop() {
		super.onStop();

		if (programBinder != null) {
			programBinder.unregisterCountDownObserver(countDownObserver);

			if (!programBinder.isRunning() && !getActivity().isChangingConfigurations()) {
				getActivity().unbindService(connection);
				getActivity().stopService(serviceIntent);
			}
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();

		hostingActivity = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (isFinished) {
			outState.putBoolean(FINISHED_KEY, isFinished);

			if (exerciseProgressBar != null) {
				outState.putInt(EXERCISE_WHEEL_COLOR_KEY, exerciseProgressBar.getBarColor());
			}
		}
	}

	private void onRunFinish() {
		if (hostingActivity != null) {
			hostingActivity.onProgramRunStopped();
		}

		refreshPauseState();

		if (programProgressBar != null) {
			programProgressBar.setProgress(ProgressWheel.getMax());
			programProgressBar.setTextLine1(formatTime(0));
			programProgressBar.setTextLine2(formatTime(0));
			programProgressBar.invalidate();
		}

		if (exerciseProgressBar != null) {
			exerciseProgressBar.invalidate();
			exerciseProgressBar.setProgress(ProgressWheel.getMax());
		}

		if (exerciseName != null) {
			exerciseName.setVisibility(View.INVISIBLE);
		}

		if (effortLevelText != null) {
			effortLevelText.setVisibility(View.INVISIBLE);
		}

		if (effortLevelIcon != null) {
			effortLevelIcon.setVisibility(View.INVISIBLE);
		}
	}

	public void stop() {
		programBinder.stop();
	}

	private void updateExercise() {
		// View can == null if we're mid way through an orientation switch
		if (getView() != null && programBinder != null && programBinder.isActive()) {
			Exercise currentExercise = programBinder.getCurrentExercise();
			exerciseName.setText(currentExercise.getName());

			if (currentExercise.getEffortLevel().isBlank()) {
				effortLevelIcon.setVisibility(View.INVISIBLE);
				effortLevelText.setVisibility(View.INVISIBLE);
			} else {
				effortLevelIcon.setVisibility(View.VISIBLE);
				effortLevelText.setVisibility(View.VISIBLE);

				effortLevelIcon.setImageResource(currentExercise.getEffortLevel().getColourIconId());
				effortLevelText.setText(currentExercise.getEffortLevel().getString(getActivity()));
				effortLevelText.setTextColor(currentExercise.getEffortLevel().getColorId(getActivity()));
			}

			exerciseName.setVisibility(currentExercise.getName() == null ? View.INVISIBLE : View.VISIBLE);

			exerciseProgressBar.setBarColor(currentExercise.getEffortLevel().getColorId(getView().getContext()));
		}
	}

	public boolean isRunning() {
		return programBinder != null && programBinder.isRunning();
	}


	public boolean isPaused() {
		return programBinder != null && programBinder.isPaused();
	}

	private boolean isStopped() {
		return programBinder != null && programBinder.isStopped();
	}

	public void preventRun() {
		if (isRunning()) {
			stop();
		}

		playButton.setEnabled(false);
	}

	public void allowRun() {
		playButton.setEnabled(true);
	}

	private void refreshPauseState() {
		boolean enableStopButton = programBinder != null && programBinder.isActive();

		if (stopButton != null) {
			stopButton.setEnabled(enableStopButton);
			stopButton.setImageResource(enableStopButton ? R.drawable.ic_stop : R.drawable.ic_stop_disabled);
		}

		if (playButton != null) {
			playButton.setImageResource(getPlayButtonResource());
		}

		if (programBinder != null && programBinder.isPaused()) {
			int progress = isFinished ? exerciseProgressBar.getMax() : 0;

			exerciseProgressBar.setProgress(0);
			exerciseProgressBar.setBarLength(getDegrees(programBinder.getExerciseMsRemaining(), programBinder
					.getCurrentExercise().getDuration()));
			exerciseProgressBar.spin();

			programProgressBar.setProgress(0);
			programProgressBar.setBarLength(getDegrees(programBinder.getProgramMsRemaining(), duration));
			programProgressBar.spin();
		} else {
			if (exerciseProgressBar != null && exerciseProgressBar.isSpinning()) {
				// Be careful with this, it tends to reset the amount of progress.
				exerciseProgressBar.stopSpinning();
			}

			if (programProgressBar != null && programProgressBar.isSpinning()) {
				programProgressBar.stopSpinning();
			}
		}
	}

	private int getPlayButtonResource() {
		if (programBinder != null) {
			if (isRunning()) {
				return R.drawable.ic_pause;
			} else if (isStopped()) {
				return R.drawable.ic_repeat;
			}
		}

		return R.drawable.ic_play_dark;
	}

	private String formatTime(long mseconds) {
		int minutes = (int) mseconds / 60000;
		int seconds = (int) mseconds % 60000;
		int dSeconds = (int) seconds % 1000 / 100;

		return minutes + ":" + ViewUtils.timeUnitToString(seconds / 1000) + "." + ViewUtils.timeUnitToString(dSeconds);
	}

	private int getDegrees(long msecondsRemaining, int duration) {
		return getDegrees(msecondsRemaining, duration, 0);
	}

	private int getDegrees(long msecondsRemaining, int duration, int minDegrees) {
		float degreesFraction = ((float) duration - (float) msecondsRemaining)
				/ ((float) duration / (float) ProgressWheel.getMax());

		return Math.round(degreesFraction);
	}

	private void showAlertThenGoBack(String message) {
		new AlertDialog.Builder(getActivity()).setMessage(message)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).setCancelable(false).show();
	}

	private void updateProgress(long exerciseMsRemaining, long programMsRemaining) {
		if (programProgressBar != null) {
			programProgressBar.setTextLine1(formatTime(exerciseMsRemaining));
			programProgressBar.setTextLine2(formatTime(programMsRemaining));
			programProgressBar.setProgress(getDegrees(programMsRemaining, duration));
		}

		if (exerciseProgressBar != null) {
			exerciseProgressBar.setProgress(getDegrees(exerciseMsRemaining, programBinder.getCurrentExercise()
					.getDuration(), EXERCISE_WHEEL_START_DEGREES));
		}
	}

	private OnClickListener stopButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			stop();
		}
	};

	private OnClickListener playButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!playButton.isEnabled()) {
				return;
			}

			if (programBinder.isRunning()) {
				programBinder.pause();
			} else {
				programBinder.start();
			}
		}
	};

	private class RunnerServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			programBinder = (ProgramBinder) service;

			programBinder.getProgram(new ProgramCallback() {
				@Override
				public void onProgramReady(Program program) {
					programBinder.registerCountDownObserver(countDownObserver);

					if (program.getId() != getArguments().getLong(MainActivity.ARG_PROGRAM_ID)) {

					}

					duration = program.getAssociatedNode().getDuration();
				}
			});

			if (programBinder.isActive()) {
				hostingActivity.onProgramRunStarted();
				updateProgress(programBinder.getExerciseMsRemaining(), programBinder.getProgramMsRemaining());
			} else {
				hostingActivity.onProgramRunStopped();
			}
			updateExercise();

			if (stopButton != null) {
				refreshPauseState();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Toast.makeText(getActivity(), R.string.error_no_connection, Toast.LENGTH_SHORT).show();
		}
	}

	private final CountDownObserver countDownObserver = new CountDownObserver() {
		@Override
		public void onTick(long exerciseMsRemaining, long programMsRemaining) {
			updateProgress(exerciseMsRemaining, programMsRemaining);
		}

		@Override
		public void onExerciseStart() {
			updateExercise();
		}

		@Override
		public void onProgramFinish() {
			isFinished = true;
			onRunFinish();
		}

		@Override
		public void onPause() {
			refreshPauseState();
		}

		@Override
		public void onStart() {
			isFinished = false;

			if (hostingActivity != null) {
				hostingActivity.onProgramRunStarted();
			}

			refreshPauseState();
			updateExercise();
		}

		@Override
		public void onError(ProgramError error) {
			showAlertThenGoBack(getString(R.string.error_program_zero_duration));
		}
	};

	public interface Callbacks {
		void onProgramRunStarted();

		void onProgramRunStopped();
	}
}