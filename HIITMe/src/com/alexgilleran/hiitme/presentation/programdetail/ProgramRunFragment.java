package com.alexgilleran.hiitme.presentation.programdetail;

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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.model.ProgramNodeObserver;
import com.alexgilleran.hiitme.programrunner.ExerciseCountDown.CountDownObserver;
import com.alexgilleran.hiitme.programrunner.ProgramRunService;
import com.alexgilleran.hiitme.programrunner.ProgramRunService.ProgramBinder;

public class ProgramRunFragment extends RoboFragment {
	@InjectView(R.id.textview_time_remaining)
	private TextView timeRemainingView;

	@InjectView(R.id.progressbar_exercise)
	private ProgressBar exerciseProgressBar;
	@InjectView(R.id.progressbar_program)
	private ProgressBar programProgressBar;

	@InjectView(R.id.rep_button_play_pause)
	private ImageButton playButton;

	private ProgramBinder programBinder;

	private int duration;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = new Intent(getActivity(), ProgramRunService.class);
		intent.putExtra(Program.PROGRAM_ID_NAME, 1l);

		getActivity().getApplicationContext().bindService(intent, connection,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_program_run, container,
				false);

		view.findViewById(R.id.rep_button_play_pause).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (programBinder.isRunning()) {
							programBinder.pause();
						} else {
							programBinder.start();
						}

						refreshPlayButtonIcon();
					}
				});

		return view;
	}

	private void refreshPlayButtonIcon() {
		int iconResId = programBinder.isRunning() ? android.R.drawable.ic_media_pause
				: android.R.drawable.ic_media_play;

		playButton.setImageResource(iconResId);
	}

	private String formatTime(long mseconds) {
		int minutes = (int) mseconds / 60000;
		int seconds = (int) mseconds % 60000;

		return minutes + ":" + seconds / 1000 + "." + (seconds % 1000 / 100);
	}

	private int getPercentage(long msecondsRemaining, long duration) {
		return ((int) duration - (int) msecondsRemaining)
				/ ((int) duration / 100);
	}

	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			programBinder = (ProgramBinder) service;
			programBinder.getProgram().registerObserver(observer);
			programBinder.regExerciseCountDownObs(exCountDownObs);
			programBinder.regProgCountDownObs(progCountDownObs);
			exerciseProgressBar.setProgress(0);

			duration = programBinder.getProgram().getTotalDuration();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {

		}
	};

	private CountDownObserver exCountDownObs = new CountDownObserver() {
		@Override
		public void onTick(long msecondsRemaining) {
			timeRemainingView.setText(formatTime(msecondsRemaining));
			int currentExerciseDuration = programBinder.getProgram()
					.getCurrentExercise().getDuration();
			programProgressBar.setProgress(getPercentage(msecondsRemaining,
					currentExerciseDuration));
		}

		@Override
		public void onFinish() {
		}
	};

	private CountDownObserver progCountDownObs = new CountDownObserver() {
		@Override
		public void onTick(long msecondsRemaining) {
			exerciseProgressBar.setProgress(getPercentage(msecondsRemaining,
					duration));
		}

		@Override
		public void onFinish() {
		}
	};

	private ProgramNodeObserver observer = new ProgramNodeObserver() {
		@Override
		public void onNextExercise(Exercise newExercise) {
			exerciseProgressBar.setProgress(0);
		}

		@Override
		public void onFinish(ProgramNode node) {
			timeRemainingView.setText(formatTime(0));
			refreshPlayButtonIcon();
			exerciseProgressBar.setProgress(exerciseProgressBar.getMax());
		}

		@Override
		public void onRepFinish(ProgramNode superset, int remainingReps) {

		}

		@Override
		public void onReset(ProgramNode node) {

		}
	};
}
