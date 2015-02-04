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

package com.alexgilleran.hiitme.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.LongSparseArray;

import com.alexgilleran.hiitme.model.DatabaseModel;
import com.alexgilleran.hiitme.model.EffortLevel;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Node;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.ProgramMetaData;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class ProgramDAOSqlite extends SQLiteOpenHelper implements ProgramDAO {
	private static ProgramDAOSqlite INSTANCE;

	private ExerciseTable exerciseTable = new ExerciseTable();
	private NodeTable nodeTable = new NodeTable();
	private ProgramTable programTable = new ProgramTable();
	private LongSparseArray<Program> cache = new LongSparseArray<Program>();

	public static ProgramDAO getInstance(Context context) {
		if (INSTANCE == null) {
			INSTANCE = new ProgramDAOSqlite(context);
		}

		return INSTANCE;
	}

	private ProgramDAOSqlite(Context context) {
		super(context, "hiitme.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		programTable.addForeignKey(ProgramTable.Columns.ASSOCIATED_NODE_ID, nodeTable, Table.FKOption.CASCADE_DELETE);
		programTable.addForeignKey(NodeTable.Columns.ID, nodeTable, NodeTable.Columns.PARENT_NODE_ID,
				Table.FKOption.CASCADE_DELETE);
		nodeTable.addForeignKey(NodeTable.Columns.EXERCISE_ID, exerciseTable, Table.FKOption.CASCADE_DELETE);

		db.execSQL(exerciseTable.getCreateSql());
		db.execSQL(nodeTable.getCreateSql());
		db.execSQL(programTable.getCreateSql());

		Program tabata = new Program("Tabata Protocol");
		tabata.getAssociatedNode().setTotalReps(8);
		tabata.getAssociatedNode().addChildExercise("Hard", 20000, EffortLevel.HARD);
		tabata.getAssociatedNode().addChildExercise("Rest", 10000, EffortLevel.REST);

		saveProgram(tabata, db);

		Program c25k = new Program("Couch to 5k Week 1");
		c25k.getAssociatedNode().addChildExercise("Warm up walk", minutesInMs(5), EffortLevel.NONE);
		Node running = c25k.getAssociatedNode().addChildNode(7);
		running.addChildExercise("Run", 60000, EffortLevel.EASY);
		running.addChildExercise("Walk", 90000, EffortLevel.NONE);
		c25k.getAssociatedNode().addChildExercise("Run", 60000, EffortLevel.EASY);
		c25k.getAssociatedNode().addChildExercise("Cool down walk", minutesInMs(5), EffortLevel.NONE);

		saveProgram(c25k, db);

		Program fartlek = new Program("Fartlek");
		fartlek.getAssociatedNode().addChildExercise("Warm up", minutesInMs(15), EffortLevel.EASY);
		Node exercises = fartlek.getAssociatedNode().addChildNode(6);
		exercises.addChildExercise("Burst!", 30000, EffortLevel.HARD);
		exercises.addChildExercise("Recover", minutesInMs(3), EffortLevel.EASY);
		fartlek.getAssociatedNode().addChildExercise("Cool down", minutesInMs(10), EffortLevel.EASY);

		saveProgram(fartlek, db);

		Program pyramid = new Program("Cardio-Sprint Pyramid");
		pyramid.getAssociatedNode().addChildExercise("Warm up", minutesInMs(15), EffortLevel.EASY);

		pyramid.getAssociatedNode().addChildExercise("Sprint", 30000, EffortLevel.HARD);
		pyramid.getAssociatedNode().addChildExercise("Recover", 30000, EffortLevel.EASY);

		pyramid.getAssociatedNode().addChildExercise("Sprint", minutesInMs(1), EffortLevel.HARD);
		pyramid.getAssociatedNode().addChildExercise("Recover", minutesInMs(1), EffortLevel.EASY);

		pyramid.getAssociatedNode().addChildExercise("Sprint", minutesInMs(2), EffortLevel.HARD);
		pyramid.getAssociatedNode().addChildExercise("Recover", minutesInMs(2), EffortLevel.EASY);

		pyramid.getAssociatedNode().addChildExercise("Sprint", minutesInMs(4), EffortLevel.HARD);
		pyramid.getAssociatedNode().addChildExercise("Recover", minutesInMs(4), EffortLevel.EASY);

		pyramid.getAssociatedNode().addChildExercise("Sprint", minutesInMs(2), EffortLevel.HARD);
		pyramid.getAssociatedNode().addChildExercise("Recover", minutesInMs(2), EffortLevel.EASY);

		pyramid.getAssociatedNode().addChildExercise("Sprint", minutesInMs(1), EffortLevel.HARD);
		pyramid.getAssociatedNode().addChildExercise("Recover", minutesInMs(1), EffortLevel.EASY);

		pyramid.getAssociatedNode().addChildExercise("Sprint", 30000, EffortLevel.HARD);
		pyramid.getAssociatedNode().addChildExercise("Recover", 30000, EffortLevel.EASY);

		pyramid.getAssociatedNode().addChildExercise("Cool down", minutesInMs(10), EffortLevel.EASY);

		saveProgram(pyramid, db);
	}

	private static int minutesInMs(int ms) {
		return ms * 60000;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	@Override
	public List<ProgramMetaData> getProgramList() {
		Cursor cursor = getReadableDatabase().query(
				ProgramTable.NAME,
				new String[]{ProgramTable.Columns.ID.name, ProgramTable.Columns.NAME.name,
						ProgramTable.Columns.DESCRIPTION.name}, null, null, null, null, ProgramTable.Columns.NAME.name);

		List<ProgramMetaData> programs = new ArrayList<ProgramMetaData>(cursor.getCount());
		while (cursor.moveToNext()) {
			Program program = new Program(cursor.getString(1));
			program.setId(cursor.getLong(0));
			program.setDescription(cursor.getString(2));
			programs.add(program);
		}

		return programs;
	}

	@Override
	public Program getProgram(long programId, boolean skipCache) {
		if (skipCache) {
			cache.remove(programId);
		}

		Program program = cache.get(programId);
		if (program == null) {
			program = getProgram(programId, getReadableDatabase());
			cache.put(programId, program);
		}

		return program;
	}

	private Program getProgram(long programId, SQLiteDatabase db) {
		Cursor cursor = db.query(ProgramTable.NAME, null, programTable.getSingleQuery(programId), null, null, null,
				null);

		if (!cursor.moveToFirst()) {
			return null;
		}

		String name = cursor.getString(cursor.getColumnIndexOrThrow(ProgramTable.Columns.NAME.name));
		String description = cursor.getString(cursor.getColumnIndexOrThrow(ProgramTable.Columns.DESCRIPTION.name));

		Program program = new Program(name);
		program.setDescription(description);
		program.setId(programId);

		long associatedNodeId = cursor.getLong(cursor
				.getColumnIndexOrThrow(ProgramTable.Columns.ASSOCIATED_NODE_ID.name));

		program.setAssociatedNode(getNode(associatedNodeId, db));

		return program;
	}

	private Node getNode(long id, SQLiteDatabase db) {
		Cursor cursor = db.query(NodeTable.NAME, null, nodeTable.getSingleQuery(id), null, null, null, null);

		if (!cursor.moveToFirst()) {
			return null;
		}

		return getNodeFromCursor(cursor, db);
	}

	private Node getNodeFromCursor(Cursor cursor, SQLiteDatabase db) {
		if (cursor.isAfterLast() || cursor.isBeforeFirst()) {
			throw new IllegalArgumentException("Tried to get a node from an invalid cursor");
		}

		long id = cursor.getLong(cursor.getColumnIndexOrThrow(NodeTable.Columns.ID.name));

		Node node = new Node();
		node.setId(id);
		node.setTotalReps(cursor.getInt(cursor.getColumnIndexOrThrow(NodeTable.Columns.TOTAL_REPS.name)));
		node.setChildren(getChildrenOfNode(node, db));

		long exerciseId = cursor.getInt(cursor.getColumnIndexOrThrow(NodeTable.Columns.EXERCISE_ID.name));
		if (exerciseId > 0) {
			Exercise exercise = getExercise(exerciseId, db);
			node.setAttachedExercise(exercise);
			exercise.setNode(node);
		}

		return node;
	}

	private List<Node> getChildrenOfNode(Node parent, SQLiteDatabase db) {
		Cursor cursor = db.query(NodeTable.NAME, null, NodeTable.Columns.PARENT_NODE_ID.name + " = ?",
				new String[]{Long.toString(parent.getId())}, null, null, NodeTable.Columns.ORDER.name);

		List<Node> children = new ArrayList<Node>(cursor.getCount());

		while (cursor.moveToNext()) {
			Node child = getNodeFromCursor(cursor, db);

			child.setParent(parent);
			children.add(child);
		}

		return children;
	}

	private Exercise getExercise(long id, SQLiteDatabase db) {
		Cursor cursor = db.query(ExerciseTable.NAME, null, exerciseTable.getSingleQuery(id), null, null, null, null);

		if (!cursor.moveToFirst()) {
			return null;
		}

		Exercise exercise = new Exercise();

		exercise.setId(id);
		exercise.setName(cursor.getString(cursor.getColumnIndexOrThrow(ExerciseTable.Columns.NAME.name)));
		exercise.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(ExerciseTable.Columns.DURATION.name)));
		exercise.setEffortLevel(EffortLevel.values()[cursor.getInt(cursor
				.getColumnIndexOrThrow(ExerciseTable.Columns.EFFORT_LEVEL_ORDINAL.name))]);

		return exercise;
	}

	@Override
	public void replaceProgramNode(ProgramMetaData program, Node programNode) {

	}

	@Override
	public long saveProgram(Program program) {
		cache.delete(program.getId());
		long id = this.saveProgram(program, getWritableDatabase());
		cache.put(program.getId(), program);
		return id;
	}

	private long saveProgram(Program program, SQLiteDatabase db) {
		db.beginTransaction();

		if (program.getAssociatedNode() == null) {
			throw new IllegalArgumentException(
					"You're trying to save a program with no associated node man, what the hell!?");
		}

		// Easiest way to get the new node tree right is to just cascade-delete
		// the whole thing and start again.
		long storedNodeId = getStoredAssociatedNodeId(program, db);
		if (storedNodeId > 0) {
			deleteById(nodeTable, storedNodeId);
		}

		long nodeId = insertNode(program.getAssociatedNode(), db);

		ContentValues programValues = new ContentValues();
		programValues.put(ProgramTable.Columns.NAME.name, program.getName());
		programValues.put(ProgramTable.Columns.ASSOCIATED_NODE_ID.name, nodeId);

		long programId;
		if (program.isAlreadyPersisted()) {
			updateSingle(programTable, programValues, program, db);
			programId = program.getId();
		} else {
			programId = insert(programTable, program, programValues, db);
		}

		db.setTransactionSuccessful();
		db.endTransaction();

		return programId;
	}

	private long getStoredAssociatedNodeId(ProgramMetaData program, SQLiteDatabase db) {
		Cursor cursor = db.query(ProgramTable.NAME, new String[]{ProgramTable.Columns.ASSOCIATED_NODE_ID.name},
				programTable.getSingleQuery(program.getId()), null, null, null, null);
		if (!cursor.moveToFirst()) {
			return 0;
		}

		return cursor.getLong(0);
	}

	private void updateSingle(Table table, ContentValues values, DatabaseModel model, SQLiteDatabase db) {
		int result = db.update(table.name, values, table.getSingleQuery(model.getId()), null);
		if (result > 1) {
			throw new IllegalStateException(
					"Somehow more than one row was updated by an update intended for a single row");
		}
	}

	public long insertNode(Node node) {
		return insertNode(node, getWritableDatabase());
	}

	private long insertNode(Node node, SQLiteDatabase db) {
		ContentValues nodeValues = new ContentValues();
		nodeValues.put(NodeTable.Columns.TOTAL_REPS.name, node.getTotalReps());

		if (node.getParent() != null) {
			if (!node.getParent().isAlreadyPersisted()) {
				throw new IllegalArgumentException(
						"You're trying to insert a node whose parent isn't inserted yet. Are you high??");
			}

			nodeValues.put(NodeTable.Columns.PARENT_NODE_ID.name, node.getParent().getId());
		}

		if (node.getAttachedExercise() != null) {
			long exerciseId = saveExercise(node.getAttachedExercise(), db);
			nodeValues.put(NodeTable.Columns.EXERCISE_ID.name, exerciseId);
		}
		node.setId(insert(nodeTable, node, nodeValues, db));

		if (node.hasChildren()) {
			for (Node child : node.getChildren()) {
				insertNode(child, db);
			}
		}

		return node.getId();
	}

	// private long saveExercise(Exercise exercise) {
	// return saveExercise(exercise, getWritableDatabase());
	// }

	private long saveExercise(Exercise exercise, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(ExerciseTable.Columns.NAME.name, exercise.getName());
		values.put(ExerciseTable.Columns.DURATION.name, exercise.getDuration());
		values.put(ExerciseTable.Columns.EFFORT_LEVEL_ORDINAL.name, exercise.getEffortLevel().ordinal());

		if (exercise.isAlreadyPersisted()) {
			db.update(ExerciseTable.NAME, values, exerciseTable.getSingleQuery(exercise.getId()), null);
			return exercise.getId();
		} else {
			return insert(exerciseTable, exercise, values, db);
		}
	}

	private long insert(Table table, DatabaseModel model, ContentValues values, SQLiteDatabase db) {
		long id = db.insertOrThrow(table.name, null, values);
		model.setId(id);
		return id;
	}

	@Override
	public void deleteProgram(long programId) {
		cache.delete(programId);
		delete(programTable, programId);
	}

	private void delete(Table table, long id) {
		deleteById(table, id);
	}

	private void deleteById(Table table, long id) {
		getWritableDatabase().delete(table.name, Table.BaseColumns.ID.getWhereClause(),
				new String[]{Long.toString(id)});
	}

	private static class ProgramTable extends Table {
		static final String NAME = "Program";

		public ProgramTable() {
			super(NAME, Columns.NAME, Columns.DESCRIPTION, Columns.ASSOCIATED_NODE_ID);
		}

		static class Columns extends BaseColumns {
			static final Column NAME = new Column("name", Type.TEXT);
			static final Column DESCRIPTION = new Column("description", Type.TEXT);
			static final Column ASSOCIATED_NODE_ID = new Column("node_id", Type.INTEGER);
		}
	}

	;

	private static class NodeTable extends Table {
		static final String NAME = "Node";

		public NodeTable() {
			super(NAME, Columns.TOTAL_REPS, Columns.EXERCISE_ID, Columns.PARENT_NODE_ID, Columns.ORDER);
		}

		static class Columns extends BaseColumns {
			static final Column TOTAL_REPS = new Column("total_reps", Type.INTEGER);
			static final Column EXERCISE_ID = new Column("exercise_id", Type.INTEGER);
			static final Column PARENT_NODE_ID = new Column("parent_node_id", Type.INTEGER);
			static final Column ORDER = new Column("child_order", Type.INTEGER);
		}
	}

	private static class ExerciseTable extends Table {
		static final String NAME = "Exercise";

		public ExerciseTable() {
			super(NAME, Columns.NAME, Columns.DURATION, Columns.EFFORT_LEVEL_ORDINAL);
		}

		static class Columns extends BaseColumns {
			static final Column NAME = new Column("name", Type.TEXT);
			static final Column DURATION = new Column("duration", Type.INTEGER);
			static final Column EFFORT_LEVEL_ORDINAL = new Column("effort_level_ordinal", Type.INTEGER);
		}
	}

	private static class Table {
		private static final String SQL_LOG_TAG = "SQL";
		private final String name;
		private final Column[] columns;
		private final List<ForeignKey> foreignKeys = new ArrayList<ForeignKey>();

		public Table(String name, Column... columns) {
			this.name = name;
			this.columns = columns;
		}

		public void addForeignKey(Column from, Table toTable, FKOption options) {
			addForeignKey(from, toTable, BaseColumns.ID, options);
		}

		public void addForeignKey(Column from, Table toTable, Column toColumn, FKOption... options) {
			foreignKeys.add(new ForeignKey(from, toTable, toColumn, options));
		}

		public String getCreateSql() {
			StringBuilder builder = new StringBuilder();

			builder.append("CREATE TABLE ").append(name).append(" (\n");
			builder.append("_id INTEGER PRIMARY KEY AUTOINCREMENT,\n");
			for (int i = 0; i < columns.length; i++) {
				builder.append("  ").append(columns[i].name).append(" ").append(columns[i].type);
				if (i + 1 < columns.length || !foreignKeys.isEmpty()) {
					builder.append(",");
				}
				builder.append("\n");
			}
			Iterator<ForeignKey> keyIterator = foreignKeys.iterator();
			while (keyIterator.hasNext()) {
				ForeignKey foreignKey = keyIterator.next();
				builder.append("  FOREIGN KEY(").append(foreignKey.from.name).append(") REFERENCES ")
						.append(foreignKey.toTable.name).append("(").append(foreignKey.toColumn.name).append(")");
				if (foreignKey.options.contains(FKOption.CASCADE_DELETE)) {
					builder.append("ON DELETE CASCADE");
				}
				if (keyIterator.hasNext()) {
					builder.append(",");
				}
				builder.append("\n");
			}
			builder.append(");");

			Log.i(SQL_LOG_TAG, "Generated following SQL for the creation of " + name + " table:\n" + builder.toString());

			return builder.toString();
		}

		public String getSingleQuery(long id) {
			return "_id = " + id;
		}

		private static class ForeignKey {
			private Column from;
			private Table toTable;
			private Column toColumn;
			private EnumSet<FKOption> options = EnumSet.noneOf(FKOption.class);

			public ForeignKey(Column from, Table toTable, Column toColumn, FKOption... options) {
				this.from = from;
				this.toTable = toTable;
				this.toColumn = toColumn;
				for (FKOption option : options) {
					this.options.add(option);
				}
			}
		}

		public enum FKOption {
			CASCADE_DELETE;
		}

		static class BaseColumns {
			public static final Column ID = new Column("_id", Type.INTEGER);
		}
	}

	private static class Column {
		private final String name;
		private final Type type;

		public Column(String name, Type type) {
			this.name = name;
			this.type = type;
		}

		public String getWhereClause() {
			return name + " = ?";
		}
	}

	private enum Type {
		INTEGER, REAL, TEXT, BLOB;
	}
}