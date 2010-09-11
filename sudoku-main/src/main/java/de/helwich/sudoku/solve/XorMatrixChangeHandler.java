package de.helwich.sudoku.solve;

/**
 * Interface that can be used to get notified if the state of an
 * {@link XorMatrix} has changed.
 * 
 * @see XorMatrix#addChangeHandler(XorMatrixChangeHandler)
 * @see XorMatrix#removeChangeHandler(XorMatrixChangeHandler)
 * 
 * @author Hendrik Helwich
 */
public interface XorMatrixChangeHandler {

	public void onRemoveRow(int row);
	
}
