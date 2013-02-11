package com.alexgilleran.hiitme.model.impl;

import java.util.List;

import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.Rep;
import com.alexgilleran.hiitme.model.RepGroup;

public class ProgramImpl implements Program {
	private long id;
	
	/** Name of the program */
	private String name;
	/** Description **/
	private String description;
	/** List of the rep groups */
	private List<RepGroup> repGroups;
	
	private Rep warmUp = null;

	public ProgramImpl(long id, String name, String description, List<RepGroup> setGroups) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.repGroups = setGroups;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public List<RepGroup> getRepGroups() {
		return repGroups;
	}

	@Override
	public Rep getWarmUp() {
		return warmUp;
	}
}
