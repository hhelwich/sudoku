package de.helwich.sudoku.solve;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Hendrik Helwich
 *
 */
public class XorMatrixFactory {


	/* array of the first node in each row  */
	private Map<Integer, MatrixNode> firstRowNodes; // used to create the matrix
	/* array of the first node in each column  */
	private Map<Integer, MatrixNode> firstColumnNodes; // used to create the matrix
	private Map<Integer, MatrixNode> lastRowNode;
	private int currentColumn;
	
	public XorMatrixFactory() {
	}
	
	private void ensureInitialized() {
		if (firstRowNodes == null) {
			firstRowNodes = new HashMap<Integer, MatrixNode>();
			firstColumnNodes = new HashMap<Integer, MatrixNode>();
			lastRowNode = new HashMap<Integer, MatrixNode>();
			currentColumn = 0;
		}
	}
	
	public void addXorColumn(int... rows) {
		ensureInitialized();
		MatrixNode up = null;
		for (int row : rows) {
			// create new node
			MatrixNode node = new MatrixNode(row, currentColumn);
			// connect with the previous node in the current row
			if (up != null) { // not first iteration step
				up.down = node;
				node.up = up;
			} else // first iteration step
				firstColumnNodes.put(currentColumn, node);
			up = node;
			// connect with the previous node in the current column
			if (firstRowNodes.get(row) == null) { // row is empty
				firstRowNodes.put(row, node);
				lastRowNode.put(row, node);
			} else {
				lastRowNode.get(row).right = node;
				node.left = lastRowNode.get(row);
				lastRowNode.put(row, node);
			}
			
		}
		// connect top and bottom element of the column
		MatrixNode top = lastRowNode.get(rows[0]);
		MatrixNode bottom = lastRowNode.get(rows[rows.length-1]);
		top.up = bottom;
		bottom.down = top;
		// step column counter
		currentColumn++;
	}
	
	public XorMatrix createXorMatrix() {
		ensureInitialized();
		// connect first row elements with last row elements
		for (MatrixNode first : firstRowNodes.values()) {
			MatrixNode last = lastRowNode.get(first.row);
			first.left = last;
			last.right = first;
		}
		// create matrix
		XorMatrix matrix = new XorMatrix(firstRowNodes, firstColumnNodes);
		// free for garbage collector
		firstRowNodes = null;
		firstColumnNodes = null;
		lastRowNode = null;
		return matrix;
	}

	@Override
	public String toString() {
		ensureInitialized();
		return new XorMatrix(firstRowNodes, firstColumnNodes).toString();
	}
	
}
