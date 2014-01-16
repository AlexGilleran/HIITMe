package com.alexgilleran.hiitme.presentation.programdetail;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.data.ProgramDAO;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.presentation.programdetail.views.ProgramDetailView;
import com.alexgilleran.hiitme.presentation.programlist.ProgramListActivity;
import com.alexgilleran.hiitme.util.ViewUtils;
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

	private Program program;

	@Inject
	private ProgramDAO programDao;

	@InjectView(R.id.root)
	private ProgramDetailView detailView;

	@InjectView(R.id.root_node_view_container)
	private FrameLayout container;

	@InjectView(R.id.name_ro)
	private TextView nameReadOnly;

	@InjectView(R.id.description_ro)
	private TextView descriptionReadOnly;

	@InjectView(R.id.name_edit)
	private EditText nameEditable;

	@InjectView(R.id.description_edit)
	private EditText descriptionEditable;

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

		detailView.setProgramNode(program.getAssociatedNode());

		nameReadOnly.setText(program.getName());
		descriptionReadOnly.setText(program.getDescription());

		nameEditable.setText(program.getName());
		descriptionEditable.setText(program.getDescription());
	}

	public boolean isBeingEdited() {
		return detailView.isBeingEdited();
	}

	public void save() {
		program.setName(getName());
		program.setDescription(getDescription());
		program.setAssociatedNode(detailView.getProgramNode());
		programDao.saveProgram(program);
	}

	public void startEditing() {
		detailView.startEditing();

		setTextEditable(true);
	}

	private void setTextEditable(boolean editable) {
		nameReadOnly.setVisibility(ViewUtils.getVisibilityInt(!editable));
		descriptionReadOnly.setVisibility(ViewUtils.getVisibilityInt(!editable));
		nameEditable.setVisibility(ViewUtils.getVisibilityInt(editable));
		descriptionEditable.setVisibility(ViewUtils.getVisibilityInt(editable));
	}

	public void stopEditing() {
		detailView.stopEditing();

		nameReadOnly.setText(nameEditable.getText());
		descriptionReadOnly.setText(descriptionEditable.getText());

		setTextEditable(false);
	}

	public String getName() {
		return nameEditable.getText().toString();
	}

	public String getDescription() {
		return descriptionEditable.getText().toString();
	}
}