package de.helwich.sudoku.client;

import static de.helwich.sudoku.client.BitSetUtil.*;

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
	private int countNull; // number of cells with a zero value
	private int countUnique; // number of cells with a value with cardinality one
	
	//TODO maybe optimize class eg by not needing type copy
	/**
	 * Create a sudoku playfield of the given type.
	 * 
	 * @param  type
	 */
	public Field(Type type) {
		this.type = type.copy(); // copy because type can change
		field = new int[this.type.getHeight()][this.type.getWidth()];
		countNull = type.getCellCount();
		countUnique = 0;
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
			// update countNull parameter
			if (bitset == 0) {
				if (field[row][column] != 0)
					countNull ++;
			} else if (field[row][column] == 0)
				countNull --;
			// update countUnique parameter
			if (cardinality(bitset) == 1) {
				if (cardinality(field[row][column]) != 1)
					countUnique ++;
			} else if (cardinality(field[row][column]) == 1)
				countUnique --;
			// store value
			field[row][column] = bitset;
			// notify change listeners
			if (changeHandlers != null)
				for (CellChangeHandler handler : changeHandlers)
					handler.onChange(row, column, bitset);
		}
	}
	
	/**
	 * Returns <code>true</code> if the operation {@link #getBitset(int, int)}
	 * will return a value with cardinality 1 for all valid field cell indices.
	 * 
	 * @return <code>true</code> if the operation {@link #getBitset(int, int)}
	 * will return a value with cardinality 1 for all valid field cell indices.
	 */
	public boolean isSolved() {
		return countUnique == type.getCellCount();
	}
	
	/**
	 * Returns <code>true</code> if the operation {@link #getBitset(int, int)}
	 * will return a zero value for at least one valid field cell index.
	 * 
	 * @return <code>true</code> if the operation {@link #getBitset(int, int)}
	 * will return a zero value for at least one valid field cell index.
	 */
	public boolean hasNull() {
		return countNull > 0;
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

	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < type.getWidth()*2+3; i++)
			sb.append("-");
		sb.append("\n");
		for (int i = 0; i < type.getHeight(); i++) {
			sb.append("|");
			for (int j = 0; j < type.getWidth(); j++) {
				if (type.hasCellIndex(i, j)) {
					int set = getBitset(i, j);
					if (cardinality(set) == 1)
						sb.append(" "+type.getFieldChars().charAt(nextSetBit(set, 0)));
					else if (set == 0)
						sb.append(" #");
					else
						sb.append(" .");
				} else
					sb.append("  ");
			}
			sb.append(" |\n");
		}
		for (int i = 0; i < type.getWidth()*2+3; i++)
			sb.append("-");
		return sb.toString();
	}
	
}
