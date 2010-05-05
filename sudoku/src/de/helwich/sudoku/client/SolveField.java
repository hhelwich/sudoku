package de.helwich.sudoku.client;

import static de.helwich.sudoku.client.BitSetUtil.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Hendrik Helwich
 */
public class SolveField extends Field {
	
	private Map<CellIndex, Integer> masksToApply = new LinkedHashMap<CellIndex, Integer>();
	private List<CellIndex> changedIndices = new ArrayList<CellIndex>();

	public SolveField(Type type) {
		super(type);
	}

	@Override
	public synchronized void setBitset(int row, int column, int newValue)
			throws IndexOutOfBoundsException {
		assert type.hasCellIndex(row, column);
		CellIndex index = new CellIndex(row, column);
		int oldValue = getBitset(row, column);
		while (true) {
			if (oldValue != newValue) {
				setValue(index.getRow(), index.getColumn(), newValue);
				storeChangedIndex(index);
				calculateEffect(index, newValue, oldValue);
			}
			// get next cell index and value to be updated or exit loop if finished
			Entry<CellIndex, Integer> index2 = getNextMaskIndex();
			if (index2 == null)
				break;
			index = index2.getKey();
			oldValue = getBitset(index.getRow(), index.getColumn());
			newValue = oldValue & index2.getValue();
		}
		notifyChangedIndices();
	}

	
	private List<CellIndex> effectIndices = new ArrayList<CellIndex>();
	private List<Integer> effectIndixesGroups = new ArrayList<Integer>();
	
	private List<Integer> indexSets = new ArrayList<Integer>();
	private List<Integer> indexGroups = new ArrayList<Integer>();
	private List<Integer> indexValues = new ArrayList<Integer>();
	
	private List<Integer> indexSets2 = new ArrayList<Integer>();
	private List<Integer> indexGroups2 = new ArrayList<Integer>();
	private List<Integer> indexValues2 = new ArrayList<Integer>();
	
	private void calculateEffect(CellIndex index, int newValue, int oldValue) {
		Iterable<CellGroup> groups = type.getCellGroups(index);
		if (newValue == 0) {
			for (CellGroup group : groups)
				for (CellIndex idx : group.getCellIndices())
					storeMask(idx, 0);
			return;
		} else if (cardinality(newValue) == 1) {
			for (CellGroup group : groups)
				for (CellIndex idx : group.getCellIndices())
					if (!idx.equals(index)) //TODO uses ==  ??
						storeMask(idx, ~newValue);
			return;
		}
		// calculate idcs map
		int groupIndex = 0;
		effectIndices.add(index);
		effectIndixesGroups.add(0);
		for (CellGroup group : groups) {
			CellIndex[] indices = group.getCellIndices();
			for (CellIndex idx : indices) {
//				if (!idx.equals(index)) {
					int v = set(0, groupIndex);
					int i = effectIndices.indexOf(idx);
					if (i != -1)
						effectIndixesGroups.set(i, effectIndixesGroups.get(i) | v);
					else {
						effectIndices.add(idx);
						effectIndixesGroups.add(v);
					}
				}
			groupIndex++;
		}

		indexSets.add(0);
		indexGroups.add(effectIndixesGroups.get(0));
		indexValues.add(oldValue);
		indexValues.add(newValue);

		int card = 1;
		
		while (! indexSets.isEmpty()) {
			for (int i = 0; i < indexGroups.size(); i++) {
				int lastIndex = i * card + card -1;
				oldValue = indexValues.get(i*2);
				newValue = indexValues.get(i*2+1);
				int gps = indexGroups.get(i);
				for (int k = indexSets.get(lastIndex)+1; k < effectIndices.size(); k++) {
					int gps2 = effectIndixesGroups.get(k) & gps;
					if (gps != 0) {
						CellIndex in = effectIndices.get(k);
						int nv = getBitset(in.getRow(), in.getColumn());
						int ov = nv | oldValue;
						nv |= newValue;
						if (ov != nv) {
							int c = cardinality(nv);
							if (c == card + 1) { // set is complete
								for (int m = 0; m < effectIndices.size(); m++) {
									int gp = effectIndixesGroups.get(m);
									if ((gp & gps2) != 0) {
										boolean bla = true;
										for (int l=i*card; l<=lastIndex; l++)
											if (m == k || indexSets.get(l) == m) {
												bla = false;
												break;
											}
										if (bla)
											storeMask(effectIndices.get(m), ~nv);
									}
								}
							} else if (c > card + 1) { // cardinality(nv) > card ???
								for (int n = i * card; n <= lastIndex; n++)
									indexSets2.add(indexSets.get(n));
								indexSets2.add(k);
								indexGroups2.add(gps2);
								indexValues2.add(ov);
								indexValues2.add(nv);
							} else { // c < card
								throw new RuntimeException("oops need to implement set 0");
							}
						}
					}
				}
				
				
			}

			card ++;
			
			// clear old lists
			indexSets.clear();
			indexGroups.clear();
			indexValues.clear();
			
			// swap lists
			List<Integer> tmp = indexSets;
			indexSets = indexSets2;
			indexSets2 = tmp;
			tmp = indexGroups;
			indexGroups = indexGroups2;
			indexGroups2 = tmp;
			tmp = indexValues;
			indexValues = indexValues2;
			indexValues2 = tmp;
		}
		
		// clear used global data structs
		effectIndices.clear();
		effectIndixesGroups.clear();
	}

	private void storeMask(CellIndex index, int mask) {
		Integer m = masksToApply.get(index);
		if (m != null)
			mask &= m;
		masksToApply.put(index, mask);
	}
	
	private Entry<CellIndex, Integer> getNextMaskIndex() {
		if (masksToApply.isEmpty())
			return null;
		Iterator<Entry<CellIndex, Integer>> it = masksToApply.entrySet().iterator();
		Entry<CellIndex, Integer> entry = it.next();
		it.remove();
		return entry;
	}
	
	private void storeChangedIndex(CellIndex index) {
		changedIndices.add(index);
	}
	
	private void notifyChangedIndices() {
		for (CellIndex index : changedIndices)
			notifyChangeHandlers(index.getRow(), index.getColumn());
		changedIndices.clear();
	}
	
	public String toStringFull() {
		StringBuilder sb = new StringBuilder();
		int length = type.getFieldChars().length();
		for (int i = 0; i < type.getWidth()*(length+1)+3; i++)
			sb.append("-");
		sb.append("\n");
		for (int i = 0; i < type.getHeight(); i++) {
			sb.append("|");
			for (int j = 0; j < type.getWidth(); j++) {
				if (type.hasCellIndex(i, j)) {
					int set = getBitset(i, j);
					sb.append(' ');
					for (int k = 0; k < length; k++) {
						if (get(set, k))
							sb.append(type.getFieldChars().charAt(k));
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
	
	@Override
	public String toString() {
		return toStringFull();
	}
	
	static boolean createSetCounter(List<Integer> counter, int cardSet, int cardCounter) {
		if (cardCounter > cardSet)
			return false;
		counter.clear();
		for (int i = 0; i < cardCounter; i ++)
			counter.add(i);
		return true;
	}
	
	static boolean incSetCounter(List<Integer> counter, int cardSet) {
		int cardCounter = counter.size();
		for (int i = cardCounter-1; i >= 0; i --) {
			int x = counter.get(i);
			if (x < --cardSet) {
				for (; i < cardCounter; i ++)
					counter.set(i, ++x);
				return true;
			}
		}
		return false;
	}

}
