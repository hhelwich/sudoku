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
		if (node != null) {
			while (node.right != node) {
				removeNodeAndEffect(node);
				node = node.right;
			} 
			removeNodeAndEffect(node);
		}
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
		List<Integer> removeRowsLater = new ArrayList<Integer>(); //TODO get from pool
		
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
				removeColumn(column, removeRowsLater);
			maxcol++;
		}
		
		for (int i = removeRowsLater.size()-1; i >= 0; i--)
			removeRow(removeRowsLater.remove(i));
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
		if (node != node.up)
			removeRowsOfColumn(node.up, removeRowsLater);
	}

	
	/**
	 * Remove all rows of the column of the given node.
	 */
	private void removeRowsOfColumn(MatrixNode node, List<Integer> removeRowsLater) {
		for (removeRow(node, removeRowsLater); node != node.up; node = node.up)
			removeRow(node.up, removeRowsLater);
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
