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

	private static final boolean debug = false;
	
	@Override
	public synchronized void setBitset(int row, int column, int newValue)
			throws IndexOutOfBoundsException {
		assert type.hasCellIndex(row, column);
		CellIndex index = new CellIndex(row, column);
		int oldValue = getBitset(row, column);
		newValue &= oldValue;
		if (debug)
			System.out.println("start");
		while (true) {
			if (debug)
				System.out.print(index);
			if (oldValue != newValue) {
				if (debug)
					System.out.println(" changed "+oldValue+" "+newValue);
				calculateEffect(index, newValue, oldValue);
				setValue(index.getRow(), index.getColumn(), newValue);
				storeChangedIndex(index);
			} else
				if (debug)
					System.out.println(" no change "+newValue);
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
	private List<Integer> effectIndicesGroups = new ArrayList<Integer>();
	
	private List<Integer> indexSets = new ArrayList<Integer>();
	private List<Integer> indexGroups = new ArrayList<Integer>();
	private List<Integer> indexValues = new ArrayList<Integer>();
	
	private List<Integer> indexSets2 = new ArrayList<Integer>();
	private List<Integer> indexGroups2 = new ArrayList<Integer>();
	private List<Integer> indexValues2 = new ArrayList<Integer>();
	
	private void calculateEffect(CellIndex index, int newValue, int oldValue) {
		// get groups which contain the given index
		Iterable<CellGroup> groups = type.getCellGroups(index);
		
		if (newValue == 0) {
			// value set is empty => all group indices must be empty too
			for (CellGroup group : groups)
				for (CellIndex idx : group.getCellIndices())
					storeMask(idx, 0);
			return;
		} else if (cardinality(newValue) == 1) {
			// value set does contain only one value => remove value from all
			// other indices values
			for (CellGroup group : groups)
				for (CellIndex idx : group.getCellIndices())
					if (!idx.equals(index)) //TODO use "==" later
						storeMask(idx, ~newValue);
			return;
		}
		
		calculateIndexGroups(index);

		indexSets.add(0);
		indexGroups.add(effectIndicesGroups.get(0));
		indexValues.add(oldValue);
		indexValues.add(newValue);

		int card = 1;
		
		while (! indexSets.isEmpty()) {
			if (debug)
				System.out.println("build sets with cardinality "+(card +1));
			// sets with cardinality card do exist => build sets with
			// cardinality card + 1
			for (int i = 0; i < indexGroups.size(); i++) { // iterate over all sets with cardinality card
				int lastIndex = i * card + card -1; // index of last (and highest) element of set i
				// get old and new values of set with index i 
				oldValue = indexValues.get(i*2);
				newValue = indexValues.get(i*2+1);
				// get groups of set with index i
				int gps = indexGroups.get(i);
				// iterate over all elements which should be united with set i
				// (all cells grouped with the given cell and higher than the
				// highest element of set i).
				for (int k = indexSets.get(lastIndex)+1; k < effectIndices.size(); k++) {
					// calculate groups of new union
					int ngps = effectIndicesGroups.get(k) & gps;
					if (ngps != 0) { // groups not empty => set could be relevant
						// get cell index of new element which should be united with the set i
						CellIndex in = effectIndices.get(k);
						// get old and new values of the set i unified with the new element
						int nv = getBitset(in.getRow(), in.getColumn());
						int ov = nv | oldValue;
						    nv = nv | newValue;
						// check if the value of the set has changed
						if (ov != nv) {
							// calculate cardinality of new set
							int c = cardinality(nv);
							// is the number of values of the new set equal
							// to the number of cells which build this set?
							// if true, remove this values from all other cell values
							if (c == card + 1) { // new set is complete
								// iterate over all cells
								
								cellit:
								for (int m = 0; m < effectIndices.size(); m++) {
									// get groups which contain this cell
									if (m != k && 
											((ngps & effectIndicesGroups.get(m)) != 0)) {
										// apply mask later to all cells which
										// are grouped with the init cell and
										// which are not the init cell and are
										// not included in set i
										
										// iterate over all cells of the set i
										for (int l = i*card; l <= lastIndex; l++)
											// 
											if (indexSets.get(l) == m)
												continue cellit;
										storeMask(effectIndices.get(m), ~nv);
									}
								}
							} else if (c > card + 1) {
								if (debug)
									System.out.print("create set ");
								for (int n = i * card; n <= lastIndex; n++) {
									indexSets2.add(indexSets.get(n));
									if (debug)
										System.out.print(effectIndices.get(indexSets.get(n))+", ");
								}
								indexSets2.add(k);
								if (debug)
									System.out.println(effectIndices.get(k));
								indexGroups2.add(ngps);
								indexValues2.add(ov);
								indexValues2.add(nv);
							} else // c < card //TODO remove later if algorithm is final
								throw new RuntimeException("should not be possible");
						}
					}
				}
				
				
			}

			card ++;
			
			prepareLists();
		}
		
		// clear used global data structs
		effectIndices.clear();
		effectIndicesGroups.clear();
	}
	
	private void calculateIndexGroups(CellIndex index) {
		Iterable<CellGroup> groups = type.getCellGroups(index);
		// store indices which are grouped with the given index and calculate
		// in which groups they are
		int groupIndex = 0; // an index for the groups
		// add given index to the index list. index groups is now empty but will
		// be filled below
		effectIndices.add(index);
		effectIndicesGroups.add(0);
		for (CellGroup group : groups) {
			CellIndex[] indices = group.getCellIndices();
			for (CellIndex idx : indices) {
				int v = set(0, groupIndex);
				int i = effectIndices.indexOf(idx); // search index in index list
				if (i != -1) // index is in the list => add group id to group id set
					effectIndicesGroups.set(i, effectIndicesGroups.get(i) | v);
				else { // index is not in the list => add
					effectIndices.add(idx);
					effectIndicesGroups.add(v);
				}
			}
			groupIndex++;
		}
	}

	private void prepareLists() {
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
