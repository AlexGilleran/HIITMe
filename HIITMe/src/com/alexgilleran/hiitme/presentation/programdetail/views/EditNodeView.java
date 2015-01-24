/*
 * HIIT Me, a High Intensity Interval Training app for Android
 * Copyright (C) 2015 Alex Gilleran
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alexgilleran.hiitme.presentation.programdetail.views;

import android.content.Context;
import android.text.InputFilter;
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

		repCount.setFilters(new InputFilter[]{new InputFilterMinMax("1", "100")});
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
