package com.alexgilleran.hiitme.presentation.programdetail;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;
import android.app.Notification;
import android.app.PendingIntent;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.programrunner.ProgramRunner;
import com.alexgilleran.hiitme.programrunner.ProgramRunner.ProgramBinder;
import com.alexgilleran.hiitme.programrunner.ProgramRunner.ProgramObserver;

public class ProgramRunFragment extends RoboFragment {
	@InjectView(R.id.textview_time_remaining)
	private TextView timeRemainingView;

	@InjectView(R.id.progressbar_exercise)
	private ProgressBar progressBar;

	private ProgramBinder programBinder;

	private boolean bound;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_program_run, container,
				false);

		view.findViewById(R.id.rep_button_pause).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						programBinder.start();
					}
				});

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		// Bind to LocalService
		Intent intent = new Intent(this.getActivity(), ProgramRunner.class);
		intent.putExtra(Program.PROGRAM_ID_NAME, 1l);



		this.getActivity().bindService(intent, connection,
				Context.BIND_AUTO_CREATE);

	}

	@Override
	public void onStop() {
		super.onStop();

		if (bound) {
			this.getActivity().unbindService(connection);
			bound = false;
		}
	}

	private ProgramObserver observer = new ProgramObserver() {

		@Override
		public void onTick(long msecondsRemaining) {
			timeRemainingView.setText(Double.toString(Math
					.ceil(msecondsRemaining / 100)));
			progressBar
					.setProgress((int) (progressBar.getMax() - msecondsRemaining));
		}

		@Override
		public void onNextExercise(Exercise newExercise) {
			progressBar.setMax(newExercise.getDuration());
		}

		@Override
		public void onFinish() {

		}

	};

	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			programBinder = (ProgramBinder) service;
			programBinder.registerObserver(observer);

			bound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			bound = false;
		}
	};

}
