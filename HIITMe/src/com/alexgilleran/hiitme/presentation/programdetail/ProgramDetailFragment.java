package com.alexgilleran.hiitme.presentation.programdetail;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.data.ProgramDAO;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.presentation.programdetail.views.EditExerciseFragment;
import com.alexgilleran.hiitme.presentation.programdetail.views.EditNodeFragment;
import com.alexgilleran.hiitme.presentation.programdetail.views.ExerciseView;
import com.alexgilleran.hiitme.presentation.programdetail.views.NodeView;
import com.alexgilleran.hiitme.presentation.programdetail.views.ProgramDetailView;
import com.alexgilleran.hiitme.presentation.programlist.ProgramListActivity;
import com.google.inject.Inject;

/**
 * A fragment representing a single Program detail screen. This fragment is either contained in a
 * {@link ProgramListActivity} in two-pane mode (on tablets) or a {@link ProgramDetailActivity} on handsets.
 */
public class ProgramDetailFragment extends RoboFragment {
	/**
	 * The fragment argument representing the item ID that this fragment represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	private Program program;

	@Inject
	private ProgramDAO programDao;

	@InjectView(R.id.layout_root)
	private RelativeLayout rootLayout;

	@InjectView(R.id.layout_root)
	private ProgramDetailView detailView;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_program_detail, container, false);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// The program is actually set before the view is rendered in a fragment... as opposed to a view where it'd be
		// the other way around.
		detailView.setExerciseLongClickListener(editExerciseListener);
		detailView.setNodeLongClickListener(editNodeListener);
		detailView.setProgram(program);
	}

	public boolean isBeingEdited() {
		return detailView.isEditable();
	}

	public void save() {
		programDao.saveProgram(detailView.getProgram());
	}

	public void startEditing() {
		detailView.setEditable(true);
	}

	public void stopEditing() {
		detailView.setEditable(false);
	}

	private OnLongClickListener editNodeListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View view) {
			final NodeView nodeView = (NodeView) view;
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