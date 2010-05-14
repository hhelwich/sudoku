package de.helwich.sudoku.client;

/**
 * An interface which can be used to get notified on field value changes.
 * 
 * @see Field#addChangeHandler(CellChangeHandler)
 * 
 * @author Hendrik Helwich
 */
public interface CellChangeHandler {
		
	public void onChange(Cell index, int value);
	
}
