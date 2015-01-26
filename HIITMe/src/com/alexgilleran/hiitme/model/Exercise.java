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

package com.alexgilleran.hiitme.model;

import com.alexgilleran.hiitme.util.ViewUtils;

public class Exercise extends DatabaseModel implements Cloneable {
	private String name;
	private int duration;
	private EffortLevel effortLevel;
	private Node exerciseGroup;

	public Exercise() {
		super();

		this.duration = 30000;
		this.effortLevel = EffortLevel.NONE;
	}

	public Exercise(int duration, EffortLevel effortLevel, Node node) {
		this.duration = duration;
		this.effortLevel = effortLevel;
		this.exerciseGroup = node;
	}

	public Exercise(String name, int duration, EffortLevel effortLevel, Node node) {
		this(duration, effortLevel, node);

		this.name = name;
	}

	public int getDuration() {
		return duration;
	}

	public EffortLevel getEffortLevel() {
		return effortLevel;
	}

	public Node getParentNode() {
		return exerciseGroup;
	}

	public void setNode(Node exerciseGroup) {
		this.exerciseGroup = exerciseGroup;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setEffortLevel(EffortLevel effortLevel) {
		this.effortLevel = effortLevel;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the number of minutes this exercise lasts, rounded down.
	 */
	public int getMinutes() {
		return ViewUtils.getMinutesFromMs(duration);
	}

	/**
	 * Gets the number of seconds this exercise lasts, not including minutes.
	 */
	public int getSeconds() {
		return ViewUtils.getSecondsFromMs(duration);
	}

	@Override
	public Exercise clone() {
		Exercise clone = new Exercise();
		clone.setDuration(duration);
		clone.setEffortLevel(effortLevel);
		clone.setName(name);

		return clone;
	}
}
