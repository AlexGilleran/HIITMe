package com.alexgilleran.hiitme.data.tables;

import com.alexgilleran.hiitme.data.impl.Column;
import com.alexgilleran.hiitme.data.impl.Table;
import com.alexgilleran.hiitme.data.impl.Column.SQLiteType;

public class ProgramNodeTable extends Table {
	public static final Column TOTAL_REPS = new Column("TOTAL_REPS",
			SQLiteType.INTEGER);
	public static final Column PARENT_ID = new Column("PARENT_ID",
			SQLiteType.INTEGER);
	public static final Column EXERCISE_ID = new Column("EXERCISE_ID",
			SQLiteType.INTEGER);

	public ProgramNodeTable() {
		super("PROGRAM", TOTAL_REPS, PARENT_ID, EXERCISE_ID);
	}
}