package de.helwich.sudoku.solve;

import java.util.ArrayList;
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
			removeNodeAndEffect(node);
			node = node.right;
		} 
		removeNodeAndEffect(node);
		return removedNodes.size();
	}
	
	private void removeNode(MatrixNode node) {
		if (node.remove())
			removedNodes.add(node);
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
	private void removeNodeAndEffect(MatrixNode node) {
		
		if (node.up != node && node.down == node.up) { // only one node is left in the column
			// remove all columns which are connected with the single node which
			removeNode(node);
			// is left in the current column
			node = node.up;//is 1
			while (node != node.right)
				removeColumnAndRows(node.right);
			return;
		}
		
		
		// add all remaining nodes of the column to a column list
		int col = node.column;
		List<MatrixNode> column = new ArrayList<MatrixNode>(firstColumn.length); //TODO get from pool
		MatrixNode currentNode = node; //TODO columns which have a node in this row can be ignored due to minimal constraint
		int maxcol = Integer.MIN_VALUE;
		while (currentNode.up != node) {
			currentNode = currentNode.up;
			MatrixNode first = firstColumn[currentNode.row];
			column.add(first);
			maxcol = Math.max(maxcol, first.column);
		}
		
		removeNode(node);
		
		//
		int height = column.size();
		if (height == 0)
			return;
		outerloop:
		while (true) {
			for (int i = 0; i < height; i++) {
				MatrixNode cnode = column.get(i);
				while (cnode.column < maxcol) {
					if (cnode == cnode.right || cnode.right.column < cnode.column)
						break outerloop;
					cnode = cnode.right;
					column.set(i, cnode);
				}
				if (cnode.column > maxcol) {
					maxcol = cnode.column;
					continue outerloop;
				}
			}
			if (maxcol != col)
				removeColumn(column.get(0));
			maxcol++;
		}
	}

	private void removeColumnAndRows(MatrixNode node) {
		while (node != node.up) {
			while (node.up != node.up.right)
				removeNode(node.up.right);
			removeNode(node.up);
		}
		removeNode(node);
	}

	private void removeColumn(MatrixNode node) {
		while (node != node.up)
			removeNode(node.up);
		removeNode(node);
		
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
