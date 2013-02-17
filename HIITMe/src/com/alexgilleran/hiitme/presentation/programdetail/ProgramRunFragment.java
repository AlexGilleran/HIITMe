package com.alexgilleran.hiitme.presentation.programdetail;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.programrunner.ProgramRunner;
import com.alexgilleran.hiitme.programrunner.ProgramRunner.ProgramObserver;

public class ProgramRunFragment extends RoboFragment {
	@InjectView(R.id.textview_time_remaining)
	private TextView timeRemainingView;

	private ProgramRunner programRunner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void setProgramRunner(ProgramRunner programRunner) {
		this.programRunner = programRunner;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_program_run, container,
				false);

		programRunner.registerObserver(observer);

		view.findViewById(R.id.rep_button_pause).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						programRunner.start();
					}
				});
		return view;
	}

	private ProgramObserver observer = new ProgramObserver() {

		@Override
		public void onTick(long msecondsRemaining) {
			timeRemainingView.setText(Long.toString(msecondsRemaining));
		}

		@Override
		public void onNextExercise(Exercise newExercise) {

		}

		@Override
		public void onFinish() {

		}

	};
}
