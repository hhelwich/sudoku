package de.helwich.sudoku.solve;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author Hendrik Helwich
 */
public class XorMatrix extends BMatrix {

	
	/**
	 * Must only be called by {@link XorMatrixFactory}.
	 * 
	 * @param firstRowNodes
	 */
	XorMatrix(Map<Integer, MatrixNode> firstRowNodes) {
		super(firstRowNodes);
	}
	



	@Override
	void removeRow(int rowIndex) {
		List<Integer> removedRows = new LinkedList<Integer>();
		removedRows.add(rowIndex);
		removeRow(removedRows);
	}
	
	private void removeRow(List<Integer> removedRows) {
		if (removedRows.isEmpty()) {
			notifyChanges();
			return;
		}
		int rowIndex = removedRows.remove(0);
		MatrixNode node = getRowFirstNode(rowIndex);
		if (node != null) {
			super.removeRow(node);
			removeRow_(node, removedRows);
		}
		removeRow(removedRows);
	}



	private Set<Integer> removedRows = new HashSet<Integer>();
	private Set<Integer> insertedRows = new HashSet<Integer>();
	
	@Override
	protected void notifyRemovedRow(int rowIndex) {
		if (!insertedRows.remove(rowIndex));
			removedRows.add(rowIndex);
	}



	@Override
	protected void notifyInsertedRow(int rowIndex) {
		if (!removedRows.remove(rowIndex));
			insertedRows.add(rowIndex);
	}
	
	

	private void notifyChanges() {
		for (int rowIndex : removedRows)
			super.notifyRemovedRow(rowIndex);
		removedRows.clear();
		for (int rowIndex : insertedRows)
			super.notifyInsertedRow(rowIndex);
		insertedRows.clear();
	}


	/**
	 * 
	 * 
	 * @param node
	 * @param removedRows 
	 */
	private void removeRow_(MatrixNode node, List<Integer> removedRows) {
		//boolean ret = removeNode(node, true); // TODO collect notifications and do notification at end of the root operation
		//assert ret; // must be true because is not a single node (see above)
		if (node.isSingleInColumn()) {
			if (!node.isSingleInRow())
				removeRow_(node.right, removedRows);
			return;
		}
			
		// remove all remaining nodes in the row of the node (and calculate the
		// effect) until the row is empty
		if (!node.isSingleInRow()) {
			// remove the column of the given node because it should be ignored by
			// the recursive calls
			int state = getRemovedNodesCount();
			removeColumn(node.down);
			removeRow_(node.right, removedRows);
			// the removing of the column can be undone
			undoRemove(state);
		}
		// calculate the effect for removing the current node
		calculateEffect(node.down, removedRows);
		// the node can be removed now
		//removeNode(node, true); // TODO collect notifications and do notification at end of the root operation

	}



	/**
	 * ///The given node and its column is removed before by
	 * /// {@link #removeColumn(MatrixNode)}.
	 * Calculate the effect for removing a node which was single in its row and
	 * was in the column which is given by its top node and return the rows of
	 * the matrix which should also be removed.
	 * 
	 * @param node
	 */
	private void calculateEffect(MatrixNode node, List<Integer> removedRows) {
		int stateFrom = getRemovedNodesCount();
		
		removeColumn(node);

		int stateTo = getRemovedNodesCount();
		

		List<MatrixNode> column = new ArrayList<MatrixNode>(getHeight()); //TODO get from pool
		int maxcol = Integer.MIN_VALUE;
		int mincol = Integer.MAX_VALUE;
		int height = 0;
		for (MatrixNode n : new RemovedColumnIterator(node)) {
			MatrixNode first = getRowFirstNode(n.row);
			if (first == null)  { //TODO more elegant ?
				undoRemove(stateFrom, stateTo);
				return;
			}
			column.add(first);
			maxcol = max(maxcol, first.column);
			mincol = min(mincol, first.column);
			height++;
		}
		
	/*
		int height = 1; // count the number of nodes in the given column
		// exit operation if the given column does contain a single row node
		for (MatrixNode n = node;; n = n.down, height++) { // iterate over all column nodes
			if (n == n.right) // if column node id single row node: exit operation
				return;
			if (n == n.down) // break if end of column is reached
				break;
		}
	
		// add all column nodes to a list; each row has >= 2 nodes
		List<MatrixNode> column = new ArrayList<MatrixNode>(height);//TODO get from pool?
		int maxcol = Integer.MIN_VALUE;
		MatrixNode n = node;
		for (int i = 0; i < height; i++) {
			maxcol = max(maxcol, node.right.column);
			column.add(node.right);
			n = n.down;
		}*/
		
		// search for complete columns in all given rows (without the column of
		// the deleted node) and remove 
		outerloop:
		for (int ccol = maxcol;;) {
			for (int i = 0; i < height; i++) { // iterate over all rows in the column
				MatrixNode cnode = column.get(i);
				while (cnode.column < ccol) {
					if (cnode == cnode.right || cnode.right.column < cnode.column)
						break outerloop; // no unprocessed node left in the row => return
					// skip to next unprocessed node in the row
					cnode = cnode.right;
					column.set(i, cnode);
				}
				if (cnode.column > ccol) { // no complete column for all rows at maxcol => skip incomplete columns
					ccol = cnode.column;
					continue outerloop;
				}
			}
			// found a complete column in maxcol for the rows
			removeColumn(column, removedRows);
			// skip to next column
			ccol++;
		}
		
		undoRemove(stateFrom, stateTo);
		
	}

	


