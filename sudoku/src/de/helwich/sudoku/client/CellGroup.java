package de.helwich.sudoku.client;

import java.util.Arrays;

/**
 * An immutable set of sudoku playfield cells. Aggregated instances of this
 * class are hold in a sudoku {@link Type} instance which defines a sudoku
 * playfield.
 * This class also holds a subset of the characters of the parent sudoku
 * {@link Type}.
 * 
 * @see Type
 * 
 * @author Hendrik Helwich
 */
public class CellGroup {

	private int bitset;
	private CellIndex[] cellIndices;
	
	/**
	 * @param  bitset
	 *         Holds the indices of the character of the parent sudoku type
	 *         ({@link Type#getFieldChars()}) which can be inserted in the
	 *         cell indices which are also stored in this class. 
	 * @param  cellIndices
	 *         A strict monotonic list of cell indices of a parent sudoku
	 *         {@link Type}. The number of cell indices must be equal to the
	 *         cardinality of the bitset.
	 * @throws IllegalArgumentException
	 *         If the bitset or the cell indices are empty, if the cardinality
	 *         of the bitset and the cell indices are not equal, or if the
	 *         cell indices are not a strict monotonic.
	 */
	public CellGroup(int bitset, CellIndex... cellIndices)
			throws IllegalArgumentException {
		if (cellIndices.length == 0)
			throw new IllegalArgumentException("group must not be empty");
		if (BitSetUtil.cardinality(bitset) != cellIndices.length)
			throw new IllegalArgumentException("number of group chars must be equal to the number of group cells");
		if (! isSortedSet(cellIndices))
			throw new IllegalArgumentException("cell indices must be strict monotonic");
		this.bitset = bitset;
		this.cellIndices = cellIndices;
	}
	
	static boolean isSortedSet(CellIndex[] cellIndices) {
		CellIndex idx = null;
		for (CellIndex i : cellIndices)
			if (idx != null && i.compareTo(idx) <= 0)
				return false;
			else
				idx = i;
		return true;
	}

	public int getBitset() {
		return bitset;
	}
	
	public CellIndex[] getCellIndices() {
		return cellIndices;
	}
	
	public int getSize() {
		return cellIndices.length;
	}

	@Override
	public String toString() {
		return "["+ Arrays.toString(cellIndices) + "]";
	}

	@Override
	public int hashCode() {
		return 31 + Arrays.hashCode(cellIndices);
	}

	/**
	 * only instances which are used in the same {@link Type} can be compared.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CellGroup other = (CellGroup) obj;
		if (!Arrays.equals(cellIndices, other.cellIndices))
			return false;
		if (bitset != other.bitset)
			throw new IllegalArgumentException(
					"cell groups for the same type and with the same cell "
					+"indices must have the same bitset");
		return true;
	}
	
}
