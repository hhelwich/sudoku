package de.helwich.sudoku.client;

import java.util.BitSet;

/**
 * @author Hendrik Helwich
 */
public class FieldSolveMatrix {

	int cellCount;
	int valueCount;
	int groupCount;
	Node[] nodes;
	
	public FieldSolveMatrix(Type2 type) {
		cellCount = type.getCellCount();
		valueCount = type.getChars().length();
		groupCount = type.getGroupCount();
		nodes = new Node[cellCount];
		int hIndex = 0;
		for (int v = 0; v < valueCount; v++) {
			for (int g = 0; g < groupCount; g++) {
				Node up = null;
				BitSet cells = type.getGroupCellUnionRead(g);
				for (int c = cells.nextSetBit(0); c >= 0; c = cells.nextSetBit(c+1)) {
					Node node = new Node(hIndex);
					// set horizontal cell pointers
					if (nodes[c] != null) {
						nodes[c].right = node;
						node.left = nodes[c];
					}
					nodes[c] = node;
					// set vertical group pointers
					if (up != null) {
						up.down = node;
						node.up = up;
					}
					up = node;
				}
				hIndex ++;
			}
		}
		for (int c = 0; c < cellCount; c++)
			nodes[c] = nodes[c].first();
	}
	
}

class Node {
	
	public Node left, right, up, down;
	public final int hIndex;
	
	public Node(int hIndex) {
		this.hIndex = hIndex;
	}
	
	public Node first() {
		Node node = this;
		while (node.left != null)
			node = node.left;
		return node;
	}
	
	public void remove() {
		if (up != null)
			up.down = down;
		if (down != null)
			down.up = up;
		if (left != null)
			left.right = right;
		if (right != null)
			right.left = left;
	}
	
	public void reInsert() {
		if (up != null)
			up.down = this;
		if (down != null)
			down.up = this;
		if (left != null)
			left.right = this;
		if (right != null)
			right.left = this;
	}
	
	public void clear() {
		up = null;
		down = null;
		left = null;
		right = null;
	}
	
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
	
}