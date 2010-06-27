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


	public void remove(Node node) {
		if (node.remove() && STORE_REMOVED_NODES)
			removedNodes.add(node);
	}
	
}
