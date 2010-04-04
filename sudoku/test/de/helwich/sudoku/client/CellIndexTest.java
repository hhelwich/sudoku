package de.helwich.sudoku.client;


import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hendrik Helwich
 */
public class CellIndexTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testAll() {
		CellIndex index = new CellIndex(1, 2);
		assertEquals(1, index.getRow());
		assertEquals(2, index.getColumn());
		index = new CellIndex(0, 0);
		assertEquals(0, index.getRow());
		assertEquals(0, index.getColumn());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testNegativeRow() {
		new CellIndex(-1, 2);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testNegativeColumn() {
		new CellIndex(2, -1);
	}
	
	@Test
	public void testCompareTo() {
		CellIndex idx1 = new CellIndex(1, 1);
		CellIndex idx2 = new CellIndex(1, 3);
		CellIndex idx3 = new CellIndex(2, 2);
		assertTrue(idx1.compareTo(idx2) < 0);
		assertTrue(idx2.compareTo(idx3) < 0);
		assertTrue(idx1.compareTo(idx3) < 0);
	}

}
