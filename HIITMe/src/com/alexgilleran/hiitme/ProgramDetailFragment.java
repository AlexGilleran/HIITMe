package com.alexgilleran.hiitme;

import roboguice.fragment.RoboFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alexgilleran.hiitme.data.ProgramDAO;
import com.alexgilleran.hiitme.model.Program;
import com.google.inject.Inject;

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

	/** The dummy content this fragment is presenting. */
	private Program mItem;

	@Inject
	private ProgramDAO programDao;

	/** Mandatory empty constructor */
	public ProgramDetailFragment() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			mItem = programDao.getProgram(getArguments().getLong(ARG_ITEM_ID));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_program_detail,
				container, false);

		// Show the dummy content as text in a TextView.
		if (mItem != null) {
			((TextView) rootView.findViewById(R.id.program_detail))
					.setText(mItem.getName());
		}

		return rootView;
	}
}
