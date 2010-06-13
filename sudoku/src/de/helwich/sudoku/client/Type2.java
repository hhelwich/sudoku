package de.helwich.sudoku.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * immutable
 * 
 * @author Hendrik Helwich
 *
 */
public class Type2 {

	private final Cell[] cells;
	private final String chars;

	private final IGroup[] groups;
	
	private final int height;
	private final int width;
	                           
	public Type2(Group... groups) {
		List<Character> chars = new ArrayList<Character>();
		List<Cell> cells = new ArrayList<Cell>();
		int maxHeight = 0;
		int minHeight = Integer.MAX_VALUE;
		int maxWidth = 0;
		int minWidth = Integer.MAX_VALUE;
		for (Group group : groups) {
			for (Cell cell : group.getCells()) {
				int idx = Collections.binarySearch(cells, cell);
				if (idx < 0) { // not in list
					cells.add(-idx -1, cell);
					//
					if (cell.getRow() > maxHeight)
						maxHeight = cell.getRow();
					else if (cell.getRow() < minHeight)
						minHeight = cell.getRow();
					if (cell.getColumn() > maxWidth)
						maxWidth = cell.getColumn();
					else if (cell.getColumn() < minWidth)
						minWidth = cell.getColumn();
				}
			}
			String grpchrs = group.getChars();
			for (int i = grpchrs.length()-1; i >= 0; i--) {
				int idx = Collections.binarySearch(chars, grpchrs.charAt(i));
				if (idx < 0) // not in list
					chars.add(-idx -1, grpchrs.charAt(i));
			}
		}
		
		if (minHeight != 0 || minWidth != 0)
			throw new IllegalArgumentException("field must not have empty top left rows/columns");
		height = maxHeight + 1;
		width = maxWidth + 1;
		
		this.cells = cells.toArray(new Cell[cells.size()]);
		cells = null;
		char[] chrs = box(chars);
		chars = null;
		this.chars = new String(chrs);

		List<IGroup> grps = new ArrayList<IGroup>(groups.length);
		for (Group group : groups) {
			BitSet cellSubSet = new BitSet();
			for (Cell cell : group.getCells()) {
				int idx = Arrays.binarySearch(this.cells, cell);
				assert idx >= 0;
				cellSubSet.set(idx);
			}
			BitSet charSubSet = new BitSet();
			String grpchrs = group.getChars();
			for (int i = 0; i < grpchrs.length(); i++) {
				int idx = Arrays.binarySearch(chrs, grpchrs.charAt(i));
				assert idx >= 0;
				charSubSet.set(idx);
			}
			IGroup grp = new IGroup(cellSubSet, charSubSet);
			int idx = Collections.binarySearch(grps, grp, GROUP_COMPARATOR);
			if (idx < 0) // not found
				grps.add(-idx-1, grp);
		}
		this.groups = grps.toArray(new IGroup[grps.size()]);
	}
	
	private static final Comparator<IGroup> GROUP_COMPARATOR = new Comparator<IGroup>() {
		@Override
		public int compare(IGroup g1, IGroup g2) {
			int j, i = 0;
			do {
				j = g1.getCellSubSet().nextSetBit(i);
				i = g2.getCellSubSet().nextSetBit(i);
				if (j != i)
					return j - i;
			} while (i++ >= 0);
			if (!g1.getCharSubSet().equals(g2.getCharSubSet()))
				throw new IllegalArgumentException("groups with same cells but with different chars");
			return 0;
		}
	};
	
	private static char[] box(List<Character> chars) {
		char[] chrs = new char[chars.size()];
		for (int i = 0; i < chars.size(); i++)
			chrs[i] = chars.get(i);
		return chrs;
	}

	public int getCellCount() {
		return cells.length;
	}

	public int getCellIndex(Cell cell) {
		return Arrays.binarySearch(cells, cell);
	}
	
	public Cell getCell(int index) {
		return cells[index];
	}
	
