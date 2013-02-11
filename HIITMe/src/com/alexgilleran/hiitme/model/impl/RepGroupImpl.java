package com.alexgilleran.hiitme.model.impl;

import java.util.List;

import com.alexgilleran.hiitme.model.Rep;
import com.alexgilleran.hiitme.model.RepGroup;

public class RepGroupImpl implements RepGroup {
	private int repCount;
	private List<Rep> reps;
	
	public RepGroupImpl(int repCount, List<Rep> reps) {
		super();
		this.repCount = repCount;
		this.reps = reps;
	}

	@Override
	public int getRepCount() {
		return repCount;
	}

	@Override
	public List<Rep> getReps() {
		return reps;
	}
}
