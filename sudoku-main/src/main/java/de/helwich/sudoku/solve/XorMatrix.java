package de.helwich.sudoku.solve;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Hendrik Helwich
 */
public class XorMatrix {

	private final Map<Integer, MatrixNode> firstRowNodes;
	private final Map<Integer, MatrixNode> firstColumnNodes;
	private List<XorMatrixChangeHandler> handlers;
	private LinkedList<MatrixNode> removedNodes;
	
	/**
	 * Must only be called by {@link XorMatrixFactory}.
	 * 
	 * @param firstRowNodes
	 */
	XorMatrix(Map<Integer, MatrixNode> firstRowNodes, Map<Integer, MatrixNode> firstColumnNodes) {
		this.firstRowNodes = firstRowNodes;
		this.firstColumnNodes = firstColumnNodes;
		removedNodes = new LinkedList<MatrixNode>();
	}
	
	public void addChangeHandler(XorMatrixChangeHandler handler) {
		if (handlers == null)
			handlers = new LinkedList<XorMatrixChangeHandler>();
		handlers.add(handler);
	}
	
	public boolean removeChangeHandler(XorMatrixChangeHandler handler) {
		if (handlers != null)
			return handlers.remove(handler);
		return false;
	}
	
	private void notifyRemovedRows(Set<Integer> rows) {
		if (handlers != null)
			for (XorMatrixChangeHandler handler : handlers)
				handler.onRemoveRows(rows);
	}
	
	public int removeRow(int row) {
		MatrixNode node = firstRowNodes.get(row);
		if (node != null) {
			Set<Integer> removedRows = new TreeSet<Integer>();//TODO get from pool, Hashset?
			

			removeRow___(node);
			removeRow_(node, removedRows);
			removedRows.add(row);
			//List<Integer> removeRowsLater = new ArrayList<Integer>(); //TODO get from pool
			/*
			List<MatrixNode> removed = new LinkedList<MatrixNode>(); //TODO get from pool
			while (node.right != node) {
				removeNode(node);
				removed.add(node);
				node = node.right;
			} 
			removeNode(node);
			removed.add(node);
			for (MatrixNode n : removed)
				removeNodeAndEffect(n, removeRowsLater);
			removeNodeAndEffect(node,removeRowsLater);
				*/
			//for (int i : removedRows)
			notifyRemovedRows(removedRows);
		}
		return removedNodes.size();
	}
	
