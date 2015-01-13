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
		TextView text = (TextView) convertView.findViewById(R.id.text_effort_level);
		text.setText(effortLevel.getString(context));

		ImageView image = (ImageView) convertView.findViewById(R.id.imageview_effort_level);

		if (effortLevel.isBlank()) {
			image.setVisibility(View.INVISIBLE);
		} else {
			image.setVisibility(View.VISIBLE);
			image.setImageResource(effortLevel.getIconId());
		}

		return convertView;
	}

}