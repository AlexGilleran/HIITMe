package com.alexgilleran.hiitme.presentation.run;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;
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

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.R.drawable;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.programrunner.ProgramBinder;
import com.alexgilleran.hiitme.programrunner.ProgramBinder.ProgramCallback;
import com.alexgilleran.hiitme.programrunner.ProgramRunService;
import com.alexgilleran.hiitme.programrunner.ProgramRunnerImpl.CountDownObserver;
import com.todddavies.components.progressbar.ProgressWheel;

public class RunFragment extends RoboFragment {
	@InjectView(R.id.progressbar_program)
	private ProgressWheel programProgressBar;

	@InjectView(R.id.progressbar_exercise)
	private ProgressWheel exerciseProgressBar;

	@InjectView(R.id.rep_button_play_pause)
	private ImageButton playButton;

	@InjectView(R.id.rep_button_play_stop)
	private ImageButton stopButton;

	private ProgramBinder programBinder;
	private Program program;

	private int duration;
	private Intent serviceIntent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void setProgramId(long programId) {
		// Bind to LocalService
		serviceIntent = new Intent(getActivity(), ProgramRunService.class);
		serviceIntent.putExtra(Program.PROGRAM_ID_NAME, programId);
		getActivity().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
		getActivity().startService(serviceIntent);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_run, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		playButton.setOnClickListener(playButtonListener);
		stopButton.setOnClickListener(stopButtonListener);

		refreshButtons();
	}

	@Override
	public void onStop() {
		super.onStop();

		if (!programBinder.isActive()) {
			getActivity().stopService(serviceIntent);
			getActivity().unbindService(connection);
		}
	}

	private void refreshButtons() {
		boolean isRunning = programBinder != null ? programBinder.isRunning() : false;
		stopButton.setEnabled(isRunning);
		stopButton.setImageResource(isRunning ? R.drawable.ic_action_stop : R.drawable.ic_action_stop_light);

		playButton.setImageResource(getPlayButtonResource());
	}

	private int getPlayButtonResource() {
		if (programBinder != null) {
			if (programBinder.isRunning()) {
				return R.drawable.ic_action_pause;
			} else if (programBinder.isStopped()) {
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

	private int getDegrees(long msecondsRemaining, long duration) {
		return ((int) duration - (int) msecondsRemaining) / ((int) duration / ProgressWheel.getMax());
	}

	private OnClickListener stopButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			programBinder.stop();
		}
	};

	private OnClickListener playButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (programBinder.isRunning()) {
				programBinder.pause();
			} else {
				programBinder.start();
			}

			refreshButtons();
		}
	};

	private final ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			programBinder = (ProgramBinder) service;
			programBinder.getProgram(new ProgramCallback() {
				@Override
				public void onProgramReady(Program program) {
					RunFragment.this.program = program;

					programBinder.registerCountDownObserver(countDownObserver);
					programProgressBar.setProgress(0);

					duration = program.getAssociatedNode().getDuration();
				}
			});

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {

		}
	};

	private final CountDownObserver countDownObserver = new CountDownObserver() {
		@Override
		public void onTick(long exerciseMsRemaining, long programMsRemaining) {
			exerciseProgressBar.setTextLine1(formatTime(exerciseMsRemaining));
			exerciseProgressBar.setTextLine2(formatTime(programMsRemaining));

			exerciseProgressBar.setProgress(getDegrees(exerciseMsRemaining, program.getAssociatedNode()
					.getCurrentExercise().getDuration()));
			programProgressBar.setProgress(getDegrees(programMsRemaining, duration));
		}

		@Override
		public void onExerciseFinish() {
			exerciseProgressBar.setProgress(0);
			exerciseProgressBar.setTextLine1(formatTime(0));
		}

		@Override
		public void onProgramFinish() {
			programProgressBar.setProgress(ProgressWheel.getMax());
			exerciseProgressBar.setProgress(ProgressWheel.getMax());
			exerciseProgressBar.setTextLine1(formatTime(0));
			exerciseProgressBar.setTextLine2(formatTime(0));
			exerciseProgressBar.invalidate();
			refreshButtons();
		}

		@Override
		public void onStart() {
			refreshButtons();
		}
	};
}
