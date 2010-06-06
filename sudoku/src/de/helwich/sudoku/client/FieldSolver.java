package de.helwich.sudoku.client;

import static de.helwich.sudoku.client.Field2.cloneBitSet;

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
	private final Type2 type;
	private Map<Integer, BitSet> updates = new HashMap<Integer, BitSet>();
	private Map<Integer, BitSet> updatesNext = new HashMap<Integer, BitSet>();

	public FieldSolver(Field2 field) {
		this.field = field;
		type = field.getType();
		createMap();
		field.addChangeHandler(this);
	}



	private void addUpdatesNext(int index, BitSet value, boolean mask) throws NotSolvableException {
		BitSet valueOld = getUpdatesNextValue(index);
		BitSet valueOldClone = cloneBitSet(valueOld);
		if (mask)
			valueOldClone.andNot(value);
		else
			valueOldClone.and(value);
		if (!valueOld.equals(valueOldClone))
			if (valueOldClone.isEmpty())
				throw new NotSolvableException();
			else
				updatesNext.put(index, valueOldClone);
	}
	
	private void commitUpdates() {
		// commit updatedValues to field
		for (Entry<Integer, BitSet> entry : updates.entrySet())
			setFieldValue(entry.getKey(), entry.getValue());
		updates.clear();
		// switch updatedValues and updatedValuesNext
		Map<Integer, BitSet> tmp = updates;
		updates = updatesNext;
		updatesNext = tmp;
	}

	public void detach() {
		field.removeChangeHandler(this);
	}
	
	private boolean expectingChange = false; 

	public synchronized void setValue(Cell index, BitSet value) throws NotSolvableException {
		assert !expectingChange;
		addUpdatesNext(type.getCellIndex(index), value, false);
		commitUpdates();
		while (! updates.isEmpty()) {
			calculateUpdatesNext();
			commitUpdates();
		}
	}
	
	private void calculateUpdatesNext() throws NotSolvableException {
		Map<Integer, Map<BitSet, BitSet>> map =
			new HashMap<Integer, Map<BitSet,BitSet>>();
		
		for (Entry<Integer, BitSet> entry : updates.entrySet()) {
			int cellIndex = entry.getKey();
			BitSet value = entry.getValue();
			int card = value.cardinality();
			BitSet bs = type.getCellGroups(cellIndex);
			if (card == 1) {
				bs = type.getGroupCellUnion(bs);
				bs.clear(cellIndex);
				for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1))
					addUpdatesNext(i, value, true);
			} else {
				for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
					Map<BitSet, BitSet> m = map.get(i);
					BitSet cells = null;
					if (m == null) {
						m = new HashMap<BitSet, BitSet>();
						map.put(i, m);
					} else {
						cells = m.get(value);
					}
					if (cells == null) {
						cells = new BitSet();
						m.put(cloneBitSet(value), cells);
					}
					cells.set(cellIndex);
				}
			}
		}
		
		int cardCells = 1;
		
		Map<Integer, Map<BitSet, BitSet>>
			map2 = new HashMap<Integer, Map<BitSet,BitSet>>(),
			map3 = null;
		
		while (!map.isEmpty()) {
			
			// map : card of values > card of cells

			for (Entry<Integer, Map<BitSet, BitSet>> ent : map.entrySet()) {
				int grp = ent.getKey();
				Map<BitSet, BitSet> map_ = ent.getValue();

				for (Entry<BitSet, BitSet> entry : map_.entrySet()) {
					BitSet values = entry.getKey();
					BitSet cells = entry.getValue();
					
					int cardC = cells.cardinality();
					int cardV = values.cardinality();
					
					BitSet cls = type.getGroupCellUnion(grp);
					
					cls.andNot(cells);
					
					if (cardC == cardV) {
						for (int i = cls.nextSetBit(0); i >= 0; i = cls.nextSetBit(i + 1))
							addUpdatesNext(i, values, true);
					} else {
						assert cardV > cardC;
					
						
						for (int i = cls.nextSetBit(0); i >= 0; i = cls.nextSetBit(i + 1)) {
							BitSet bs = cloneBitSet(getUpdatesValue(i));
							bs.or(values);
							Map<BitSet, BitSet> mp = map2.get(grp);
							if (mp == null) {
								mp = new HashMap<BitSet, BitSet>();
								map2.put(grp, mp);
							}
							BitSet c = mp.get(bs);
							if (c == null) {
								c = new BitSet();
								mp.put(bs, c);
							}
							c.set(i);
							c.or(cells);
						}
					}
					
				}
			}
			
			cardCells ++;
			
			map.clear();
			map3 = map;
			map = map2;
			map2 = map3;
		}
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
	
	private void setFieldValue(int cell, BitSet value) {
		assert !expectingChange;
		expectingChange = true;
		field.setValue(cell, value);
	}
	
	private BitSet getFieldValue(int cellIndex) {
		return field.getValue(cellIndex);
	}
	
	private BitSet getUpdatesValue(Integer index) {
		BitSet value = updates.get(index);
		return value == null ? getFieldValue(index) : value;
	}
	
	private BitSet getUpdatesNextValue(Integer index) {
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
