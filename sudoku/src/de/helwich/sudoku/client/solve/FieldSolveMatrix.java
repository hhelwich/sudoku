package de.helwich.sudoku.client.solve;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import de.helwich.sudoku.client.Type2;

/**
 * @author Hendrik Helwich
 */
public class FieldSolveMatrix {

	private int cellCount;
	private int valueCount;
	private int groupCount;
	private Node[] nodes;
	
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
	
	public void restoreNodes() {
		if (!STORE_REMOVED_NODES)
			throw new RuntimeException();
		while (!removedNodes.isEmpty()) {
			Node lastRemoved = removedNodes.remove(removedNodes.size()-1);
			lastRemoved.reInsert();
		}
	}

	private boolean STORE_REMOVED_NODES = true; 
	private List<Node> removedNodes = STORE_REMOVED_NODES ? new ArrayList<Node>() : null;

	public class Node {
		
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
			if (!isRemoved()) {
				if (up != null)
					up.down = down;
				if (down != null)
					down.up = up;
				if (left != null)
					left.right = right;
				if (right != null)
					right.left = left;
				if (STORE_REMOVED_NODES)
					removedNodes.add(this);
//				else
//					clear(); // clear to be removed by garbage collector
			}
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
		
//		private void clear() {
//			up = null;
//			down = null;
//			left = null;
//			right = null;
//		}
		
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
	
}
