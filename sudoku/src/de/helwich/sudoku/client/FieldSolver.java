package de.helwich.sudoku.client;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Hendrik Helwich
 *
 */
public class FieldSolver implements CellChangeHandler {
	
	private final Field2 field;
	private Map<Cell, BitSet> updates = new HashMap<Cell, BitSet>();
	private Map<Cell, BitSet> updatesNext = new HashMap<Cell, BitSet>();

	public FieldSolver(Field2 field) {
		this.field = field;
		createMap();
		field.addChangeHandler(this);
	}

	private void addUpdatesNextMask(Cell index, BitSet mask) {
		BitSet value = getUpdatesNextValue(index);
		mask.and(value);
		if (!mask.equals(value))
			updatesNext.put(index, mask);
	}
	
	private void commitUpdates() {
		// commit updatedValues to field
		for (Entry<Cell, BitSet> entry : updates.entrySet())
			setFieldValue(entry.getKey(), entry.getValue());
		updates.clear();
		// switch updatedValues and updatedValuesNext
		Map<Cell, BitSet> tmp = updates;
		updates = updatesNext;
		updatesNext = tmp;
	}
	
	public void initializeField() {
		for (Cell index : field.getType().getCellIterator())
			setFieldValue(index, getFullIndex(index));
	}

	public void detach() {
		field.removeChangeHandler(this);
	}
	
	private BitSet getFullIndex(Cell cell) {
		BitSet cbs = getSingleCellBitSet(cell);
		cbs = field.getType().getCellGroups(cbs);
		return field.getType().getGroupCharIntersection(cbs);
	}
	
	private BitSet getSingleCellBitSet(Cell cell) {
		int idx = field.getType().getCellIndex(cell);
		BitSet bs = new BitSet();
		bs.set(idx);
		return bs;
	}
	
	private boolean expectingChange = false; 

	public synchronized void setValue(Cell index, BitSet value) {
		assert !expectingChange;
		addUpdatesNextMask(index, value);
		commitUpdates();
		while (! updates.isEmpty()) {
			calculateUpdatesNext();
			commitUpdates();
		}
	}
	
	private void calculateUpdatesNext() {
		// TODO implement
	}
	
	private Map<Integer, Map<BitSet, List<AGroups>>> map;
	
	private void createMap() {
		map = new HashMap<Integer, Map<BitSet,List<AGroups>>>();
		int gc = field.getType().getGroupCount();
		for (int i = 0; i < gc; i++)
			for (int j = i+1; j < gc; j++) {
				BitSet G = field.getType().getGroupCellUnion(i);
				BitSet H = field.getType().getGroupCellUnion(j);
				// calculate intersection
				BitSet GiH = new BitSet();
				GiH.or(G);
				GiH.and(H);
				if (GiH.cardinality() >= 2) { // intersection holds more than one cell
					G.andNot(GiH); // G := G \ H
					H.andNot(GiH); // H := H \ G
					int cGmH = G.cardinality();
					int cHmG = H.cardinality();
					if (cGmH > 0 && cHmG > 0) {
						bls(cGmH, G, H, GiH);
						bls(cHmG, H, G, GiH);
					}
				}
			}
	}
	
	
	private void bls(int cGmH, BitSet G, BitSet H, BitSet GiH) {
		Map<BitSet, List<AGroups>> map2 = map.get(cGmH);
		if (map2 == null) {
			map2 = new HashMap<BitSet, List<AGroups>>();
			map.put(cGmH, map2);
		}
		List<AGroups> AG = map2.get(G);
		if (AG == null) {
			AG = new ArrayList<AGroups>(1);
			map2.put(G, AG);
		}
		AG.add(new AGroups(GiH, H));
	}

	public class AGroups {

		private final BitSet intersectionCells;
		private final BitSet applyCells;
		
		public AGroups(BitSet intersectionCells, BitSet applyCells) {
			this.intersectionCells = intersectionCells;
			this.applyCells = applyCells;
		}

		public BitSet getIntersectionCells() {
			return intersectionCells;
		}

		public BitSet getApplyCells() {
			return applyCells;
		}
		
	}
	
	private void setFieldValue(Cell cell, BitSet value) {
		assert !expectingChange;
		expectingChange = true;
		field.setValue(cell, value);
	}
	
	private BitSet getFieldValue(Cell cell) {
		return field.getValue(cell);
	}
	
	private BitSet getUpdatesValue(Cell index) {
		BitSet value = updates.get(index);
		return value == null ? getFieldValue(index) : value;
	}
	
	private BitSet getUpdatesNextValue(Cell index) {
		BitSet value = updatesNext.get(index);
		return value == null ? getUpdatesValue(index) : value;
	}
	
	@Override
	public void onChange(Cell index) {
		if (expectingChange)
			expectingChange = false;
		else
			throw new ConcurrentModificationException("do not write to field directly if it is used in solver");
	}

	public Field2 getField() {
		return field;
	}
	
}