	/**
	 * 
	 * 
	 * @param node
	 * @param removedRows 
	 */
	private void removeRow_(MatrixNode node, Set<Integer> removedRows) {
		
		
		
		//boolean ret = removeNode(node, true); // TODO collect notifications and do notification at end of the root operation
		//assert ret; // must be true because is not a single node (see above)
		boolean singleInColumn = node == node.down;
		// remove all remaining nodes in the row of the node (and calculate the
		// effect) until the row is empty
		if (node != node.right) {
			// remove the column of the given node because it should be ignored by
			// the recursive calls
			if (!singleInColumn)
				removeColumn(node.down);
			removeRow_(node.right, removedRows);
			if (!singleInColumn)
				// the removing of the column can be undone
				reInsertColumn(node.down);
		}
		if (!singleInColumn) {
			// calculate the effect for removing the current node
			calculateEffect(node.down, removedRows);
		}
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
	private void calculateEffect(MatrixNode node, Set<Integer> removedRows) {
		
		removeColumn(node);
		

		List<MatrixNode> column = new ArrayList<MatrixNode>(firstRowNodes.size()); //TODO get from pool
		int maxcol = Integer.MIN_VALUE;
		int mincol = Integer.MAX_VALUE;
		int height = 0;
		for (MatrixNode n : new RemovedColumnIterator(node)) {
			MatrixNode first = firstRowNodes.get(n.row);
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
						return; // no unprocessed node left in the row => return
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
		
		//reInsertColumn(node);
		
		//TODO implement
		
	}
	/**
	 * Removes the column of the given node from the matrix. This operation is
	 * undoable by calling {@link #reInsertColumn(MatrixNode)} for the given
	 * node on a matrix in the same state as after this operation.
	 * This operation is intended for private use but has class visibility to
	 * enable unit testing. 
	 * 
	 * @param node
	 */
	void removeColumn(MatrixNode node) {
		removeNode(node, false);
		if (node != node.down)
			removeColumn(node.down);
	}

	/**
	 * Removes the row of the given node from the matrix.
	 * This operation is intended for private use but has class visibility to
	 * enable unit testing. 
	 * 
	 * @param node
	 */
	void removeRow___(MatrixNode node) {
		removeNode(node, true);
		if (node != node.right)
			removeRow___(node.right);
	}

	/**
	 * Undoes the effect of operation {@link #removeColumn(MatrixNode)} when
	 * called on a matrix state which is the same as directly after the
	 * operation and if the given node is the same instance as in the called
	 * operation.
	 * This operation is intended for private use but has class visibility to
	 * enable unit testing. 
	 * 
	 * @param node
	 */
	void reInsertColumn(MatrixNode node) {
		if (node != node.down)
			reInsertColumn(node.down);
		node.reInsert();
		adaptRowFirstIndexOnInsert(node);
	}
	
	// for testing
	MatrixNode getRowFirstNode(int row) {
		return firstRowNodes.get(row);
	}
	
	/**
	 * Removes the given node from the matrix (if it is not removed before) and
	 * stores it so that the effect of this operation can be undone later.
	 * The index of the first row nodes of the matrix is adapted if the removed
	 * node was the first node of its row.
	 * If the removed node was the last node of its row, a row remove
	 * notification is send to the registered change handlers.
	 * 
	 * @param  node
	 * @return 
	 */
	private boolean removeNode(MatrixNode node, boolean store) {
		boolean removed = node.remove();
		if (removed && store)
			removedNodes.add(node);
		// if node is a single node we do not know if it is removed before
		if (adaptRowFirstIndexOnRemove(node))
			;//notifyChangeHandler(node.row);
		return removed;
	}
	
	private boolean adaptRowFirstIndexOnRemove(MatrixNode node) {
		// if node is an element of first column array => adapt array
		if (firstRowNodes.get(node.row) == node)
			if (node.right == node) { // single node in the row
				firstRowNodes.remove(node.row);
				return true;
			} else
				firstRowNodes.put(node.row, node.right);
		return false;
	}

	private void adaptRowFirstIndexOnInsert(MatrixNode node) {
		MatrixNode frn = firstRowNodes.get(node.row);
		if (frn == null || (node.right == frn && frn.right != node))
			firstRowNodes.put(node.row, node);
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

	private void removeRow(MatrixNode node, Set<Integer> removeRowsLater) {
		removeNode(node, true);
		removeRowsLater.add(node.row);
	}

	// remove all given column nodes. If the column does have more nodes which
	// are not in the argument list, remove the row for each of this nodes.
	private void removeColumn(List<MatrixNode> columNodes, Set<Integer> removeRowsLater) {
		for (int i = columNodes.size()-1; i>= 0; i--)
			removeNode(columNodes.get(i), true);
		MatrixNode node = columNodes.get(0); // last removed node
		if (node != node.up)
			removeRow(node.up, removeRowsLater);
			//removeRowsOfColumn(node.up, removeRowsLater);
	}

	
	/**
	 * Remove all rows of the column of the given node.
	 */
//	private void removeRowsOfColumn(MatrixNode node, List<Integer> removeRowsLater) {
//		for (removeRow(node, removeRowsLater); node != node.up; node = node.up)
//			removeRow(node.up, removeRowsLater);
//	}

	public void restoreNodes(int matrixStateId) {
		if (matrixStateId < 0)
			throw new IllegalArgumentException("argument must not be negative");
		// calculate the number of node which must be removed
		int removeCount = removedNodes.size() - matrixStateId;
		// remove removeCount nodes
		for (int i = 0; i < removeCount; i++) {
			// get last removed node
			MatrixNode node = removedNodes.removeLast();
			// reinsert node in matrix
			node.reInsert();
			adaptRowFirstIndexOnInsert(node);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int maxrow = Integer.MIN_VALUE;
		for (MatrixNode first : firstRowNodes.values()) 
			maxrow = Math.max(maxrow, first.row);
		for (int i = 0; i <= maxrow; i++) {
			MatrixNode first = firstRowNodes.get(i);
			sb.append(i).append("  |");
			int column = 0;
			for (MatrixNode node : new MatrixNodeRowIterable(first)) {
				for (; column < node.column; column++)
					sb.append("  ");
				sb.append(" X");
				column++;
			}
			sb.append('\n');
		}
		return sb.toString();
	}
	
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

