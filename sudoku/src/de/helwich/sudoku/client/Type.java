package de.helwich.sudoku.client;

import static de.helwich.sudoku.client.BitSetUtil.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An editable sudoku playfield type. The type is defined by cells which are
 * arranged inside a grid, by a set of characters, so that each cell can hold
 * one of this characters, and a list of cell groups for which a subset of the
 * given characters can be stored in.
 * 
 * The type is editable so it can be used in a sudoku type construction gui
 * later. The dimension of the sudoku type is given by its height and width. If
 * a cell in this field exists can be checked by the operation
 * {@link #hasCellIndex(int, int)}.
 * 
 * @author Hendrik Helwich
 */
public class Type {
	//TODO check constraint: max 32 groups per index (caused by implementation of solveField)

	/**
	 * all possible field chars. No duplicates allowed; must be in ascending
	 * order; maximum length of 32
	 */
	private String fieldChars;

	/** This list can be read an changed via getter an setter operations */
	private List<CellGroup> groups = new LinkedList<CellGroup>();

	/**
	 * An inverse index of the {@link #groups} parameter for better performance.
	 * It can be used if all groups which are holding a given cell index need to
	 * be find quickly (e.g. {@link #getCellGroups(int, int)}). This parameter
	 * must be adapted if the parameter {@link #groups} is changed.
	 */
	private Map<CellIndex, Set<CellGroup>> cellGroups = new HashMap<CellIndex, Set<CellGroup>>();

	/** -1 if {@link #groups} and {@link #cellGroups} have changed */
	private int height;
	private int width;
	private boolean topLeftAligned;
	private int maxBitsetIndex;

	/**
	 * Create an empty sudoku type.
	 */
	public Type() {
		resetDimension();
	}

	/**
	 * Add an immutable group of cell indices.
	 * 
	 * @param  cellGroup
	 * @throws IllegalArgumentException
	 *         If the fieldChars are set by the operation
	 *         {@link #setFieldChars(String)} before and the highest index
	 *         of the group bitset is not lower than the length of this string.
	 */
	public void addCellGroup(CellGroup cellGroup)
			throws IllegalArgumentException {
		checkGroupCharacterBitset(cellGroup.getBitset());
		addGroupToIndexMap(cellGroup);
		groups.add(cellGroup);
		resetDimension();
	}

	/**
	 * Replace the group of cell indices with the given index and the given new
	 * cell index group.
	 * 
	 * @param  index
	 * @param  group
	 * @throws IllegalArgumentException
	 *         If the fieldChars are set by the operation
	 *         {@link #setFieldChars(String)} before and the highest index
	 *         of the group bitset is not lower than the length of this string.
	 */
	public void setCellGroup(int index, CellGroup group)
			throws IllegalArgumentException {
		checkGroupCharacterBitset(groups.get(index).getBitset());
		removeGroupFromIndexMap(index);
		addGroupToIndexMap(group);
		groups.set(index, group);
		resetDimension();
	}

	/**
	 * Remove the group of cell indices with the given index.
	 * 
	 * @param  index
	 */
	public void removeGroup(int index) {
		removeGroupFromIndexMap(index);
		groups.remove(index);
		resetDimension();
	}

	/**
	 * Returns the group of cell indices with the given index.
	 * 
	 * @param  index
	 * @return
	 * @throws IndexOutOfBoundsException
	 *         if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt;= getCellGroupCount()</tt>)
	 */
	public CellGroup getCellGroup(int index) throws IndexOutOfBoundsException {//TODO remove
		return groups.get(index);
	}

	/**
	 * Returns the number of groups of cell indices.
	 * 
	 * @return
	 */
	public int getCellGroupCount() { //TODO remove
		return groups.size();
	}
	
	public Iterable<CellGroup> getCellGroups() { // TODO disable remove operation
		return groups;
	}
	
	/**
	 * @param  bitset
	 * @throws IllegalArgumentException
	 *         If the highest bitset index is not lower then the length of
	 *         the {@link #fieldChars} parameter.
	 */
	private void checkGroupCharacterBitset(int bitset)
			throws IllegalArgumentException {
		if (fieldChars != null && nextSetBit(bitset, 0) >= fieldChars.length())
			throw new IllegalArgumentException("");
	}

	/**
	 * Adapt parameter {@link #cellGroups} if the specified element is added to
	 * parameter {@link #groups}.
	 * 
	 * @param  group
	 */
	private void addGroupToIndexMap(CellGroup group) {
		for (CellIndex idx : group.getCellIndices()) {
			Set<CellGroup> cgps = cellGroups.get(idx);
			if (cgps == null) { // new cell index => create empty group set
				cgps = new HashSet<CellGroup>();
				cellGroups.put(idx, cgps);
			}
			// add new group to cell index group set
			cgps.add(group);
		}
	}

	/**
	 * Adapt parameter {@link #cellGroups} if the element with the given index
	 * is removed from the list parameter {@link #groups}.
	 * 
	 * @param  index
	 */
	private void removeGroupFromIndexMap(int index) {
		CellGroup group = groups.get(index);
		for (CellIndex idx : group.getCellIndices()) {
			// remove group from the groups index group set
			Set<CellGroup> gps = cellGroups.get(idx);
			if (!gps.remove(group))
				throw new RuntimeException("this should not happen");
			// if index group set is empty remove index key
			if (gps.isEmpty())
				cellGroups.remove(idx);
		}
	}

	/**
	 * Returns <code>true</code> if the cell with the given row and column
	 * exists in this type.
	 * 
	 * @param  row
	 * @param  column
	 * @return
	 */
	public boolean hasCellIndex(int row, int column) {
		return hasCellIndex(new CellIndex(row, column));
	}

	public boolean hasCellIndex(CellIndex index) {
		return cellGroups.keySet().contains(index);
	}

	/**
	 * Returns the set of all cell index groups which contain the given cell
	 * index.
	 * 
	 * @param  row
	 * @param  column
	 * @return
	 */
	public Iterable<CellGroup> getCellGroups(int row, int column) { //TODO remove?
		return getCellGroups(new CellIndex(row, column));
	}
	
	public Set<CellGroup> getCellGroups(CellIndex index) {
		return cellGroups.get(index);
	}

	/**
	 * Returns the set of character which can be stored in a cell of this type.
	 * 
	 * @return
	 */
	public String getFieldChars() {
		return fieldChars;
	}

	/**
	 * Sets the set of character which can be stored in a cell of this type.
	 * 
	 * @param  fieldChars
	 * @throws IllegalArgumentException
	 *         If the characters are not in strict monotonic order or if the
	 *         length of this character list is not greater than the maximum
	 *         bitset index which is used by the groups of this type.
	 */
	public void setFieldChars(String fieldChars)
			throws IllegalArgumentException {
		if (!isStrictMonotonic(fieldChars))
			throw new IllegalArgumentException(
					"chars must be in ascending order: "+fieldChars);
		if (getMaxBitsetIndex() >= fieldChars.length())
			throw new IllegalArgumentException("need at least "
					+ (getMaxBitsetIndex() + 1) + " characters");
		this.fieldChars = fieldChars;
	}

	/**
	 * Returns <code>true</code> if the given string holds a strict monotonic
	 * list of characters.
	 * 
	 * @param  s
	 * @return
	 */
	static boolean isStrictMonotonic(String s) {
		char c = Character.MIN_VALUE, c2;
		for (int i = 0; i < s.length(); i++) {
			c2 = s.charAt(i);
			if (c >= c2)
				return false;
			c = c2;
		}
		return true;
	}

	/**
	 * Returns the width of the sudoku type.
	 * 
	 * @return The width of the sudoku type.
	 */
	public int getWidth() {
		calculateDimensions();
		return width;
	}

	/**
	 * Returns the height of the sudoku type.
	 * 
	 * @return The height of the sudoku type.
	 */
	public int getHeight() {
		calculateDimensions();
		return height;
	}

	/**
	 * Returns the maximum character set index which is stored in a group of
	 * this type. It is assured that this value is lower then the length of the
	 * {@link #fieldChars} parameter (if this parameter is not <code>null</code>
	 * ) but it should be equal to the length minus one.
	 * 
	 * @return
	 */
	private int getMaxBitsetIndex() {
		calculateDimensions();
		return maxBitsetIndex;
	}

	/**
	 * Returns <code>true</code> if the first row and the first column of this
	 * type contain at least one cell.
	 * 
	 * @return
	 */
	public boolean isTopLeftAligned() {
		calculateDimensions();
		return topLeftAligned;
	}

	/**
	 * Returns <code>true</code> if the type is empty.
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return cellGroups.keySet().isEmpty();
	}
	
	public int getCellCount() {
		return cellGroups.size();
	}
	
	public Set<CellIndex> getCellIndices() {
		return cellGroups.keySet();
	}

	/**
	 * Create a normalized form of this type. Remove needless information which
	 * is hold by this type. Empty rows on the top and at the left will be
	 * removed, so that operation {@link #isTopLeftAligned()} will return
	 * <code>true</code>. All characters of the String which is returned by
	 * {@link #getFieldChars()} which are not used by the groups of this type
	 * will be removed. The groups of this type will be reordered.
	 */
	public void pack() {
		// TODO make normalized form (see following todos)
		// TODO remove inner 0s from bitset union (and apply to fieldchars)
		// TODO make top left aligned
		// TODO sort groups
	}

	/**
	 * Return a copy of this type. This can be useful if an static instance of
	 * this class is needed.
	 * 
	 * @return
	 */
	public Type copy() {
		Type type = new Type();
		for (CellGroup group : groups)
			type.addCellGroup(group);
		type.setFieldChars(getFieldChars());
		return type;
	}

	/**
	 * Has to be called if {@link #groups} and {@link #cellGroups} have changed,
	 * to be able to recalculate affected parameters
	 */
	private void resetDimension() {
		height = -1;
	}

	/**
	 * If the parameters {@link #groups} and {@link #cellGroups} have been
	 * changed, all properties which depend on them will be recalculated.
	 */
	private void calculateDimensions() {
		if (height != -1) // abort if parameters have not been changed
			return;
		if (isEmpty()) { // type is empty
			height = 0;
			width = 0;
			topLeftAligned = true;
			maxBitsetIndex = -1;
		} else { // type is not empty
			int minRow = Integer.MAX_VALUE;
			int maxRow = -1;
			int minColumn = Integer.MAX_VALUE;
			int maxColumn = -1;
			for (CellIndex index : cellGroups.keySet()) {
				if (index.getRow() < minRow)
					minRow = index.getRow();
				if (index.getRow() > maxRow)
					maxRow = index.getRow();
				if (index.getColumn() < minColumn)
					minColumn = index.getColumn();
				if (index.getColumn() > maxColumn)
					maxColumn = index.getColumn();
			}
			maxBitsetIndex = -1;
			for (CellGroup group : groups) {
				int mi = nextSetBit(group.getBitset(), 0);
				if (mi > maxBitsetIndex)
					maxBitsetIndex = mi;
			}
			height = maxRow - minRow + 1;
			width = maxColumn - minColumn + 1;
			topLeftAligned = minRow == 0 && minColumn == 0;
		}
	}

}
