package de.helwich.sudoku.client.solve;


/**
 * @author Hendrik Helwich
 */
class Node {

	public Node left, right, up, down;
	public final int columnIndex;
	
	public Node(int columnIndex) {
		this.columnIndex = columnIndex;
	}
	
	public Node first() {
		Node node = this;
		while (node.left != null)
			node = node.left;
		return node;
	}
	
	public boolean remove() {
		if (!isRemoved()) {
			if (up != null)
				up.down = down;
			if (down != null)
				down.up = up;
			if (left != null)
				left.right = right;
			if (right != null)
				right.left = left;
			return true;
			
//			else
//				clear(); // clear to be removed by garbage collector
		}
		return false;
	}
	
	public void reInsert() {
		assert isRemoved();
		if (up != null)
			up.down = this;
		if (down != null)
			down.up = this;
		if (left != null)
			left.right = this;
		if (right != null)
			right.left = this;
	}
	
//	private void clear() {
//		up = null;
//		down = null;
//		left = null;
//		right = null;
//	}
	
	public boolean isRemoved() {
		if (up != null)
			return up.down != this;
		if (down != null)
			return down.up != this;
		if (left != null)
			return left.right != this;
		if (right != null)
			return right.left != this;
		return true;
	}

	public Node maxUp() {
		return up == null ? this : up.maxUp();
	}

	public Node maxDown() {
		return down == null ? this : down.maxDown();
	}

	public Node maxLeft() {
		return left == null ? this : left.maxLeft();
	}

	public Node maxRight() {
		return right == null ? this : right.maxRight();
	}

	public int countDown() {
		return down == null ? 0 : down.countDown()+1;
	}

	public int countUp() {
		return up == null ? 0 : up.countUp()+1;
	}
	
}
