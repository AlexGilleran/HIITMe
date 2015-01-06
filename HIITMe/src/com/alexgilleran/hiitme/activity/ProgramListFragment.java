package com.alexgilleran.hiitme.activity;

import java.util.List;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.alexgilleran.hiitme.data.ProgramDAOSqlite;
import com.alexgilleran.hiitme.model.ProgramMetaData;
import com.alexgilleran.hiitme.presentation.programdetail.ProgramDetailFragment;

/**
 * A list fragment representing a list of Programs. This fragment also supports tablet devices by allowing list items to
 * be given an 'activated' state upon selection. This helps indicate which item is currently being viewed in a
 * {@link ProgramDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks} interface.
 */
public class ProgramListFragment extends ListFragment {

	/**
	 * The serialization (saved instance state) Bundle key representing the activated item position. Only used on
	 * tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item clicks.
	 */
	private Callbacks hostingActivity;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	private ProgramAdapter adapter;

	/**
	 * A callback interface that all activities containing this fragment must implement. This mechanism allows
	 * activities to be notified of item selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 * 
		 * @param name
		 *            TODO
		 */
		public void onProgramSelected(long id, String name);
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation
	 * changes).
	 */
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
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
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
		// fragment is attached to one) that an item has been selected.
		hostingActivity.onProgramSelected(id, adapter.getItem(position).getName());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be given the 'activated' state when
	 * touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
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
				convertView = inflater.inflate(android.R.layout.simple_list_item_activated_1, parent, false);
			}

			TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
			textView.setText(programList.get(position).getName());

			return convertView;
		}
	};
}
