package com.alexgilleran.hiitme.presentation.programdetail;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.presentation.programdetail.views.EditExerciseFragment;
import com.alexgilleran.hiitme.presentation.programdetail.views.EditExerciseFragment.EditExerciseListener;
import com.alexgilleran.hiitme.presentation.programdetail.views.ProgramNodeView;
import com.alexgilleran.hiitme.presentation.programlist.ProgramListActivity;

/**
 * A fragment representing a single Program detail screen. This fragment is
 * either contained in a {@link ProgramListActivity} in two-pane mode (on
 * tablets) or a {@link ProgramDetailActivity} on handsets.
 */
public class ProgramDetailFragment extends RoboFragment implements
		EditExerciseListener {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	private LayoutInflater inflater;

	private Program program;

	@InjectView(R.id.layout_repgroups)
	private LinearLayout repGroupLayout;

	/** Mandatory empty constructor */
	public ProgramDetailFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void setProgram(Program program) {
		this.program = program;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;

		View rootView = inflater.inflate(R.layout.fragment_program_detail,
				container, false);

		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ProgramNodeView nodeView = (ProgramNodeView) inflater.inflate(
				R.layout.view_program_node, null);
		nodeView.setEditExerciseListener(ProgramDetailFragment.this);
		nodeView.setProgramNode(program.getAssociatedNode());

		repGroupLayout.addView(nodeView, 0);
	}

	@Override
	public void onEditExercise(Exercise exerciseToEdit) {
		EditExerciseFragment editExercise = new EditExerciseFragment();
		editExercise.setExercise(exerciseToEdit);
		editExercise.show(getFragmentManager(), "editexercise");
	}
}
