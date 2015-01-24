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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.EffortLevel;

class EffortLevelAdapter extends BaseAdapter {
	private Context context;

	public EffortLevelAdapter(Context context) {
		this.context = context;
	}

	@Override
	public int getCount() {
		return EffortLevel.values().length;
	}

	@Override
	public Object getItem(int position) {
		return EffortLevel.values()[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private EffortLevel getEffortLevel(int position) {
		return ((EffortLevel) getItem(position));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) parent.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.spinner_effort_level, null);
		}

		EffortLevel effortLevel = getEffortLevel(position);

		// simple_spinner_item is a textview
		TextView text = (TextView) convertView.findViewById(R.id.textview_effort_level);
		text.setText(effortLevel.getString(context));

		ImageView image = (ImageView) convertView.findViewById(R.id.imageview_effort_level);

		if (effortLevel.isBlank()) {
			image.setVisibility(View.INVISIBLE);
		} else {
			image.setVisibility(View.VISIBLE);
			image.setImageResource(effortLevel.getColourIconId());
		}

		return convertView;
	}

}