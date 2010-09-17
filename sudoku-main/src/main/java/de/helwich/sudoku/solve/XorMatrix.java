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
	
	private void notifyChangeHandler(int row) {
		if (handlers != null)
			for (XorMatrixChangeHandler handler : handlers)
				handler.onRemoveRow(row);
	}
	
	public int removeRow(int row) {
		MatrixNode node = firstColumn[row];
		while (node.right != node) {
			removeNode(node, true);
			node = node.right;
		} 
		removeNode(node, true);
		return removedNodes.size();
	}
	
	private void removeNode(MatrixNode node, boolean calculateEffect) {
		if (node.remove()) {
			removedNodes.add(node);
			if (calculateEffect)
				calculateEffect(node);
		}
		
		// if node is a single node we do not know if it is removed before
		// if node is an element of first column array => adapt array
		if (firstColumn[node.row] == node)
			if (node.right == node) { // single node in the row
				firstColumn[node.row] = null;
				notifyChangeHandler(node.row);
			} else
				firstColumn[node.row] = node.right;
	}
	
	/**
	 * @param  node
	 *         node which is removed before
	 */
	private void calculateEffect(MatrixNode node) {
		if (node.up != node && node.down == node.up) { // only one node is left in the column
			// remove all columns which are connected with the single node which
			// is left in the current column
			node = node.up;
			while (node != node.right)
				removeColumn(node.right);
		}
	}

	private void removeColumn(MatrixNode node) {
		while (node != node.up)
			removeNode(node.up, false);
		removeNode(node, false);
		
	}

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
			if (firstColumn[node.row] == node.right)
				firstColumn[node.row] = node;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int row = 0;
		for (MatrixNode first : firstColumn) {
			sb.append(row).append("  |");
			int column = 0;
			for (MatrixNode node : new MatrixNodeRowIterable(first)) {
				for (; column < node.column; column++)
					sb.append("  ");
				sb.append(" X");
				column++;
			}
			sb.append('\n');
			row++;
		}
		return sb.toString();
	}
	
}
