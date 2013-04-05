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
import com.alexgilleran.hiitme.programrunner.ProgramRunService;
import com.alexgilleran.hiitme.programrunner.ProgramRunService.ProgramBinder;
import com.alexgilleran.hiitme.programrunner.ProgramRunService.ProgramRunObserver;

public class ProgramRunFragment extends RoboFragment {
	@InjectView(R.id.textview_time_remaining)
	private TextView timeRemainingView;

	@InjectView(R.id.progressbar_exercise)
	private ProgressBar progressBar;

	@InjectView(R.id.rep_button_play_pause)
	private ImageButton playButton;

	private ProgramBinder programBinder;

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

	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			programBinder = (ProgramBinder) service;
			programBinder.registerObserver(observer);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {

		}
	};

	private ProgramRunObserver observer = new ProgramRunObserver() {
		@Override
		public void onTick(long msecondsRemaining) {
			timeRemainingView.setText(formatTime(msecondsRemaining));
			progressBar
					.setProgress((int) (progressBar.getMax() - msecondsRemaining));
		}

		@Override
		public void onNextExercise(Exercise newExercise) {
			progressBar.setMax(newExercise.getDuration());
		}

		@Override
		public void onFinish(ProgramNode node) {
			timeRemainingView.setText(formatTime(0));
			refreshPlayButtonIcon();
			progressBar.setProgress(progressBar.getMax());
		}

		@Override
		public void onRepFinish(ProgramNode superset, int remainingReps) {

		}
	};
}
