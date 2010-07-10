package de.helwich.sudoku.client.solve;

import java.util.HashMap;
import java.util.Map;

/**
 * A node array object which also can quickly replace array objects without
 * knowing the nodes array index.
 * This can be done by an internal index structure so that it is not necessary
 * to iterate over the array.
 * 
 * It is assured that all elements in the array are not equal to each other.
 * Otherwise the {@link #replace(Node, Node)} operation will not work correctly.
 * 
 * It is assured at the moment, that the {@link #set(int, Node)} operation is
 * only used to (re)fill the complete array. Otherwise the
 * {@link #replace(Node, Node)} operation might be slow.
 * 
 * @author Hendrik Helwich
 */
public class NodeArray {
	
	private final Node[] array;
	private final Map<Node, Integer> map;
	
	private boolean calledSet = false;
	
	/**
	 * Create a node array with a fixed size.
	 * 
	 * @param  size
	 */
	public NodeArray(int size) {
		array = new Node[size];
		map = new HashMap<Node, Integer>(size);
	}
	
	/**
	 * Set the array element with the given index to the given node value.
	 * 
	 * @param  idx
	 * @param  node
	 */
	public void set(int idx, Node node) {
		array[idx] = node;
		calledSet = true;
	}
	
	/**
	 * Return the array element with the given index.
	 * 
	 * @param  idx
	 * @return
	 */
	public Node get(int idx) {
		return array[idx];
	}
	
	/**
	 * Replace the given array element with the given new value.
	 * 
	 * @param  old
	 * @param  new_
	 */
	public void replace(Node old, Node new_) {
		// completely rebuild the map if the set operation has been called
		if (calledSet) {
			if (!map.isEmpty())
				map.clear();
			for (int i = array.length-1; i >= 0; i--)
				map.put(array[i], i);
			calledSet = false;
		}
		// get the array index of the old node
		Integer idx = map.remove(old);
		assert idx != null;
		// add the new node to the data structures
		map.put(new_, idx);
		array[idx] = new_;
	}
	
}
