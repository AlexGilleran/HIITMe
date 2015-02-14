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

package com.alexgilleran.hiitme.presentation.programdetail;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.activity.MainActivity;
import com.alexgilleran.hiitme.data.ProgramDAOSqlite;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.presentation.programdetail.views.ProgramDetailView;


public class ProgramDetailFragment extends Fragment {
	private final static String IS_EDITING_KEY = "IS_EDITING";

	private Program program;
	private ProgramDetailView detailView;


	/**
	 * Mandatory empty constructor
	 */
	public ProgramDetailFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		refreshProgram(false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_program_detail, container, false);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		detailView = (ProgramDetailView) getView().findViewById(R.id.layout_root);
		detailView.setFragmentManager(getFragmentManager());
		if (program != null) {
			detailView.setProgram(program);
		}

		if (savedInstanceState != null && savedInstanceState.getBoolean(IS_EDITING_KEY)) {
			detailView.setEditable(true);
		}

		View editButton = getActivity().findViewById(R.id.actionbar_icon_edit);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (detailView != null) { // THIS GETS RUN EVEN WHEN THIS FRAGMENT ISN'T ON SCREEN WTF ANDROID?!
			outState.putBoolean(IS_EDITING_KEY, detailView.isEditable());
		}

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStop() {
		save();

		super.onStop();
	}

	private void refreshProgram(boolean skipCache) {
		long programId = getArguments().getLong(MainActivity.ARG_PROGRAM_ID);
		program = ProgramDAOSqlite.getInstance(getActivity()).getProgram(programId, skipCache);
	}


	public boolean isBeingEdited() {
		return detailView.isEditable();
	}

	public void save() {
		ProgramDAOSqlite.getInstance(getActivity()).saveProgram(detailView.rebuildProgram());
	}

	public void startEditing() {
		detailView.setEditable(true);

		ProgramDetailTour tour = new ProgramDetailTour((MainActivity) getActivity(), R.id.button_edit, detailView.getRootNodeView(), detailView.getFirstExerciseView());
		tour.init();
	}

	public void stopEditing(boolean save) {
		if (save) {
			save();
		} else {
			refreshProgram(true);
			detailView.setProgram(program);
		}

		detailView.setEditable(false);
	}
}