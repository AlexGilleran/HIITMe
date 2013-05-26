package com.alexgilleran.hiitme.data.tables;

import com.alexgilleran.hiitme.data.impl.Column;
import com.alexgilleran.hiitme.data.impl.Table;
import com.alexgilleran.hiitme.data.impl.Column.SQLiteType;

public class ExerciseTable extends Table {
	public static final Column TITLE = new Column("TITLE", SQLiteType.TEXT);
	public static final Column DURATION = new Column("DURATION",
			SQLiteType.INTEGER);
	public static final Column EFFORT = new Column("EFFORT", SQLiteType.INTEGER);

	public ExerciseTable() {
		super("PROGRAM", TITLE, DURATION, EFFORT);
	}
}