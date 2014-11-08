package com.alexgilleran.hiitme.presentation.programdetail.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TableLayout;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Node;
import com.alexgilleran.hiitme.util.ViewUtils;

public class EditNodeView extends TableLayout {
	private EditText repCount;

	private Node node;

	public EditNodeView(Context context) {
		super(context);
	}

	public EditNodeView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		repCount = (EditText) findViewById(R.id.node_edit_rep_count);
	}

	public Node update() {
		node.setTotalReps(ViewUtils.getIntFromTextViewSafe(repCount));

		return node;
	}

	public void setNode(Node node) {
		this.node = node;

		repCount.setText(Integer.toString(node.getTotalReps()));
	}
}
