package de.helwich.sudoku.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
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
	                           
	public Type2(Group... groups) {
		List<Character> chars = new ArrayList<Character>();
		List<Cell> cells = new ArrayList<Cell>();
		this.groups = new IGroup[groups.length];
		int groupIndex = 0;
		for (Group group : groups) {
			BitSet cellSubSet = new BitSet();
			for (Cell cell : group.getCells()) {
				int idx = Collections.binarySearch(cells, cell);
				if (idx < 0) { // not in list
					idx = -idx -1;
					cells.add(idx, cell);
					// shift all group cell bitsets right at position idx
					for (int i = 0; i < groupIndex; i++)
						shiftRight(this.groups[i].getCellSubSet(), idx);
				}
				cellSubSet.set(idx);
			}
			BitSet charSubSet = new BitSet();
			String grpchrs = group.getChars();
			for (int i = 0; i < grpchrs.length(); i++) {
				int idx = Collections.binarySearch(chars, grpchrs.charAt(i));
				if (idx < 0) { // not in list
					idx = -idx -1;
					chars.add(idx, grpchrs.charAt(i));
					// shift all group cell bitsets right at position idx
					for (int j = 0; j < groupIndex; j++)
						shiftRight(this.groups[j].getCharSubSet(), idx);
				}
				charSubSet.set(idx);
			}
			this.groups[groupIndex] = new IGroup(cellSubSet, charSubSet);
			groupIndex++;
		}
		
		this.cells = cells.toArray(new Cell[cells.size()]);
		//TODO sort groups
//		Arrays.sort(this.groups, new Comparator<IGroup>() {
//			@Override
//			public int compare(IGroup g1, IGroup g2) {
//				return 0;
//			}
//		});
		this.chars = box(chars);
	}
	
	private static String box(List<Character> chars) {
		char[] string = new char[chars.size()];
		for (int i = 0; i < chars.size(); i++)
			string[i] = chars.get(i);
		return new String(string);
	}
	
	private static final void shiftRight(BitSet bs, int pos) {
		for (int i = bs.length()-1; i >= pos; i--)
			if (bs.get(i)) {
				bs.clear(i);
				bs.set(i+1);
			}
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
	
	public Iterator<Cell> getCellIterator(final BitSet bs) {
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

	public String getChars() {
		return chars;
	}

	public int getGroupCount() {
		return groups.length;
	}
	
	public BitSet getGroupCells(BitSet groupIndices) {
		BitSet bs = new BitSet();
		for (int i = groupIndices.nextSetBit(0); i >= 0; i = groupIndices.nextSetBit(i + 1))
			bs.or(groups[i].getCellSubSet());
		return bs;
	}
	
//	public BitSet getGroupCells(BitSet groupIndices) {
//		BitSet bs = new BitSet();
//		int i = groupIndices.nextSetBit(0);
//		if (i != -1) {
//			bs.or(groups[i].getCellSubSet());
//			for (i = groupIndices.nextSetBit(i + 1); i >= 0; i = groupIndices.nextSetBit(i + 1))
//				bs.and(groups[i].getCellSubSet());
//		}
//		return bs;
//	}
	
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
