package com.alexgilleran.hiitme.presentation.programdetail.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Superset;

public class RepGroupView extends LinearLayout {
	private Superset repGroup;

	public RepGroupView(Context context) {
		super(context);
	}

	public RepGroupView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RepGroupView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onFinishInflate() {

	}

	public void setRepGroup(Superset repGroup) {
		this.repGroup = repGroup;

		updateView();
	}

	private void updateView() {
		updateReps();

		TextView repCountView = (TextView) this
				.findViewById(R.id.textview_repcount);
		repCountView.setText("x" + repGroup.getRepCount());
	}

	private void updateReps() {
		TableLayout repLayout = (TableLayout) this
				.findViewById(R.id.layout_reps);

		for (Exercise rep : repGroup.getExercises()) {
			TableRow repRow = new TableRow(this.getContext());

			TextView repLabelView = new TextView(this.getContext());
			repLabelView.setText(rep.getName());
			repRow.addView(repLabelView);

			TextView repDurationView = new TextView(this.getContext());
			repDurationView.setText((rep.getDuration() / 1000) + " seconds");
			repRow.addView(repDurationView);

			repLayout.addView(repRow);
		}
	}
}
