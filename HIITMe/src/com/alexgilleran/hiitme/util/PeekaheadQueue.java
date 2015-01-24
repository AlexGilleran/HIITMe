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

package com.alexgilleran.hiitme.util;

import java.util.Queue;

public interface PeekaheadQueue<T> extends Queue<T> {
	/**
	 * Peeks n entries up the queue - <code>peek(0)</code> is equivalent to
	 * {@link #peek()}.
	 * 
	 * @param howMuch
	 *            How many entries up the queue to peek up.
	 * @return The entry at the desired position, or null if the queue isn't big
	 *         enough.
	 */
	T peek(int howMuch);
}
