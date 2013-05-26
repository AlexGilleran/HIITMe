package com.alexgilleran.hiitme.data.impl;

public class Column {
	private String name;
	private SQLiteType type;

	public Column(String name, SQLiteType type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public SQLiteType getType() {
		return type;
	}

	public enum SQLiteType {
		INTEGER, REAL, TEXT, BLOB;
	}
}
