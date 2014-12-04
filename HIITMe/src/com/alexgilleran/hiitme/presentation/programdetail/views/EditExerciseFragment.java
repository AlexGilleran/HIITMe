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

		builder.setTitle("Edit Exercise");
		final EditExerciseView editView = (EditExerciseView) getActivity().getLayoutInflater().inflate(
				R.layout.dialog_edit_exercise, null);
		editView.setExercise(exercise);
		builder.setView(editView);
		builder.setCancelable(true);
		builder.setPositiveButton("OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				editView.update();
				listener.onUpdated();
			}
		});

		return builder.create();
	}
}
