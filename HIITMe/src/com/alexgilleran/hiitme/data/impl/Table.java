package com.alexgilleran.hiitme.data.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

public class Table {
	private static final String ID_WHERE_CLAUSE = Table.ID.getName() + " = ?";
	public static final Column ID = new Column("_id", Column.SQLiteType.INTEGER);

	private final SQLiteOpenHelper helper;
	private String name;
	private Set<Column> columns = new HashSet<Column>();

	public Table(SQLiteOpenHelper helper, String name, Column... columns) {
		this.helper = helper;
		this.name = name;

		this.columns.add(ID);
		for (Column column : columns) {
			if (!column.getName().equals(ID.getName())) {
				this.columns.add(column);
			}
		}
	}

	public String getName() {
		return name;
	}

	public Set<Column> getColumns() {
		return columns;
	}

	protected Cursor getForId(String tableName, String id) {
		Cursor cursor = helper.getReadableDatabase().query(tableName, null,
				ID_WHERE_CLAUSE, new String[] { id }, null, null, null);
		if (cursor.moveToFirst()) {
			return cursor;
		}

		return null;
	}

	public String getCreateSql() {
		StringBuilder builder = new StringBuilder();
		builder.append("CREATE TABLE ");
		builder.append(name);
		builder.append(" (");

		Iterator<Column> it = columns.iterator();

		while (it.hasNext()) {
			Column column = it.next();
			builder.append(column.getName());
			builder.append(" ");
			builder.append(column.getType());

			if (it.hasNext()) {
				builder.append(",");
			}
		}

		builder.append(");");

		return builder.toString();
	}

	public String getDropSql() {
		return "DROP TABLE " + name;
	}
}
