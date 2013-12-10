package com.alexgilleran.hiitme.presentation.programdetail.views;

import roboguice.fragment.RoboDialogFragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;

public class EditExerciseFragment extends RoboDialogFragment {
	private Exercise exercise;

	public EditExerciseFragment() {

	}

	public void setExercise(Exercise exercise) {
		this.exercise = exercise;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle("Edit Exercise");
		EditExerciseView editView = (EditExerciseView) getActivity()
				.getLayoutInflater().inflate(R.layout.dialog_edit_activity,
						null);
		editView.setExercise(exercise);
		builder.setView(editView);
		builder.setCancelable(true);
		builder.setPositiveButton("OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				exercise.save();
				exercise.getParentNode().save();
			}
		});

		return builder.create();
	}

	public interface EditExerciseListener {
		void onEditExercise(Exercise exerciseToEdit);
	}
}
