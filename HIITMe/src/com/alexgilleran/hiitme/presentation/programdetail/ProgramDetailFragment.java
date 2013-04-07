package com.alexgilleran.hiitme.presentation.programdetail;

import java.util.HashMap;
import java.util.Map;

import roboguice.fragment.RoboFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.presentation.programdetail.views.ProgramNodeView;
import com.alexgilleran.hiitme.presentation.programlist.ProgramListActivity;
import com.alexgilleran.hiitme.programrunner.ProgramRunService;
import com.alexgilleran.hiitme.programrunner.ProgramRunService.ProgramBinder;
import com.alexgilleran.hiitme.programrunner.ProgramRunService.ProgramRunObserver;

/**
 * A fragment representing a single Program detail screen. This fragment is
 * either contained in a {@link ProgramListActivity} in two-pane mode (on
 * tablets) or a {@link ProgramDetailActivity} on handsets.
 */
public class ProgramDetailFragment extends RoboFragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	private ProgramBinder programBinder;

	private LayoutInflater inflater;

	private Map<ProgramNode, ProgramNodeView> supersetViews = new HashMap<ProgramNode, ProgramNodeView>();

	/** Mandatory empty constructor */
	public ProgramDetailFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = new Intent(getActivity(), ProgramRunService.class);
		getActivity().getApplicationContext().bindService(intent, connection,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_program_detail,
				container, false);

		this.inflater = inflater;

		return rootView;
	}

	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			programBinder = (ProgramBinder) service;
			programBinder.registerObserver(observer);

			if (programBinder.getProgram() != null) {
				LinearLayout repGroupLayout = (LinearLayout) getView()
						.findViewById(R.id.layout_repgroups);

				for (ProgramNode superset : programBinder.getProgram()
						.getChildren()) {
					ProgramNodeView supersetView = (ProgramNodeView) inflater
							.inflate(R.layout.view_repgroup, null);
					supersetView.setProgramNode(superset);

					repGroupLayout.addView(supersetView, 0);
					supersetViews.put(superset, supersetView);
				}
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {

		}
	};

	private ProgramRunObserver observer = new ProgramRunObserver() {
		@Override
		public void onNextExercise(Exercise newExercise) {
//			supersetViews.get(newExercise.getParentNode()).setCurrentExercise(
//					newExercise);
		}

		@Override
		public void onFinish(ProgramNode node) {

		}

		@Override
		public void onRepFinish(ProgramNode superset, int remainingReps) {
			supersetViews.get(superset).setRemainingReps(remainingReps);
		}

		@Override
		public void onTick(long msecondsRemaining) {
		}
	};
}
