package com.alexgilleran.hiitme.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
		nodeTable.addForeignKey(NodeTable.Columns.exerciseId, exerciseTable);

		db.execSQL(nodeTable.getCreateSql());
		db.execSQL(exerciseTable.getCreateSql());
		db.execSQL(programTable.getCreateSql());

		Program tabata = new Program("Tabata", "The tabata protocol");
		tabata.getAssociatedNode().addChildExercise("Hard", 2000, EffortLevel.HARD, 1);
		tabata.getAssociatedNode().addChildExercise("Rest", 1000, EffortLevel.REST, 1);

		insertProgram(tabata, db);

		Program nestTest = new Program("NestTest", "A nested test program");
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
	public List<Program> getAllPrograms() {
		Cursor cursor = getReadableDatabase().query(ProgramTable.name,
				new String[] { ProgramTable.Columns.name.name, ProgramTable.Columns.description.name }, null, null,
				null, null, ProgramTable.Columns.name.name);

		List<Program> programs = new ArrayList<Program>(cursor.getCount());
		while (cursor.moveToNext()) {
			programs.add(new Program(cursor.getString(0), cursor.getString(1)));
		}

		return programs;
	}

	@Override
	public Program getProgram(long programId) {
		return null;
	}

	@Override
	public void replaceProgramNode(Program program, Node programNode) {

	}

	@Override
	public long insertProgram(Program program) {
		return this.insertProgram(program, getWritableDatabase());
	}

	public long insertProgram(Program program, SQLiteDatabase db) {
		if (program.getAssociatedNode() == null) {
			throw new IllegalArgumentException("This program has no associated node man, what the hell!?");
		}

		long nodeId = insertNode(program.getAssociatedNode(), db);

		ContentValues programValues = new ContentValues();
		programValues.put(ProgramTable.Columns.name.name, program.getName());
		programValues.put(ProgramTable.Columns.description.name, program.getDescription());
		programValues.put(ProgramTable.Columns.ASSOCIATED_NODE_ID.name, nodeId);

		return db.insertOrThrow(ProgramTable.name, null, programValues);
	}

	private long insertNode(Node node) {
		return insertNode(node, getWritableDatabase());
	}

	private long insertNode(Node node, SQLiteDatabase db) {
		ContentValues nodeValues = new ContentValues();
		nodeValues.put(NodeTable.Columns.totalReps.name, node.getTotalReps());
		if (node.getAttachedExercise() != null) {
			long exerciseId = saveExercise(node.getAttachedExercise(), db);
			nodeValues.put(NodeTable.Columns.exerciseId.name, exerciseId);
		}
		long id = insert(nodeTable, node, nodeValues, db);

		if (node.hasChildren()) {
			for (Node child : node.getChildren()) {
				insertNode(child, db);
			}
		}

		return id;
	}

	private long saveExercise(Exercise exercise) {
		return saveExercise(exercise, getWritableDatabase());
	}

	private long saveExercise(Exercise exercise, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(ExerciseTable.Columns.name.name, exercise.getName());
		values.put(ExerciseTable.Columns.duration.name, exercise.getDuration());
		values.put(ExerciseTable.Columns.effortLevelId.name, exercise.getEffortLevel().ordinal());

		if (exercise.getId() > 0) {
			db.update(exerciseTable.name, values, exerciseTable.getSingleQuery(exercise.getId()),
					null);
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
		getWritableDatabase().delete(table.name, Table.ID.getWhereClause(), new String[] { Long.toString(id) });
	}

	private static class ProgramTable extends Table {
		static final String name = "Program";

		public ProgramTable() {
			super(name, Columns.name, Columns.description, Columns.ASSOCIATED_NODE_ID);
		}

		static class Columns {
			static final Column name = new Column("name", Type.TEXT);
			static final Column description = new Column("description", Type.TEXT);
			static final Column ASSOCIATED_NODE_ID = new Column("node_id", Type.INTEGER);
		}
	};

	private static class NodeTable extends Table {
		static final String name = "Node";

		public NodeTable() {
			super(name, Columns.totalReps, Columns.exerciseId, Columns.parentNodeId);
		}

		static class Columns {
			static final Column totalReps = new Column("total_reps", Type.INTEGER);
			static final Column exerciseId = new Column("exercise_id", Type.INTEGER);
			static final Column parentNodeId = new Column("parent_node_id", Type.INTEGER);
		}
	}

	private static class ExerciseTable extends Table {
		static final String name = "Exercise";

		public ExerciseTable() {
			super(name, Columns.name, Columns.duration, Columns.effortLevelId);
		}

		static class Columns {
			static final Column name = new Column("name", Type.TEXT);
			static final Column duration = new Column("duration", Type.INTEGER);
			static final Column effortLevelId = new Column("effort_level_id", Type.INTEGER);
		}
	}

	private static class Table {
		public static final Column ID = new Column("_id", Type.INTEGER);

		private final String name;
		private final Column[] columns;
		private final List<ForeignKey> foreignKeys = new ArrayList<ForeignKey>();

		public Table(String name, Column... columns) {
			this.name = name;
			this.columns = columns;
		}

		public void addForeignKey(Column from, Table toTable) {
			foreignKeys.add(new ForeignKey(from, toTable, Table.ID));
		}

		public void addForeignKey(Column from, Table toTable, Column toColumn) {
			foreignKeys.add(new ForeignKey(from, toTable, toColumn));
		}

		public String getCreateSql() {
			StringBuilder builder = new StringBuilder();

			builder.append("CREATE TABLE ").append(name).append(" (\n");
			for (int i = 0; i < columns.length; i++) {
				builder.append("  ").append(columns[i].name).append(" ").append(columns[i].type);
				if (i + 1 < columns.length) {
					builder.append(",");
				}
				builder.append("\n");
			}
			builder.append(");");

			return builder.toString();
		}

		public String getSingleQuery(long id) {
			return "SELECT * FROM " + name + " WHERE _id = " + id;
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