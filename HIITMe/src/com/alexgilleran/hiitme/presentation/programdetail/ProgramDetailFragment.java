package com.alexgilleran.hiitme.presentation.programdetail;

import roboguice.fragment.RoboFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Superset;
import com.alexgilleran.hiitme.presentation.programdetail.views.RepGroupView;
import com.alexgilleran.hiitme.presentation.programlist.ProgramListActivity;
import com.alexgilleran.hiitme.programrunner.ProgramRunner;

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

	private ProgramRunner programRunner;

	/** Mandatory empty constructor */
	public ProgramDetailFragment() {

	}

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
		View rootView = inflater.inflate(R.layout.fragment_program_detail,
				container, false);

		if (programRunner != null) {
			LinearLayout repGroupLayout = (LinearLayout) rootView
					.findViewById(R.id.layout_repgroups);

			for (Superset repGroup : programRunner.getProgram().getSupersets()) {
				RepGroupView repGroupView = (RepGroupView) inflater.inflate(
						R.layout.view_repgroup, null);
				repGroupView.setRepGroup(repGroup);

				repGroupLayout.addView(repGroupView, 0);
			}

		}

		return rootView;
	}
}
