package de.helwich.sudoku.solve;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Hendrik Helwich
 */
public class XorMatrix {

	private final MatrixNode[] firstColumn;
	private List<XorMatrixChangeHandler> handlers;
	private LinkedList<MatrixNode> removedNodes;
	
	/**
	 * Must only be called by {@link XorMatrixFactory}.
	 * 
	 * @param firstColumn
	 */
	XorMatrix(MatrixNode[] firstColumn) {
		this.firstColumn = firstColumn;
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
	
	public int removeRow(int row) {
		MatrixNode node = firstColumn[row];
		if (node != null) {
			do {
				removeNode(node);
				node = node.right;
			} while (node != null);
			firstColumn[row] = null;
		}
		return removedNodes.size();
	}
	
	private void removeNode(MatrixNode node) {
		if (node.remove())
			removedNodes.add(node);
		
		// if node is a single node we do not know if it is removed before
		// if node is an element of first column array => adapt array
		if (firstColumn[node.row] == node)
			firstColumn[node.row] = node.right;
	}
	
	public void restoreNodes(int matrixStateId) {
		// calculate the number of node which must be removed
		int removeCount = removedNodes.size() - matrixStateId;
		// remove removeCount nodes
		for (int i = 0; i < removeCount; i++) {
			// get last removed node
			MatrixNode node = removedNodes.removeLast();
			// reinsert node in matrix
			node.reInsert();
			if (firstColumn[node.row] == node.right)
				firstColumn[node.row] = node;
		}
	}
	
}
