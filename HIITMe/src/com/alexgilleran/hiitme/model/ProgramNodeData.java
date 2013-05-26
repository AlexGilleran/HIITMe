package com.alexgilleran.hiitme.model;

import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "program_node")
public class ProgramNodeData {
	@DatabaseField(generatedId = true, columnName = "_id")
	private long id;
	@DatabaseField
	private int totalReps;
	@DatabaseField(foreign = true, foreignAutoCreate = true)
	private Exercise attachedExercise;
	@ForeignCollectionField
	private List<ProgramNode> children;

	public int getTotalReps() {
		return totalReps;
	}

	public Exercise getAttachedExercise() {
		return attachedExercise;
	}

	public long getId() {
		return id;
	}

	public void setTotalReps(int totalReps) {
		this.totalReps = totalReps;
	}

	public List<ProgramNode> getChildren() {
		if (children == null) {
			children = new ArrayList<ProgramNode>();
		}
		return children;
	}

	public void setAttachedExercise(Exercise attachedExercise) {
		this.attachedExercise = attachedExercise;
	}

	protected boolean existsInDatabase() {
		return id != 0;
	}
}