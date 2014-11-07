package com.alexgilleran.hiitme.presentation.programdetail.views;

import roboguice.fragment.RoboDialogFragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Node;
import com.alexgilleran.hiitme.presentation.programdetail.EditDialogUpdateListener;

public class EditNodeFragment extends RoboDialogFragment {
	private Node node;
	private EditDialogUpdateListener listener;

	public void setNode(Node node) {
		this.node = node;
	}

	public void setDialogUpdateListener(EditDialogUpdateListener listener) {
		this.listener = listener;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle("Edit Node");
		final EditNodeView editView = (EditNodeView) getActivity().getLayoutInflater().inflate(
				R.layout.dialog_edit_node, null);
		editView.setNode(node);
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
