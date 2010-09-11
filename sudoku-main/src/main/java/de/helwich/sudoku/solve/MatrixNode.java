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
	
	public boolean remove() {
		if (isSingle() || isRemoved())
			return false;
		left.right = right;
		right.left = left;
		up.down = down;
		down.up = up;
		return true;
	}
	
	public boolean reInsert() {
		if (!isRemoved())
			return false;
		left.right = this;
		right.left = this;
		up.down = this;
		down.up = this;
		return true;
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
	 * Returns <code>true</code> if the node had adjacent nodes
	 * ({@link #isSingle()} returned <code>false</code>) and was removed before
	 * by {@link #remove()}.
	 * {@link #isRemoved()} <code>== true</code> implicates {@link #isSingle()}
	 * <code>== false</code> or in other words it is never possible that
	 * {@link #isRemoved()} and {@link #isSingle()} are both <code>true</code>.
	 * 
	 * @return <code>true</code> if the node had adjacent nodes and was removed
	 *         before by {@link #remove()}.
	 */
	public boolean isRemoved() {
		if (left.right != this) { // node removed before; node row count unknown
			assert right.left != this;
			assert (up.down == this) == (down.up == this);
			return true;
		} else if (up.down != this) { // node removed before and only node in row
			assert right.left == this;
			assert down.up != this;
			return true;
		} else { // not removed
			assert right.left == this;
			assert down.up == this;
			return false;
		}
	}
	
	/**
	 * Returns <code>true</code> if the node has no adjacent nodes.
	 * If <code>true</code> is returned, the operation {@link #isRemoved()}
	 * will return <code>false</code>.
	 * 
	 * @return <code>true</code> if the node has no adjacent nodes
	 */
	public boolean isSingle() {
		if (left == this) { // single in row
			assert right == this;
			if (up == this) { // single in row and column
				assert down == this;
				return true;
			} else // single in row but not in column
				assert down != this;
		} else { // not single in row
			assert (up == this) == (down == this);
			assert right != this;
		}
		return false;
	}
	
}
