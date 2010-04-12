package de.helwich.sudoku.client;


import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hendrik Helwich
 */
public class CellIndexTest {

	private CellIndex idx0, idx1, idx1_b, idx2, idx3;
	
	@Before
	public void setUp() throws Exception {
		idx0 = new CellIndex(0, 0);
		idx1 = new CellIndex(1, 1);
		idx1_b = new CellIndex(1, 1);
		idx2 = new CellIndex(1, 3);
		idx3 = new CellIndex(2, 2);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testAll() {
		assertEquals(1, idx2.getRow());
		assertEquals(3, idx2.getColumn());
		assertEquals(0, idx0.getRow());
		assertEquals(0, idx0.getColumn());
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
		assertTrue(idx1.compareTo(idx2) < 0);
		assertTrue(idx2.compareTo(idx3) < 0);
		assertTrue(idx1.compareTo(idx3) < 0);
		assertTrue(idx1.compareTo(idx1_b) == 0);
	}
	
	@Test
	public void testEquals() {
		assertTrue(idx1.equals(idx1_b));
		assertTrue(idx1_b.equals(idx1));
		assertFalse(idx1.equals(idx2));
		assertFalse(idx2.equals(idx1));
		assertFalse(idx1.equals(idx3));
		assertFalse(idx3.equals(idx1));
		assertFalse(idx2.equals(idx3));
		assertFalse(idx3.equals(idx2));
	}

}
