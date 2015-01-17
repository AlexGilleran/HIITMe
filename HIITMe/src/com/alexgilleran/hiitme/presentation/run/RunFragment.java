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
import com.todddavies.components.progressbar.ProgressWheel;

public class RunFragment extends Fragment {
	/**
	 * The number of degrees at which the exercise wheel starts - leaving this above 0 gives the illusion that the wheel
	 * is turning from one exercise to the next instead of stopping and starting
	 */
	private static final int EXERCISE_WHEEL_START_DEGREES = 5;

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
		boolean isLarge = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE;
		boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

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

		refreshPauseState();
	}

	@Override
	public void onStop() {
		super.onStop();

		if (programBinder != null && !programBinder.isRunning() && !getActivity().isChangingConfigurations()) {
			getActivity().unbindService(connection);
		}
	}

	public void stop() {
		programBinder.stop();
	}

	private void updateExercise() {
		// View can == null if we're mid way through an orientation switch
		if (getView() != null && programBinder.isActive()) {
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
			exerciseProgressBar.setBarColor(currentExercise.getEffortLevel().getColorId(getView().getContext()));
		}
	}

	public boolean isRunning() {
		return programBinder != null && programBinder.isRunning();
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
		stopButton.setEnabled(enableStopButton);
		stopButton.setImageResource(enableStopButton ? R.drawable.ic_stop : R.drawable.ic_stop_disabled);

		playButton.setImageResource(getPlayButtonResource());

		if (programBinder != null && programBinder.isPaused()) {
			exerciseProgressBar.setProgress(0);
			exerciseProgressBar.setBarLength(getDegrees(programBinder.getExerciseMsRemaining(), programBinder
					.getCurrentExercise().getDuration()));
			exerciseProgressBar.spin();

			programProgressBar.setProgress(0);
			programProgressBar.setBarLength(getDegrees(programBinder.getProgramMsRemaining(), duration));
			programProgressBar.spin();
		} else {
			// Be careful with this, it tends to reset the amount of progress.
			exerciseProgressBar.stopSpinning();
			programProgressBar.stopSpinning();
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

		return minutes + ":" + seconds / 1000 + "." + (seconds % 1000 / 100);
	}

	private int getDegrees(long msecondsRemaining, int duration) {
		return getDegrees(msecondsRemaining, duration, 0);
	}

	private int getDegrees(long msecondsRemaining, int duration, int minDegrees) {
		float degreesFraction = ((float) duration - (float) msecondsRemaining)
				/ ((float) duration / (float) ProgressWheel.getMax());


		// This is a bit of a tweak to make the bar appear at 100% for a tiny
		// bit longer while revolving.
//		if (degreesFraction < minDegrees) {
//			return ProgressWheel.getMax();
//		} else {
		return Math.round(degreesFraction);
//		}
	}

	private void showAlertThenGoBack(String message) {
		new AlertDialog.Builder(getActivity()).setMessage(message)
				.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).setCancelable(false).show();
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

			refreshPauseState();
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

					if (programProgressBar != null) {
						programProgressBar.setProgress(0);
					}

					duration = program.getAssociatedNode().getDuration();
				}
			});

			updateExercise();
			refreshPauseState();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Toast.makeText(getActivity(), "Lost connection to run service", Toast.LENGTH_SHORT).show();
		}
	}

	;

	private final CountDownObserver countDownObserver = new CountDownObserver() {
		@Override
		public void onTick(long exerciseMsRemaining, long programMsRemaining) {
			programProgressBar.setTextLine1(formatTime(exerciseMsRemaining));
			programProgressBar.setTextLine2(formatTime(programMsRemaining));

			exerciseProgressBar.setProgress(getDegrees(exerciseMsRemaining, programBinder.getCurrentExercise()
					.getDuration(), EXERCISE_WHEEL_START_DEGREES));
			programProgressBar.setProgress(getDegrees(programMsRemaining, duration));
		}

		@Override
		public void onExerciseStart() {
			updateExercise();
		}

		@Override
		public void onProgramFinish() {
			hostingActivity.onProgramRunStopped();
			refreshPauseState();
			programProgressBar.setProgress(ProgressWheel.getMax());
			exerciseProgressBar.setProgress(ProgressWheel.getMax());
			programProgressBar.setTextLine1(formatTime(0));
			programProgressBar.setTextLine2(formatTime(0));
			exerciseProgressBar.invalidate();
		}

		@Override
		public void onStart() {
			hostingActivity.onProgramRunStarted();
			refreshPauseState();
			updateExercise();
		}

		@Override
		public void onError(ProgramError error) {
			showAlertThenGoBack("This program runs for 0 seconds! Add some exercises to it first!");
		}
	};

	public interface Callbacks {
		void onProgramRunStarted();

		void onProgramRunStopped();
	}
}