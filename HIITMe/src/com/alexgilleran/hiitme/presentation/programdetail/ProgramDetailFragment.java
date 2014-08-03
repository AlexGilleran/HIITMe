package com.alexgilleran.hiitme.presentation.programdetail;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.data.ProgramDAO;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Node;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.presentation.programdetail.views.ExerciseView;
import com.alexgilleran.hiitme.presentation.programdetail.views.NodeView;
import com.alexgilleran.hiitme.presentation.programdetail.views.ProgramDetailView;
import com.alexgilleran.hiitme.presentation.programlist.ProgramListActivity;
import com.alexgilleran.hiitme.util.ViewUtils;
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

	@InjectView(R.id.view_program_detail)
	private ProgramDetailView detailView;

	@InjectView(R.id.name_ro)
	private TextView nameReadOnly;

	@InjectView(R.id.name_edit)
	private EditText nameEditable;

	@InjectView(R.id.button_add_exercise)
	private ImageButton addExerciseButton;
	@InjectView(R.id.button_add_node)
	private ImageButton addNodeButton;

	private LayoutInflater layoutInflater;

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
		this.layoutInflater = inflater;

		View rootView = inflater.inflate(R.layout.fragment_program_detail, container, false);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		addExerciseButton.setOnTouchListener(addExerciseListener);
		addNodeButton.setOnTouchListener(addNodeListener);

		detailView.setProgramNode(program.getAssociatedNode());

		nameReadOnly.setText(program.getName());
		nameEditable.setText(program.getName());
	}

	public boolean isBeingEdited() {
		return detailView.isBeingEdited();
	}

	public void save() {
		program.setName(getName());
		program.setAssociatedNode(detailView.getProgramNode());
		programDao.saveProgram(program);
	}

	public void startEditing() {
		detailView.startEditing();

		setTextEditable(true);
	}

	private void setTextEditable(boolean editable) {
		nameReadOnly.setVisibility(ViewUtils.getVisibilityInt(!editable));
		nameEditable.setVisibility(ViewUtils.getVisibilityInt(editable));
		addExerciseButton.setVisibility(ViewUtils.getVisibilityInt(editable));
		addNodeButton.setVisibility(ViewUtils.getVisibilityInt(editable));
		((RelativeLayout.LayoutParams) detailView.getLayoutParams()).addRule(RelativeLayout.BELOW,
				editable ? R.id.name_edit : R.id.name_ro);
	}

	public void stopEditing() {
		detailView.stopEditing();

		nameReadOnly.setText(nameEditable.getText());

		setTextEditable(false);
	}

	public String getName() {
		return nameEditable.getText().toString();
	}

	private OnTouchListener addExerciseListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, final MotionEvent event) {
			if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
				// FIXME: This is a copy of a lot of the stuff that happens in NodeView...
				Exercise exercise = new Exercise();
				exercise.setNode(program.getAssociatedNode());
				final ExerciseView view = (ExerciseView) layoutInflater.inflate(R.layout.view_exercise,
						detailView.getNodeView(), false);
				view.setExercise(exercise);
				view.setNodeView(detailView.getNodeView());
				view.setEditable(true);
				view.setDragManager(detailView);
				detailView.getNodeView().addChild(view, 1);

				detailView.post(new Runnable() {
					@Override
					public void run() {
						detailView.startDrag(view, (int) event.getRawY(), addExerciseButton.getTop());
					}
				});
			}
			return false;
		}
	};

	private OnTouchListener addNodeListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, final MotionEvent event) {
			if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
				// FIXME: This is a copy of a lot of the stuff that happens in NodeView...
				Node node = new Node();
				node.setParent(node);
				final NodeView view = (NodeView) layoutInflater.inflate(R.layout.view_node, detailView.getNodeView(),
						false);
				view.init(node);
				view.setEditable(true);
				view.setDragManager(detailView);
				detailView.getNodeView().addChild(view, 1);

				detailView.post(new Runnable() {
					@Override
					public void run() {
						detailView.startDrag(view, (int) event.getRawY(), addNodeButton.getTop());
					}
				});
			}
			return false;
		}
	};
}