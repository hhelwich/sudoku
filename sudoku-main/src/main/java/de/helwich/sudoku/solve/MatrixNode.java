package de.helwich.sudoku.solve;

import java.util.HashSet;
import java.util.Set;


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
	 * Initializes the immutable position of the matrix node.
	 * The adjacent pointers {@link #left}, {@link #right}, {@link #up} and
	 * {@link #down} need to be set to a non <code>null</code> value afterwards
	 * before any of the operations of this class are called.
	 * 
	 * @param  row
	 * @param  column
	 */
	public MatrixNode(int row, int column) {
		this.row = row;
		this.column = column;
	}
	
	/**
	 * Removes the node from the matrix. It is expected that the node is not
	 * already removed from the matrix.
	 * If the node is a single node, this operation has no effect and
	 * <code>false</code> will be returned.
	 * 
	 * @return <code>true</code> if the given node is not a single node.
	 */
	public boolean remove() {
		assert !isRemoved();
		assert checkValid(this);
		if (isSingle())
			return false;
		left.right = right;
		right.left = left;
		up.down = down;
		down.up = up;
		return true;
	}
	
	/**
	 * Reinserts the node from the matrix. It is expected that the node is 
	 * removed before from the matrix by the operation {@link #remove()} which
	 * has returned the value <code>true</code> which means that the node must
	 * not be a single node.
	 */
	public void reInsert() {
		assert isRemoved();
		left.right = this;
		right.left = this;
		up.down = this;
		down.up = this;
		assert checkValid(this);
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
		if (left == this) { // single in row
			assert right == this;
			if (up == this) { // single in row and column
				assert down == this;
				assert checkValid(this);
				return false; // is single node
			} else { // single in row but not in column => has neighbors
				assert down != this;
				if (up.down == this) { // is not removed
					assert down.up == this;
					assert checkValid(this);
					return false;
				} else { // is removed
					return true;
				}
			}
		}
		// has neighbors
		assert right != this;
		if (left.right == this) { // and is not removed
			assert right.left == this;
			assert up.down == this;
			assert down.up == this;
			assert checkValid(this);
			return false;
		} else { // is removed
			assert right.left != this;
			assert (up == this && down == this) ||
			       (up.down != this && down.up != this);
			return true;
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
			assert right != this;
			assert (up == this) == (down == this);
		}
		return false;
	}
	
	private static boolean checkValid(MatrixNode node) {
		return checkValid(node, new HashSet<MatrixNode>());
	}
	
	private static boolean checkValid(MatrixNode node, Set<MatrixNode> visitedNodes) {
		// return if node is visited before
		if (visitedNodes.contains(node))
			return true;
		// check if given node is valid and return false if not
		if (	node.left == null ||
				node.right == null ||
				node.up == null ||
				node.down == null)
			return false;
		if (	node.left.right != node ||
				node.right.left != node ||
				node.up.down    != node ||
				node.down.up    != node)
			return false;
		//TODO add more checks
		// mark node as visited
		visitedNodes.add(node);
		// return true if all linked nodes are valid
		return checkValid(node.left, visitedNodes) &&
			checkValid(node.right, visitedNodes) &&
			checkValid(node.up, visitedNodes) &&
			checkValid(node.down, visitedNodes);
	}

	@Override
	public String toString() {
		return "(" + row + "," + column + ")";
	}
	
}
