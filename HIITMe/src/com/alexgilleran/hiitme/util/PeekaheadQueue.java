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
