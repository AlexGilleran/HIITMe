package com.alexgilleran.hiitme.model;

public abstract class DatabaseModel {
	private long id = -1;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
