package de.helwich.sudoku.solve;

/**
 * @author Hendrik Helwich
 *
 */
public class XorMatrixFactory {

	private boolean matrixCreated = false;

	private MatrixNode[] columnFirst;
	private MatrixNode[] columnLast; // used in initialization
	private int currentColumn = 0; // used in initialization
	
	public XorMatrixFactory(int height) {
		columnFirst = new MatrixNode[height];
		columnLast = new MatrixNode[height];
	}
	
	public void addXorColumn(int... rows) {
		if (matrixCreated)
			throw new RuntimeException("matrix is already created");
		MatrixNode up = null;
		for (int row : rows) {
			// create new node
			MatrixNode node = new MatrixNode(row, currentColumn);
			// connect with the previous node in the current row
			if (up != null) { // not first iteration step
				up.down = node;
				node.up = up;
			}
			up = node;
			// connect with the previous node in the current column
			if (columnFirst[row] == null) { // row is empty
				columnFirst[row] = node;
				columnLast[row] = node;
			} else {
				columnLast[row].right = node;
				node.left = columnLast[row];
				columnLast[row] = node;
			}
			
		}
		// connect top and bottom element of the column
		MatrixNode top = columnLast[rows[0]];
		MatrixNode bottom = columnLast[rows[rows.length-1]];
		top.up = bottom;
		bottom.down = top;
		// step column counter
		currentColumn++;
	}
	
	public XorMatrix createXorMatrix() {
		if (matrixCreated)
			throw new RuntimeException("matrix is already created");
		XorMatrix matrix = new XorMatrix(columnFirst);
		matrixCreated = true;
		// free for garbage collector
		columnFirst = null;
		columnLast = null;
		return matrix;
	}
	
}
