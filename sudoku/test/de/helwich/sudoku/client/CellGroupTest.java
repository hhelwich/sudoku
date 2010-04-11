package de.helwich.sudoku.client;

import static de.helwich.sudoku.client.CellGroup.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hendrik Helwich
 */
public class CellGroupTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCellGroup() {
		new CellGroup(0);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCellGroup2() {
		new CellGroup(1);
	}

	@Test
	public void testCellGroup3() {
		new CellGroup(1, new CellIndex(1, 1));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCellGroup4() {
		new CellGroup(3, new CellIndex(1, 1), new CellIndex(1, 1));
	}

	@Test
	public void testIsSortedSet() {
		assertTrue(isSortedSet(new CellIndex[] {}));
		assertTrue(isSortedSet(new CellIndex[] {
				new CellIndex(1, 2)
		}));
		assertTrue(isSortedSet(new CellIndex[] {
				new CellIndex(1, 2), new CellIndex(1, 3), new CellIndex(2, 0)
		}));
		assertFalse(isSortedSet(new CellIndex[] {
				new CellIndex(1, 2), new CellIndex(1, 2)
		}));
		assertFalse(isSortedSet(new CellIndex[] {
				new CellIndex(1, 2), new CellIndex(1, 1)
		}));
	}

	@Test
	public void testGetBitset() {
	}

	@Test
	public void testGetCellIndices() {
	}

}