	/**
	 * Returns <code>true</code> if the column of the given node does have the
	 * expected height.
	 * 
	 * @param  columnNode
	 *         A node which specifies a column
	 * @param  expectedHeight
	 *         The expected height of the given column
	 * @return <code>true</code> if the column of the given node does have the
	 *         expected height
	 */
	private static boolean isColumnHeightEqual(MatrixNode columnNode, int expectedHeight) {
		assert expectedHeight >= 1;
		MatrixNode node = columnNode;
		for (int i = 1; i <= expectedHeight; i++) {
			node = node.up;
			if (node == columnNode)
				return expectedHeight == i;
		}
		return false;
	}

	/**
	 * Returns the the width of the row of the the given node or <code>-1</code>
	 * if the row width is greater than <code>checkTillWidth</code>.
	 * 
	 * @param  rowNode
	 *         A node which specifies a row
	 * @param  checkTillWidth
	 *         The maximum width till which the row is checked. This can be the
	 *         expected row width.
	 * @return The the width of the row of the the given node or <code>-1</code>
	 *         if the row width is greater than <code>checkTillWidth</code>.
	 */
	private static int getRowWidth(MatrixNode rowNode, int checkTillWidth) {
		assert checkTillWidth >= 1;
		int width = 1;
		for (MatrixNode node = rowNode.right; node != rowNode; node = node.right)
			if (++width > checkTillWidth)
				return -1;
		return width;
	}
	
	private static int getRowWidth(MatrixNode rowNode) {
		return getRowWidth(rowNode, Integer.MAX_VALUE);
	}

	private void removeRow(MatrixNode node, List<Integer> removeRowsLater) {
		removeNode(node);
		removeRowsLater.add(node.row);
	}

	// remove all given column nodes. If the column does have more nodes which
	// are not in the argument list, remove the row for each of this nodes.
	private void removeColumn(List<MatrixNode> columNodes, List<Integer> removeRowsLater) {
		for (int i = columNodes.size()-1; i>= 0; i--)
			removeNode(columNodes.get(i));
		MatrixNode node = columNodes.get(0); // last removed node
		while (node != node.up) {
			removeRow(node.up, removeRowsLater);
			node = node.up;
		}
	}

	
	/**
	 * Remove all rows of the column of the given node.
	 */
//	private void removeRowsOfColumn(MatrixNode node, List<Integer> removeRowsLater) {
//		for (removeRow(node, removeRowsLater); node != node.up; node = node.up)
//			removeRow(node.up, removeRowsLater);
//	}




	
}






/**
 * Use this class to iterate over the nodes of a column which is removed
 * before by {@link XorMatrix#removeColumn(MatrixNode)}.
 * The nodes must be removed in the order from top to bottom.
 * The top node must be given to the constructor.
 */
class RemovedColumnIterator implements Iterable<MatrixNode> {
	
	private final MatrixNode topNode;
	
	public RemovedColumnIterator(MatrixNode topNode) {
		this.topNode = topNode;
	}

	@Override
	public Iterator<MatrixNode> iterator() {
		return new Iterator<MatrixNode>() {

			private MatrixNode next = topNode;
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public MatrixNode next() {
				if (next == null)
					throw new NoSuchElementException();
				try {
					return next;
				} finally {
					next = next == next.down ? null : next.down;
				}
			}
			
			@Override
			public boolean hasNext() {
				return next != null;
			}
		};
	}
}

