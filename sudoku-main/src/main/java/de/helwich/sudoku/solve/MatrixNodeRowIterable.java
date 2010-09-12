package de.helwich.sudoku.solve;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class MatrixNodeRowIterable implements Iterable<MatrixNode> {
	
	private final MatrixNode startNode;
	
	public MatrixNodeRowIterable(MatrixNode startNode) {
		this.startNode = startNode;
	}

	@Override
	public Iterator<MatrixNode> iterator() {
		return new Iterator<MatrixNode>() {
			
			private MatrixNode current = startNode;
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public MatrixNode next() {
				try {
					return current;
				} finally {
					if (current == null)
						throw new NoSuchElementException();
					else if (current.right == startNode)
						current = null;
					else
						current = current.right;
				}
			}
			
			@Override
			public boolean hasNext() {
				return current != null;
			}
		};
	}
	
}
