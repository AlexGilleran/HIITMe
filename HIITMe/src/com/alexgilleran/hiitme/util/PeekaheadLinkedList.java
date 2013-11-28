package com.alexgilleran.hiitme.util;

public class PeekaheadLinkedList<T> extends java.util.LinkedList<T> implements PeekaheadQueue<T> {
	private static final long serialVersionUID = -6285960920945368616L;

	@Override
	public T peek(int howMuch) {
		if (howMuch >= size()) {
			return null;
		}
		
		return get(howMuch);
	}
}
