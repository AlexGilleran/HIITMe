package com.alexgilleran.hiitme.presentation.programdetail.views;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableRow;

import com.alexgilleran.hiitme.R;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.model.ProgramNodeObserver;

public class ExerciseView extends TableRow implements ProgramNodeObserver {

	private Spinner effortLevel;
	private EditText duration;

	private Exercise exercise;
	private ProgramNodeView nodeView;

	public ExerciseView(Context context) {
		super(context);
	}

	public ExerciseView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		effortLevel = (Spinner) findViewById(R.id.exercise_effort_level);
		duration = (EditText) findViewById(R.id.exercise_duration);

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				getContext(), android.R.layout.simple_spinner_item,
				new String[] { "Hard", "Easy", "Rest" });
		arrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		effortLevel.setAdapter(arrayAdapter);
		effortLevel.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View view) {
				ClipData data = ClipData.newPlainText("", "");
				DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
						ExerciseView.this);
				ExerciseView.this.startDrag(data, shadowBuilder, ExerciseView.this, 0);
				// view.setVisibility(View.INVISIBLE);
				return true;
			}
		});
	}
		

	public ProgramNodeView getNodeView() {
		return nodeView;
	}

	public void setNodeView(ProgramNodeView nodeView) {
		this.nodeView = nodeView;
	}

	public void setExercise(Exercise exercise) {
		this.exercise = exercise;
		exercise.getParentNode().registerObserver(this);

		onReset(exercise.getParentNode());
	}

	@Override
	public void onNextExercise(Exercise newExercise) {
		this.setBackgroundColor(Color.GREEN);
	}

	@Override
	public void onRepFinish(ProgramNode node, int completedReps) {

	}

	@Override
	public void onFinish(ProgramNode node) {
		this.setBackgroundColor(Color.TRANSPARENT);
	}

	@Override
	public void onReset(ProgramNode node) {

		duration.setText(Integer.toString(exercise.getDuration() / 1000));
	}

	@Override
	public void onChange(ProgramNode node) {
		onReset(node);
	}
}