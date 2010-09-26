package de.helwich.sudoku.solve;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	private void notifyChangeHandler(int row) {
		if (handlers != null)
			for (XorMatrixChangeHandler handler : handlers)
				handler.onRemoveRow(row);
	}
	
	public int removeRow(int row) {
		MatrixNode node = firstRowNodes.get(row);
		if (node != null) {
			List<Integer> removeRowsLater = new ArrayList<Integer>(); //TODO get from pool
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
			for (int i = removeRowsLater.size()-1; i >= 0; i--)
				removeRow(removeRowsLater.remove(i));
		}
		return removedNodes.size();
	}
	
	private void removeNode(MatrixNode node) {
		if (node.remove())
			removedNodes.add(node);
		// if node is a single node we do not know if it is removed before
		// if node is an element of first column array => adapt array
		if (firstRowNodes.get(node.row) == node)
			if (node.right == node) { // single node in the row
				firstRowNodes.remove(node.row);
				notifyChangeHandler(node.row);
			} else
				firstRowNodes.put(node.row, node.right);
	}
	
	/**
	 * @param  node
	 *         node which is removed before
	 */
	private void removeNodeAndEffect(MatrixNode node, List<Integer> removeRowsLater) {
		
		// create a list which holds the first row nodes of all the remaining
		// rows in the current column
		int col = node.column;
		List<MatrixNode> column = new ArrayList<MatrixNode>(firstRowNodes.size()); //TODO get from pool
		MatrixNode currentNode = node; //TODO columns which have a node in this row can be ignored due to minimal constraint

		do {
			currentNode = currentNode.up;
			if (currentNode == node)
				return; //empty column
		} while (currentNode.isRemoved());
		

		MatrixNode node2 = currentNode; // first not removed node in the column
		do {
			column.add(currentNode);
			currentNode = currentNode.up;
		} while (node2 != currentNode);

		
		// if the removed node was the last node in the column => return
		int height = column.size();
		if (height == 0)
			return;
		
		// special case for 6x3, 7x3 matrices
		if (height == 2 || height == 3) {
			Set<Integer> ignoreRows = new HashSet<Integer>(); //TODO from pool?
			MatrixNode n1 = null, n2 = null;
			for (MatrixNode cn : column) {
				if (getRowWidth(cn, 2) == 2) {
					if (n1 == null)
						n1 = cn;
					else if (n1.right.column != cn.right.column)
						n2 = cn;
				} else {
					if (getRowWidth(cn, 3) == 3)
						ignoreRows.add(cn.row);
					else {
						n2 = null;
						break;
					}
				}	
			}
			if (n2 != null) {
				n1 = n1.right;
				n2 = n2.right;
				n1 = firstColumnNodes.get(n1.column);
				n2 = firstColumnNodes.get(n2.column);
				Set<Integer> rows1 = new HashSet<Integer>();
				while(true) {
					rows1.add(n1.row);
					if (n1.down.row <= n1.row)
						break;
					n1 = n1.down;
				}
				boolean ret = false;
				while(true) {
					if (rows1.contains(n2.row) && !ignoreRows.contains(n2.row)) {
						removeRowsLater.add(n2.row);
						ret = true;
					}
					if (n2.down.row <= n2.row)
						break;
					n2 = n2.down;
				}
				if (ret)
					return;
			}
		}
		
		// special case for 6x4 matrices
		if (height == 2) {
			MatrixNode n1 = column.get(0);
			MatrixNode n2 = column.get(1);
			if (getRowWidth(n1, 2) == 2 &&
					getRowWidth(n2, 2) == 2 &&
					n1.right.column != n2.right.column) {
				n1 = n1.right;
				n2 = n2.right;
				boolean testok = false;
				if (isColumnHeightEqual(n1, 3))
					testok = true;
				else if (isColumnHeightEqual(n2, 3)) {
					// swap nodes
					MatrixNode tn = n1;
					n1 = n2;
					n2 = tn;
					testok = true;
				}
				if (testok) { // column of n1 hs height 3 
					if (isColumnHeightEqual(n2, 2)) {
						n2 = n2.up;
						if (getRowWidth(n2, 2) == 2) {
							n2 = n2.right;
							if (isColumnHeightEqual(n2, 2)) {
								n2 = n2.up;
								if (getRowWidth(n2, 2) == 2) {
									n2 = n2.right;
									if (n2.column == n1.column) {
										n2 = n2.up;
										if (n1 == n2)
											n2 = n2.up;
										// n2 is now the != n1 and != n2
										removeRowsLater.add(n2.row);
										return;
									}
								}
							}
						}
					}
				}
			}
		}
		

		List<MatrixNode> column2 = new ArrayList<MatrixNode>(firstRowNodes.size()); //TODO get from pool
		int maxcol = Integer.MIN_VALUE;
		int mincol = Integer.MAX_VALUE;
		for (MatrixNode n : column) {
			
			MatrixNode first = firstRowNodes.get(n.row);
			column2.add(first);
			maxcol = Math.max(maxcol, first.column);
			mincol = Math.min(mincol, first.column);
		}
		column = column2;
		
		
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
			if (ccol != col) // found column is not the column of the removed node
				removeColumn(column, removeRowsLater);
			// skip to next column
			ccol++;
		}
		
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
			if (firstRowNodes.get(node.row) == node.right)
				firstRowNodes.put(node.row, node);
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
