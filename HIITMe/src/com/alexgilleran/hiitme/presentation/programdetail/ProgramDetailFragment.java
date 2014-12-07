package com.alexgilleran.hiitme.presentation.programdetail;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.activity.MainActivity;
import com.alexgilleran.hiitme.data.ProgramDAOSqlite;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.presentation.programdetail.views.EditExerciseFragment;
import com.alexgilleran.hiitme.presentation.programdetail.views.EditNodeFragment;
import com.alexgilleran.hiitme.presentation.programdetail.views.ExerciseView;
import com.alexgilleran.hiitme.presentation.programdetail.views.NodeView;
import com.alexgilleran.hiitme.presentation.programdetail.views.ProgramDetailView;

/**
 * A fragment representing a single Program detail screen. This fragment is either contained in a {@link MainActivity}
 * in two-pane mode (on tablets) or a {@link ProgramDetailActivity} on handsets.
 */
public class ProgramDetailFragment extends Fragment {
	private Program program;
	private ProgramDetailView detailView;

	/** Mandatory empty constructor */
	public ProgramDetailFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		long programId = getArguments().getLong(MainActivity.ARG_PROGRAM_ID);
		program = ProgramDAOSqlite.getInstance(getActivity()).getProgram(programId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_program_detail, container, false);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		detailView = (ProgramDetailView) getView().findViewById(R.id.layout_root);

		if (savedInstanceState == null) {
			// The program is actually set before the view is rendered in a fragment... as opposed to a view where it'd
			// be the other way around.
			detailView.setExerciseLongClickListener(editExerciseListener);
			detailView.setNodeLongClickListener(editNodeListener);

			if (program != null) {
				detailView.setProgram(program);
			}
		}
	}

	public boolean isBeingEdited() {
		return detailView.isEditable();
	}

	public void save() {
		ProgramDAOSqlite.getInstance(getActivity()).saveProgram(detailView.rebuildProgram());
	}

	public void startEditing() {
		detailView.setEditable(true);
	}

	public void stopEditing() {
		save();
		detailView.setEditable(false);
	}

	private OnLongClickListener editNodeListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View view) {
			final NodeView nodeView = (NodeView) view;

			if (!nodeView.isEditable()) {
				return false;
			}

			EditNodeFragment dialog = new EditNodeFragment();
			dialog.setNode(nodeView.getCurrentNode());

			dialog.setDialogUpdateListener(new EditDialogUpdateListener() {
				@Override
				public void onUpdated() {
					nodeView.updateRepCount();
				}
			});

			dialog.show(getFragmentManager(), "edit_node");
			return true;
		}
	};

	private OnLongClickListener editExerciseListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View view) {
			final ExerciseView exerciseView = (ExerciseView) view;

			if (!exerciseView.isEditable()) {
				return false;
			}

			EditExerciseFragment dialog = new EditExerciseFragment();
			dialog.setExercise(exerciseView.getExercise());

			dialog.setDialogUpdateListener(new EditDialogUpdateListener() {
				@Override
				public void onUpdated() {
					exerciseView.render();
				}
			});

			dialog.show(getFragmentManager(), "edit_exercise");
			return true;
		}
	};
}