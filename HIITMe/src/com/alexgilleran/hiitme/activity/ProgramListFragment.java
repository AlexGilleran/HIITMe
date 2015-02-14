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

package com.alexgilleran.hiitme.activity;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.data.ProgramDAOSqlite;
import com.alexgilleran.hiitme.model.ProgramMetaData;

import java.util.List;

public class ProgramListFragment extends ListFragment {
	private static final String STATE_ACTIVATED_POSITION = "activated_position";
	private Callbacks hostingActivity;
	private int mActivatedPosition = ListView.INVALID_POSITION;
	private ProgramAdapter adapter;

	public interface Callbacks {
		public void onProgramSelected(long id, String name);
	}

	public ProgramListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();

		adapter = new ProgramAdapter(ProgramDAOSqlite.getInstance(getActivity().getApplicationContext())
				.getProgramList());
		setListAdapter(adapter);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			// In lollipop this shows an orange ripple, otherwise use straight orange
			getListView().setSelector(R.color.accent);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated TESTTESTTESTitem position.
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException("Activity must implement fragment's callbacks.");
		}

		hostingActivity = (Callbacks) activity;
	}

	public void refresh() {
		adapter.setProgramList(ProgramDAOSqlite.getInstance(getActivity().getApplicationContext()).getProgramList());
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		hostingActivity = null;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an TESTTESTTESTitem has been selected.
		hostingActivity.onProgramSelected(id, adapter.getItem(position).getName());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated TESTTESTTESTitem position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be given the 'activated' state when
	 * touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		getListView().setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}

	private class ProgramAdapter extends BaseAdapter {
		private final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		private List<ProgramMetaData> programList;

		private ProgramAdapter(List<ProgramMetaData> programList) {
			this.programList = programList;
		}

		protected void setProgramList(List<ProgramMetaData> programList) {
			this.programList = programList;
		}

		@Override
		public int getCount() {
			return programList.size();
		}

		@Override
		public ProgramMetaData getItem(int location) {
			return programList.get(location);
		}

		@Override
		public long getItemId(int location) {
			return programList.get(location).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.program_list_item, parent, false);
			}

			TextView textView = (TextView) convertView.findViewById(R.id.name);
			textView.setText(programList.get(position).getName());

			return convertView;
		}
	}

	;
}
