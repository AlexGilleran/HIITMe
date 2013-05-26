package com.alexgilleran.hiitme.data.tables;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import com.alexgilleran.hiitme.data.impl.Column;
import com.alexgilleran.hiitme.data.impl.Column.SQLiteType;
import com.alexgilleran.hiitme.data.impl.Table;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.impl.ProgramImpl;

public class ProgramTable extends Table {
	public static final Column PROGRAM_NAME = new Column("PROGRAM_NAME",
			SQLiteType.TEXT);
	public static final Column PROGRAM_DESCRIPTION = new Column("PROGRAM_NAME",
			SQLiteType.TEXT);

	public ProgramTable(SQLiteOpenHelper helper) {
		super(helper, "PROGRAM", PROGRAM_NAME, PROGRAM_DESCRIPTION);
	}

	public Program getProgram(long id) {

	}

	private Program mapProgram(Cursor cursor) {
		long programId = cursor.getInt(0);
		String name = cursor.getString(1);
		String description = cursor.getString(2);

		return new ProgramImpl(this, programId, name, description);
	}
}