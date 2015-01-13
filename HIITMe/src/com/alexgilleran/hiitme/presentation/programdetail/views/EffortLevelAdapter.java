package com.alexgilleran.hiitme.presentation.programdetail.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

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

	private String getEffortLevelString(int position) {
		return ((EffortLevel) getItem(position)).getString(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) parent.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (convertView == null) {
			convertView = inflater.inflate(android.R.layout.simple_spinner_item, null);
		}

		// simple_spinner_item is a textview
		TextView spinnerItem = (TextView) convertView;
		spinnerItem.setText(getEffortLevelString(position));
		return spinnerItem;
	}

	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) parent.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (convertView == null) {
			convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, null);
		}

		// simple_spinner_dropdown_item is a textview
		TextView dropDownItem = (TextView) convertView;
		dropDownItem.setText(getEffortLevelString(position));
		return dropDownItem;
	}
}