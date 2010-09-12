package de.helwich.sudoku.solve;

/**
 * @author Hendrik Helwich
 *
 */
public class XorMatrixFactory {

	private final int height;
	
	private MatrixNode[] columnFirst; // used to create the matrix
	private MatrixNode[] columnLast;
	private int currentColumn;
	
	public XorMatrixFactory(int height) {
		this.height = height;
	}
	
	private void ensureInitialized() {
		if (columnFirst == null) {
			columnFirst = new MatrixNode[height];
			columnLast = new MatrixNode[height];
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
	
	private XorMatrix createXorMatrix_() {
		ensureInitialized();
		// connect first row elements with last row elements
		for (int i = 0; i < height; i++) {
			MatrixNode first = columnFirst[i];
			MatrixNode last = columnLast[i];
			if (first != null) {
				first.left = last;
				last.right = first;
			}
		}
		// create matrix
		return new XorMatrix(columnFirst);
	}
	
	public XorMatrix createXorMatrix() {
		XorMatrix matrix = createXorMatrix_();
		// free for garbage collector
		columnFirst = null;
		columnLast = null;
		return matrix;
	}

	@Override
	public String toString() {
		return createXorMatrix_().toString();
	}
	
}
