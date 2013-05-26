package com.alexgilleran.hiitme.data.impl;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.alexgilleran.hiitme.data.ProgramDao;
import com.alexgilleran.hiitme.data.tables.ExerciseTable;
import com.alexgilleran.hiitme.data.tables.ProgramNodeTable;
import com.alexgilleran.hiitme.data.tables.ProgramTable;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.model.impl.ProgramImpl;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class HIITMeOpenHelper extends SQLiteOpenHelper implements ProgramDao {
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "HIITMe";
	@Inject
	private Context context;
	private Table exerciseTable = new ExerciseTable();
	private Table programNodeTable = new ProgramNodeTable();
	private Table programTable = new ProgramTable(this);
	private Table[] tables = new Table[] { exerciseTable, programNodeTable,
			programTable };

	public HIITMeOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		for (Table table : tables) {
			db.execSQL(table.getCreateSql());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		for (Table table : tables) {
			db.execSQL(table.getDropSql());
		}

		this.onCreate(db);
	}

	@Override
	public List<Program> getAllPrograms() {
		Cursor cursor = getReadableDatabase().query(programTable.getName(),
				null, null, null, null, null, null);

		if (!cursor.moveToFirst()) {
			return null;
		}

		List<Program> programs = new ArrayList<Program>();
		do {
			programs.add(mapProgram(cursor));
		} while (cursor.moveToNext());

		return programs;
	}

	@Override
	public Program getProgram(long id) {
		return mapProgram(getForId(programTable.getName(), Long.toString(id)));
	}

	@Override
	public ProgramNode getProgramNode(long programNodeId) {
		return null;
	}

	private ProgramNode mapProgramNode(Cursor cursor) {

	}

	@Override
	public ProgramNode getProgramNodeForProgram(long programId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ProgramNode> getChildrenOfNode(long programNodeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Exercise getExerciseForNode(long programNodeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveProgram(Program program) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveProgramNode(ProgramNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveExercise(Exercise exercise) {
		// TODO Auto-generated method stub

	}
}