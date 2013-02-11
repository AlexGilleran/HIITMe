package com.alexgilleran.hiitme.model.impl;

import com.alexgilleran.hiitme.model.Rep;

public class RepImpl implements Rep {
	private String name;
	private int duration;
	private EffortLevel effortLevel;
	
	public RepImpl(String name, int duration, EffortLevel effortLevel) {
		super();
		this.name = name;
		this.duration = duration;
		this.effortLevel = effortLevel;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getDuration() {
		return duration;
	}

	@Override
	public EffortLevel getEffortLevel() {
		return effortLevel;
	}

}
