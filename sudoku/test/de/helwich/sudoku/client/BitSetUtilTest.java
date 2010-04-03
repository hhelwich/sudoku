package de.helwich.sudoku.client;

import static de.helwich.sudoku.client.BitSetUtil.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hendrik Helwich
 */
public class BitSetUtilTest {

	int bitset;
	
	@Before
	public void setUp() throws Exception {
		bitset = 0; // empty set
		bitset = set(bitset, 3);
		bitset = set(bitset, 7);
		bitset = set(bitset, 2);
		bitset = set(bitset, MAX_INDEX);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNextSetBit() {
		int set2 = 0;
		for (int i = nextSetBit(bitset, 0); i >= 0; i = nextSetBit(bitset, i+1))
			set2 = set(set2, i);
		assertEquals(bitset, set2);
	}

	@Test
	public void testMaxIndex() {
		assertEquals(31, MAX_INDEX);
	}

	@Test
	public void testSetAndClearAndGet() {
		for (int i = 0; i <= MAX_INDEX; i ++)
			assertEquals(
					i == 2 || i == 3 || i == 7 || i == MAX_INDEX, 
					get(bitset, i));
		bitset = clear(bitset, 3);
		bitset = clear(bitset, 5);
		bitset = clear(bitset, MAX_INDEX);
		for (int i = 0; i <= MAX_INDEX; i ++)
			assertEquals(
					i == 2 || i == 7, 
					get(bitset, i));
	}

	@Test
	public void testGetPossiblePermutations() {
		int count = 6;
		int[] bitsets = new int[count];
		for (int i = 0; i < count; i ++)
			bitsets[0] = set(bitsets[0], i);
		for (int i = 1; i < count; i ++)
			bitsets[i] = bitsets[0];
		int[] newbs = getPossiblePermutations(bitsets);
		for (int i = 0; i < count; i ++)
			assertEquals(bitsets[0], newbs[i]);
	}

	@Test
	public void testGetPossiblePermutations2() {
		int[] bitsets = new int[3];
		bitsets[0] = set(bitsets[0], 1);
		bitsets[0] = set(bitsets[0], 3);
		bitsets[1] = set(bitsets[1], 1);
		bitsets[1] = set(bitsets[1], 2);
		bitsets[1] = set(bitsets[1], 3);
		bitsets[2] = bitsets[0];
		int[] newbs = getPossiblePermutations(bitsets);
		assertEquals(bitsets[0], newbs[0]);
		assertEquals(set(0, 2), newbs[1]);
		assertEquals(bitsets[0], newbs[2]);
	}

	@Test
	public void testCardinality() {
		assertEquals(4, cardinality(bitset));
	}

}
