package com.alexgilleran.hiitme.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.alexgilleran.hiitme.model.DatabaseModel;
import com.alexgilleran.hiitme.model.EffortLevel;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Node;
import com.alexgilleran.hiitme.model.Program;
import com.google.inject.Inject;

public class ProgramDAOSqlite extends SQLiteOpenHelper implements ProgramDAO {
	private ExerciseTable exerciseTable = new ExerciseTable();
	private NodeTable nodeTable = new NodeTable();
	private ProgramTable programTable = new ProgramTable();

	@Inject
	public ProgramDAOSqlite(Context context) {
		super(context, "hiitme.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		programTable.addForeignKey(ProgramTable.Columns.ASSOCIATED_NODE_ID, nodeTable);
		nodeTable.addForeignKey(NodeTable.Columns.EXERCISE_ID, exerciseTable);

		db.execSQL(exerciseTable.getCreateSql());
		db.execSQL(nodeTable.getCreateSql());
		db.execSQL(programTable.getCreateSql());

		Program tabata = new Program("Tabata", "The tabata protocol");
		tabata.getAssociatedNode().setTotalReps(8);
		tabata.getAssociatedNode().addChildExercise("Hard", 2000, EffortLevel.HARD, 1);
		tabata.getAssociatedNode().addChildExercise("Rest", 1000, EffortLevel.REST, 1);

		insertProgram(tabata, db);

		Program nestTest = new Program("NestTest", "A nested test program");
		nestTest.getAssociatedNode().setTotalReps(2);
		Node nestNode1 = nestTest.getAssociatedNode().addChildNode(2);
		Node nestNode11 = nestNode1.addChildNode(2);
		Node nestNode111 = nestNode11.addChildNode(1);
		nestNode111.addChildExercise("Ex1", 1000, EffortLevel.HARD, 2);
		nestNode111.addChildExercise("Ex2", 2000, EffortLevel.HARD, 3);
		Node nestNode12 = nestNode1.addChildNode(3);
		nestNode12.addChildExercise("Ex3", 1500, EffortLevel.EASY, 1);
		Node nestNode2 = nestTest.getAssociatedNode().addChildNode(1);
		nestNode2.addChildExercise("Ex4", 1000, EffortLevel.REST, 1);

		insertProgram(nestTest, db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	@Override
	public List<Program> getProgramList() {
		Cursor cursor = getReadableDatabase()
				.query(ProgramTable.NAME,
						new String[] { ProgramTable.Columns.ID.name, ProgramTable.Columns.NAME.name,
								ProgramTable.Columns.DESCRIPTION.name }, null, null, null, null,
						ProgramTable.Columns.NAME.name);

		List<Program> programs = new ArrayList<Program>(cursor.getCount());
		while (cursor.moveToNext()) {
			Program program = new Program(cursor.getString(1), cursor.getString(2));
			program.setId(cursor.getLong(0));
			programs.add(program);
		}

		return programs;
	}

	@Override
	public Program getProgram(long programId) {
		return getProgram(programId, getReadableDatabase());
	}

	private Program getProgram(long programId, SQLiteDatabase db) {
		Cursor cursor = db.query(ProgramTable.NAME, null, programTable.getSingleQuery(programId), null, null, null,
				null);

		if (!cursor.moveToFirst()) {
			return null;
		}

		String name = cursor.getString(cursor.getColumnIndexOrThrow(ProgramTable.Columns.NAME.name));
		String description = cursor.getString(cursor.getColumnIndexOrThrow(ProgramTable.Columns.DESCRIPTION.name));

		Program program = new Program(name, description);
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
				new String[] { Long.toString(parent.getId()) }, null, null, NodeTable.Columns.ORDER.name);

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
	public void replaceProgramNode(Program program, Node programNode) {

	}

	@Override
	public long insertProgram(Program program) {
		return this.insertProgram(program, getWritableDatabase());
	}

	private long insertProgram(Program program, SQLiteDatabase db) {
		if (program.getAssociatedNode() == null) {
			throw new IllegalArgumentException("This program has no associated node man, what the hell!?");
		}

		long nodeId = insertNode(program.getAssociatedNode(), db);

		ContentValues programValues = new ContentValues();
		programValues.put(ProgramTable.Columns.NAME.name, program.getName());
		programValues.put(ProgramTable.Columns.DESCRIPTION.name, program.getDescription());
		programValues.put(ProgramTable.Columns.ASSOCIATED_NODE_ID.name, nodeId);

		return db.insertOrThrow(ProgramTable.NAME, null, programValues);
	}

	public long insertNode(Node node) {
		return insertNode(node, getWritableDatabase());
	}

	private long insertNode(Node node, SQLiteDatabase db) {
		ContentValues nodeValues = new ContentValues();
		nodeValues.put(NodeTable.Columns.TOTAL_REPS.name, node.getTotalReps());

		if (node.getParent() != null && node.getParent().getId() > 0) {
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

	private long saveExercise(Exercise exercise) {
		return saveExercise(exercise, getWritableDatabase());
	}

	private long saveExercise(Exercise exercise, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(ExerciseTable.Columns.NAME.name, exercise.getName());
		values.put(ExerciseTable.Columns.DURATION.name, exercise.getDuration());
		values.put(ExerciseTable.Columns.EFFORT_LEVEL_ORDINAL.name, exercise.getEffortLevel().ordinal());

		if (exercise.getId() > 0) {
			db.update(exerciseTable.NAME, values, exerciseTable.getSingleQuery(exercise.getId()), null);
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

	private void deleteNode(Node node) {
		if (node.hasChildren()) {
			for (Node child : node.getChildren()) {
				deleteNode(node);
			}
		}
		if (node.getAttachedExercise() != null) {
			deleteExercise(node.getAttachedExercise());
		}

		deleteById(nodeTable, node.getId());
	}

	private void deleteExercise(Exercise exercise) {
		deleteById(exerciseTable, exercise.getId());
	}

	@Override
	public void deleteProgram(Program program) {
		deleteById(programTable, program.getId());
	}

	private void deleteById(Table table, long id) {
		getWritableDatabase().delete(table.name, Table.BaseColumns.ID.getWhereClause(),
				new String[] { Long.toString(id) });
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
	};

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
		private final String name;
		private final Column[] columns;
		private final List<ForeignKey> foreignKeys = new ArrayList<ForeignKey>();

		public Table(String name, Column... columns) {
			this.name = name;
			this.columns = columns;
		}

		public void addForeignKey(Column from, Table toTable) {
			addForeignKey(from, toTable, BaseColumns.ID);
		}

		public void addForeignKey(Column from, Table toTable, Column toColumn) {
			foreignKeys.add(new ForeignKey(from, toTable, toColumn));
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
				if (keyIterator.hasNext()) {
					builder.append(",");
				}
				builder.append("\n");
			}
			builder.append(");");

			Log.i("SQL", "Generated following SQL for the creation of " + name + " table:\n" + builder.toString());

			return builder.toString();
		}

		public String getSingleQuery(long id) {
			return "_id = " + id;
		}

		private static class ForeignKey {
			private Column from;
			private Table toTable;
			private Column toColumn;

			public ForeignKey(Column from, Table toTable, Column toColumn) {
				this.from = from;
				this.toTable = toTable;
				this.toColumn = toColumn;
			}
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