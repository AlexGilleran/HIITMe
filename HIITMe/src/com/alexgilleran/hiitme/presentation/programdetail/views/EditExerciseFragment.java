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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.presentation.programdetail.EditDialogUpdateListener;

public class EditExerciseFragment extends DialogFragment {
	private Exercise exercise;
	private EditDialogUpdateListener listener;

	public void setExercise(Exercise exercise) {
		this.exercise = exercise;
	}

	public void setDialogUpdateListener(EditDialogUpdateListener listener) {
		this.listener = listener;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(getString(R.string.heading_edit_exercise));
		final EditExerciseView editView = (EditExerciseView) getActivity().getLayoutInflater().inflate(
				R.layout.dialog_edit_exercise, null);
		editView.setExercise(exercise);
		builder.setView(editView);
		builder.setCancelable(true);
		builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				editView.update();
				listener.onUpdated();
			}
		});

		return builder.create();
	}
}
