package de.helwich.sudoku.client;

import java.util.LinkedList;
import java.util.List;

/**
 * An instance of a sudoku {@link Type}.
 * The cells of this sudoku playfield can hold a subset of the sudoku field
 * characters.
 * This class is used to be able to change the storage implementation later
 * and to add change handlers to be notified when the value of the field is
 * changed.
 * 
 * @author Hendrik Helwich
 */
public class Field {

	private final Type type;
	private int[][] field;
	private List<CellChangeHandler> changeHandlers;
	
	//TODO maybe optimize class eg by not needing type copy
	/**
	 * Create a sudoku playfield of the given type.
	 * 
	 * @param  type
	 */
	public Field(Type type) {
		this.type = type.copy(); // copy because type can change
		field = new int[this.type.getHeight()][this.type.getWidth()];
	}

	/**
	 * Set the given bitset to the field cell with the given index.
	 * 
	 * @param  row
	 * @param  column
	 * @param  bitset
	 * @throws IndexOutOfBoundsException
	 *         If the given cell index does not exists in the field type.
	 */
	public void setBitset(int row, int column, int bitset)
			throws IndexOutOfBoundsException {
		if (!type.hasCellIndex(row, column))
			throw new IndexOutOfBoundsException("cell index "+row+","+column
					+" does not exist");
		if (field[row][column] != bitset) {
			field[row][column] = bitset;
			if (changeHandlers != null)
				for (CellChangeHandler handler : changeHandlers)
					handler.onChange(row, column, bitset);
		}
	}
	
	/**
	 * Get the given bitset of the field cell with the given index.
	 * 
	 * @param  row
	 * @param  column
	 * @return
	 * @throws IndexOutOfBoundsException
	 *         If the given cell index does not exists in the field type.
	 */
	public int getBitset(int row, int column)
			throws IndexOutOfBoundsException {
		if (!type.hasCellIndex(row, column))
			throw new IndexOutOfBoundsException("cell index "+row+","+column
					+" does not exist");
		return field[row][column];
	}
	
	/**
	 * Add a handler to get notified if the field value has changed by calling
	 * the operation {@link #setBitset(int, int, int)}.
	 * 
	 * @param  changeHandler
	 */
	public void addChangeHandler(CellChangeHandler changeHandler) {
		if (changeHandlers == null)
			changeHandlers = new LinkedList<CellChangeHandler>();
		changeHandlers.add(changeHandler);
	}

	public Type getType() {
		return type;
	}
	
}
