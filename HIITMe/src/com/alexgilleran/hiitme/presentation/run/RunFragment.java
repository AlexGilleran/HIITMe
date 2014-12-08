package com.alexgilleran.hiitme.presentation.run;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
import com.alexgilleran.hiitme.programrunner.ProgramBinder;
import com.alexgilleran.hiitme.programrunner.ProgramBinder.ProgramCallback;
import com.alexgilleran.hiitme.programrunner.ProgramRunService;
import com.alexgilleran.hiitme.programrunner.ProgramRunnerImpl.CountDownObserver;
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

	private Callbacks hostingActivity;
	private ProgramBinder programBinder;

	private int duration;
	private Intent serviceIntent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Bind to LocalService
		serviceIntent = new Intent(getActivity(), ProgramRunService.class);
		serviceIntent.putExtra(Program.PROGRAM_ID_NAME, getArguments().getLong(MainActivity.ARG_PROGRAM_ID));
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
		return inflater.inflate(R.layout.fragment_run, container, false);
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

		playButton.setOnClickListener(playButtonListener);
		stopButton.setOnClickListener(stopButtonListener);

		refreshPauseState();
	}

	@Override
	public void onStop() {
		super.onStop();

		if (programBinder != null && !getActivity().isChangingConfigurations()) {
			getActivity().unbindService(connection);
		}
	}

	public void stop() {
		programBinder.stop();
	}

	private void updateExercise() {
		if (getView() != null) { // Can happen if we're mid way through an orientation switch
			Exercise currentExercise = programBinder.getCurrentExercise();
			exerciseName.setText(currentExercise.getName());
			effortLevelIcon.setImageResource(currentExercise.getEffortLevel().getIconId());
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
		stopButton.setImageResource(enableStopButton ? R.drawable.ic_action_stop : R.drawable.ic_action_stop_light);

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
				return R.drawable.ic_action_pause;
			} else if (isStopped()) {
				return R.drawable.ic_action_repeat;
			}
		}

		return R.drawable.ic_action_play_dark;
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
		if (degreesFraction < minDegrees) {
			return ProgressWheel.getMax();
		} else {
			return Math.round(degreesFraction);
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

			refreshPauseState();
		}
	};

	private final ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			programBinder = (ProgramBinder) service;
			programBinder.getProgram(new ProgramCallback() {
				@Override
				public void onProgramReady(Program program) {
					programBinder.registerCountDownObserver(countDownObserver);
					programProgressBar.setProgress(0);

					duration = program.getAssociatedNode().getDuration();
				}
			});

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Toast.makeText(getActivity(), "Lost connection to run service", Toast.LENGTH_SHORT).show();
		}
	};

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
	};

	public interface Callbacks {
		void onProgramRunStarted();

		void onProgramRunStopped();
	}
}
