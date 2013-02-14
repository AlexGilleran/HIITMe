package com.alexgilleran.hiitme.presentation.programdetail;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Rep;
import com.alexgilleran.hiitme.model.RepGroup;

public class RepGroupView extends LinearLayout {
	private RepGroup repGroup;

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

	public void setRepGroup(RepGroup repGroup) {
		this.repGroup = repGroup;

		updateView();
	}

	private void updateView() {
		updateReps();

		TextView repCountView = (TextView) this
				.findViewById(R.id.textview_repcount);
		repCountView.setText("x" + repGroup.getReps().size());
	}

	private void updateReps() {
		for (Rep rep : repGroup.getReps()) {
			TextView repLabel = new TextView(this.getContext());
			repLabel.setText(rep.getName());
			this.addView(repLabel);
		}
	}
}
