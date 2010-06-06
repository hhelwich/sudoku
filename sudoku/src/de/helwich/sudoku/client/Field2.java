package de.helwich.sudoku.client;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

public class Field2 {

	private final Type2 type;
	private BitSet[] values;
	private List<CellChangeHandler> changeHandlers;
	private int countNull; // number of cells with a zero value
	private int countUnique; // number of cells with a value with cardinality one
	
	public Field2(Type2 type) {
		this.type = type;
		values = new BitSet[type.getCellCount()];
		for (int i = 0; i < values.length; i++)
			values[i] = new BitSet();
		countNull = type.getCellCount();
		countUnique = 0;
	}
	
	public void setValue(Cell cell, BitSet value) {
		setValue(type.getCellIndex(cell), value);
	}
	
	public void setValue(int idx, BitSet value) {
		if (!values[idx].equals(value)) {
			// update countNull parameter
			if (value.isEmpty())
				countNull ++;
			else if (values[idx].isEmpty())
				countNull --;
			// update countUnique parameter
			if (value.cardinality() == 1) {
				if (values[idx].cardinality() != 1)
					countUnique ++;
			} else if (values[idx].cardinality() == 1)
				countUnique --;
			// update stored value
			values[idx].clear();
			values[idx].or(value);
			// notify change handlers
			notifyChangeHandlers(type.getCell(idx));
		}
	}
	
	public BitSet getValue(Cell cell) {
		return getValue(type.getCellIndex(cell));
	}
	
	public BitSet getValue(int cellIndex) {
		return cloneBitSet(values[cellIndex]);
	}
	
	static final BitSet cloneBitSet(BitSet bs) {
		BitSet bs2 = new BitSet();
		bs2.or(bs);
		return bs2;
	}
	
	public boolean isSolved() {
		return countUnique == type.getCellCount();
	}
	
	public int getUniqueCellCount() {
		return countUnique;
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

	public Type2 getType() {
		return type;
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
	
	public void removeChangeHandler(CellChangeHandler changeHandler) {
		if (changeHandlers == null || !changeHandlers.remove(changeHandler))
			throw new IllegalArgumentException("can not remove handler");
	}
	
	/**
	 * Call the operation {@link CellChangeHandler#onChange(int, int, int)}
	 * with the given index and its value for all handlers which are registered
	 * to this field by the operation
	 * {@link #addChangeHandler(CellChangeHandler)}.
	 * 
	 * @param  row
	 * @param  column
	 */
	protected void notifyChangeHandlers(Cell cell) {
		if (changeHandlers != null)
			for (CellChangeHandler handler : changeHandlers)
				handler.onChange(cell);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int length = type.getChars().length();
		for (int i = 0; i < type.getWidth()*(length+1)+3; i++)
			sb.append("-");
		sb.append("\n");
		for (int i = 0; i < type.getHeight(); i++) {
			sb.append("|");
			for (int j = 0; j < type.getWidth(); j++) {
				if (type.getCellIndex(new Cell(i, j)) != -1) {
					BitSet set = getValue(new Cell(i, j));
					sb.append(' ');
					for (int k = 0; k < length; k++) {
						if (set.get(k))
							sb.append(type.getChars().charAt(k));
						else
							sb.append(' ');
					}
				}
			}
			sb.append(" |\n");
		}
		for (int i = 0; i < type.getWidth()*(length+1)+3; i++)
			sb.append("-");
		return sb.toString();
	}

	public void initializeField() {
		for (int i = type.getCellCount()-1; i >= 0; i--)
			setValue(i, getFullIndex(i));
	}
	
	private BitSet getFullIndex(int cell) {
		BitSet cbs = getSingleCellBitSet(cell);
		cbs = type.getCellGroups(cbs);
		return type.getGroupCharIntersection(cbs);
	}
	
	private BitSet getSingleCellBitSet(int idx) {
		BitSet bs = new BitSet();
		bs.set(idx);
		return bs;
	}
	
}
