package com.alexgilleran.hiitme.presentation.programdetail;

import roboguice.fragment.RoboFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.data.ProgramDAO;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.RepGroup;
import com.alexgilleran.hiitme.presentation.programlist.ProgramListActivity;
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
	private Program program;

	@Inject
	private ProgramDAO programDao;

	/** Mandatory empty constructor */
	public ProgramDetailFragment() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			program = programDao
					.getProgram(getArguments().getLong(ARG_ITEM_ID));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_program_detail,
				container, false);

		if (program != null) {
			LinearLayout repGroupLayout = (LinearLayout) rootView
					.findViewById(R.id.layout_repgroups);

			for (RepGroup repGroup : program.getRepGroups()) {
				RepGroupView repGroupView = (RepGroupView) inflater.inflate(
						R.layout.view_repgroup, null);
				repGroupView.setRepGroup(repGroup);

				repGroupLayout.addView(repGroupView, 0);
			}

			repGroupLayout.findViewById(R.id.button_start_program)
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							FragmentTransaction transaction = ProgramDetailFragment.this
									.getFragmentManager().beginTransaction();

							ProgramRunFragment runFragment = new ProgramRunFragment();

							// transaction.hide(ProgramDetailFragment.this);
							transaction.add(R.id.program_detail_container,
									runFragment);
//							transaction.show(runFragment);

							transaction.commit();
						}
					});
		}

		return rootView;
	}
}
