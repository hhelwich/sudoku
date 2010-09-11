package de.helwich.sudoku.solve;


/**
 * Represents an element of a spare boolean matrix.
 * The matrix position of the element ({@link #row}, {@link #column}) and its
 * adjacent elements are stored inside the element.
 * The adjacent pointers ({@link #left}, {@link #right}, {@link #up} and
 * {@link #down}) must not be <code>null</code>.
 * The elements position should be reasonable related to the adjacent pointer
 * positions (if they are different objects):
 * <ul>
 *   <li>left.column &lt; column &lt; right.column</li>
 *   <li>up.row &lt; row &lt; down.row</li>
 * </ul>
 * 
 * @author Hendrik Helwich
 */
public class MatrixNode {
	
	/** 
	 * An adjacent pointer which must not be <code>null</code>.
	 */
	public MatrixNode left, right, up, down;
	
	/**
	 * Immutable matrix position
	 */
	public final int row, column;
	
	/**
	 * @param  row
	 * @param  column
	 */
	public MatrixNode(int row, int column) {
		this.row = row;
		this.column = column;
	}
	
	public void remove() {
		up.down = down;
		left.right = right;
		down.up = up;
		right.left = left;
	}
	
	public void reInsert() {
		up.down = this;
		down.up = this;
		left.right = this;
		right.left = this;
	}
	
	public void setAdjacents(MatrixNode left, MatrixNode right,
			MatrixNode up, MatrixNode down) {
		left.right = this;
		right.left = this;
		up.down = this;
		down.up = this;
		this.left = left;
		this.right = right;
		this.up = up;
		this.down = down;
	}
	
	/**
	 * Returns <code>true</code> if the current node is the leftmost node in
	 * its row.
	 * 
	 * @return <code>true</code> if the current node is the leftmost node in
	 *         its row
	 */
	public boolean isLeftmost() {
		return left.column >= column;
	}

	public boolean isRowLoner() {
		return left == this;
	}
	
}
