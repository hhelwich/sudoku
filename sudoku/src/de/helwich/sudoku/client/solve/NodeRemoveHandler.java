package de.helwich.sudoku.client.solve;

/**
 * Interface that can be used to get notified if a node in a matrix is removed.
 * 
 * @author Hendrik Helwich
 */
public interface NodeRemoveHandler {

	/**
	 * Notifies that a node has been removed.
	 * 
	 * @param  node The node which is removed.
	 */
	public void onRemove(Node node);
	
}