	public Iterable<Cell> getCellIterator(final BitSet bs) {
		return new Iterable<Cell>() {
			@Override
			public Iterator<Cell> iterator() {
				return new Iterator<Cell>() {
					
					private int idx = bs.nextSetBit(0);
					
					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
					
					@Override
					public Cell next() {
						Cell cell = cells[idx];
						idx = bs.nextSetBit(idx + 1);
						return cell;
					}
					
					@Override
					public boolean hasNext() {
						return idx != -1;
					}
				};
			}
		};
	}
	
	public List<Cell> getCellIterator() {
		return Arrays.asList(cells);
	}

	public String getChars() {
		return chars;
	}

	public int getGroupCount() {
		return groups.length;
	}
	
	public BitSet getGroupCellUnion(BitSet groupIndices) {
		BitSet bs = new BitSet();
		for (int i = groupIndices.nextSetBit(0); i >= 0; i = groupIndices.nextSetBit(i + 1))
			bs.or(groups[i].getCellSubSet());
		return bs;
	}
	
	public BitSet getGroupCellUnion(int groupIndex) {
		BitSet bs = new BitSet();
		bs.or(groups[groupIndex].getCellSubSet());
		return bs;
	}
	
	public BitSet getGroupCellUnionRead(int groupIndex) {
		return groups[groupIndex].getCellSubSet();
	}
	
	/**
	 * Return the indices of all groups which contain the cells set which is
	 * related to the given by the cell indices.
	 * 
	 * @param  cellIndices
	 * @return
	 */
	public BitSet getCellGroups(BitSet cellIndices) {
		BitSet bs = new BitSet();
		BitSet tmp = new BitSet();
		for (int i = 0; i < groups.length; i++) {
			if (i > 0)
				tmp.clear();
			tmp.or(cellIndices);
			tmp.and(groups[i].getCellSubSet());
			if (tmp.equals(cellIndices))
				bs.set(i);
		}
		return bs;
	}
	
	public BitSet getCellGroups(int cellIndex) {
		BitSet bs = new BitSet();
		for (int i = 0; i < groups.length; i++)
			if (groups[i].getCellSubSet().get(cellIndex))
				bs.set(i);
		return bs;
	}
	
	public BitSet getGroupCharIntersection(BitSet groupIndices) {
		BitSet bs = new BitSet();
		int i = groupIndices.nextSetBit(0);
		if (i != -1) {
			bs.or(groups[i].getCharSubSet());
			for (i = groupIndices.nextSetBit(i + 1); i >= 0; i = groupIndices.nextSetBit(i + 1))
				bs.and(groups[i].getCharSubSet());
		}
		return bs;
	}
	
	/**
	 * Returns the width of the sudoku type.
	 * 
	 * @return The width of the sudoku type.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Returns the height of the sudoku type.
	 * 
	 * @return The height of the sudoku type.
	 */
	public int getHeight() {
		return height;
	}
	
	private static class IGroup {

		private final BitSet cellSubSet;
		private final BitSet charSubSet;
		
		public IGroup(BitSet cellSubSet, BitSet charSubSet) {
			if (cellSubSet.cardinality() != charSubSet.cardinality())
				throw new IllegalArgumentException();
			this.cellSubSet = cellSubSet;
			this.charSubSet = charSubSet;
		}

		public BitSet getCellSubSet() {
			return cellSubSet;
		}

		public BitSet getCharSubSet() {
			return charSubSet;
		}
		
	}

	public final static class Group { //TODO can be made editable later
		
		private final String chars;
		private final Cell[] cells;
		
		public Group(String chars, Cell... cells) {
			if (chars.length() != cells.length)
				throw new IllegalArgumentException("number of cells and characters must be equal");
			this.chars = chars;
			this.cells = cells;
		}

		public String getChars() {
			return chars;
		}

		public Cell[] getCells() {
			return cells;
		}
		
	}        
	
}
