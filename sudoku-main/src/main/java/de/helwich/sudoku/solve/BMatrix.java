package de.helwich.sudoku.solve;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BMatrix {

	private final Map<Integer, MatrixNode> firstRowNodes;
	private List<BMatrixChangeHandler> handlers;
	private List<MatrixNode> removedNodes;
	
	/**
	 * Must only be called by {@link XorMatrixFactory}.
	 * 
	 * @param firstRowNodes
	 */
	BMatrix(Map<Integer, MatrixNode> firstRowNodes) {
		this.firstRowNodes = firstRowNodes;
		removedNodes = new ArrayList<MatrixNode>();
		handlers = new LinkedList<BMatrixChangeHandler>();
	}
	
	public void addChangeHandler(BMatrixChangeHandler handler) {
		handlers.add(handler);
	}
	
	public boolean removeChangeHandler(BMatrixChangeHandler handler) {
		return handlers.remove(handler);
	}
	
	protected void notifyRemovedRow(int rowIndex) {
		for (BMatrixChangeHandler handler : handlers)
			handler.onRemoveRow(rowIndex);
	}
	
	protected void notifyInsertedRow(int rowIndex) {
		for (BMatrixChangeHandler handler : handlers)
			handler.onInsertRow(rowIndex);
	}
	
	public MatrixNode getRowFirstNode(int rowIndex) {
		return firstRowNodes.get(rowIndex);
	}
	
	public int getHeight() {
		return firstRowNodes.size();
	}
	
	private void adaptRowFirstIndexOnRemove(MatrixNode node) {
		// if node is an element of first column array => adapt array
		if (firstRowNodes.get(node.row) == node)
			if (node.isSingleInRow()) { // single node in the row
				firstRowNodes.remove(node.row);
				notifyRemovedRow(node.row);
			} else
				firstRowNodes.put(node.row, node.right);
	}

	private void adaptRowFirstIndexOnInsert(MatrixNode node) {
		MatrixNode frn = firstRowNodes.get(node.row);
		if (frn == null) {
			firstRowNodes.put(node.row, node);
			notifyInsertedRow(node.row);
		} else if (node.column < frn.column)
			firstRowNodes.put(node.row, node);
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
	protected void removeNode(MatrixNode node) {
		node.remove();
		removedNodes.add(node);
		// if node is a single node we do not know if it is removed before
		adaptRowFirstIndexOnRemove(node);
	}
	

	public int getRemovedNodesCount() {
		return removedNodes.size();
	}
	
	public boolean isEmpty() {
		return firstRowNodes.isEmpty();
	}
	
//	protected boolean contains(MatrixNode node) {
//		
//	}
	

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
		removeNode(node);
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
	protected void removeRow(MatrixNode node) {
		assert node != null;
		removeNode(node);
		if (node != node.right)
			removeRow(node.right);
	}
	
	void removeRow(int rowIndex) {
		MatrixNode node = getRowFirstNode(rowIndex);
		if (node != null)
			removeRow(node);
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
//	void reInsertColumn(MatrixNode node) {
//		if (node != node.down)
//			reInsertColumn(node.down);
//		node.reInsert();
//		adaptRowFirstIndexOnInsert(node);
//	}
	
	private void undoRemove_(int toState) {
		int removeCount = getRemovedNodesCount();
		if (toState < 0 || toState > removeCount)
			throw new IllegalArgumentException("illegal removed nodes counter");
		// calculate the number of node which must be removed
		removeCount -= toState;
		// remove removeCount nodes
		for (int i = 0; i < removeCount; i++) {
			// get last removed node
			MatrixNode node = removedNodes.remove(removedNodes.size()-1); // remove last
			// reinsert node in matrix
			node.reInsert();
			adaptRowFirstIndexOnInsert(node);
		}
	}

	public void undoRemove(int toState) {
		undoRemove_(toState);
	}

	public void undoRemove(int from, int to) { //TODO surely this can be optimized later
		int removeCount = getRemovedNodesCount();
		if (from < 0 || from > to || to > removeCount)
			throw new IllegalArgumentException("illegal removed nodes counter");
		// calculate the number of node which must be removed
		removeCount -= to;
		removeCount = removedNodes.size()-removeCount;
		LinkedList<MatrixNode> reremove = new LinkedList<MatrixNode>();
		for (int i=removedNodes.size()-1; i >= removeCount; i--)
			reremove.add(removedNodes.get(i));
		undoRemove_(from);
		
		// remove removeCount nodes
		for (MatrixNode node : reremove)
			removeNode(node);
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
