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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.model.ProgramNodeObserver;
import com.alexgilleran.hiitme.programrunner.ExerciseCountDown.CountDownObserver;
import com.alexgilleran.hiitme.programrunner.ProgramBinder;
import com.alexgilleran.hiitme.programrunner.ProgramBinder.ProgramCallback;
import com.alexgilleran.hiitme.programrunner.ProgramRunService;
import com.todddavies.components.progressbar.ProgressWheel;

public class RunFragment extends RoboFragment {

	@InjectView(R.id.textview_run_title)
	private TextView titleText;

	@InjectView(R.id.textview_time_remaining)
	private TextView timeRemainingView;

	@InjectView(R.id.progressbar_exercise)
	private ProgressWheel exerciseProgressBar;

	@InjectView(R.id.progressbar_program)
	private ProgressWheel programProgressBar;

	@InjectView(R.id.rep_button_play_pause)
	private ImageButton playButton;

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
		getActivity().getApplicationContext().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
		getActivity().startService(serviceIntent);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_run, container, false);

		view.findViewById(R.id.rep_button_play_pause).setOnClickListener(new OnClickListener() {
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

	@Override
	public void onStop() {
		super.onStop();
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

	private int getDegrees(long msecondsRemaining, long duration) {
		return ((int) duration - (int) msecondsRemaining) / ((int) duration / ProgressWheel.getMax());
	}

	private final ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			programBinder = (ProgramBinder) service;
			programBinder.getProgram(new ProgramCallback() {
				@Override
				public void onProgramReady(Program program) {
					RunFragment.this.program = program;

					program.getAssociatedNode().registerObserver(observer);
					programBinder.regExerciseCountDownObs(exCountDownObs);
					programBinder.regProgCountDownObs(progCountDownObs);
					exerciseProgressBar.setProgress(0);

					duration = program.getAssociatedNode().getDuration();

					titleText.setText(program.getName());
				}
			});

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {

		}
	};

	private final CountDownObserver exCountDownObs = new CountDownObserver() {
		@Override
		public void onTick(long msecondsRemaining) {
			timeRemainingView.setText(formatTime(msecondsRemaining));
			int currentExerciseDuration = program.getAssociatedNode().getCurrentExercise().getDuration();
			programProgressBar.setProgress(getDegrees(msecondsRemaining, currentExerciseDuration));
		}

		@Override
		public void onFinish() {
			programProgressBar.setProgress(ProgressWheel.getMax());
		}
	};

	private final CountDownObserver progCountDownObs = new CountDownObserver() {
		@Override
		public void onTick(long msecondsRemaining) {
			exerciseProgressBar.setProgress(getDegrees(msecondsRemaining, duration));
		}

		@Override
		public void onFinish() {
			exerciseProgressBar.setProgress(ProgressWheel.getMax());
		}
	};

	private final ProgramNodeObserver observer = new ProgramNodeObserver() {
		@Override
		public void onNextExercise(Exercise newExercise) {
			exerciseProgressBar.setProgress(0);
		}

		@Override
		public void onFinish(ProgramNode node) {
			timeRemainingView.setText(formatTime(0));
			refreshPlayButtonIcon();
		}

		@Override
		public void onRepFinish(ProgramNode superset, int remainingReps) {

		}

		@Override
		public void onReset(ProgramNode node) {

		}

		@Override
		public void onChange(ProgramNode node) {

		}
	};
}
