package de.helwich.sudoku.client;

import static de.helwich.sudoku.client.Type.*;
import static de.helwich.sudoku.client.BitSetUtil.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hendrik Helwich
 */
public class TypeTest {
	
	private Type emptyType, type, type2;
	
	@Before
	public void setUp() throws Exception {
		emptyType = new Type();
		type = new Type();
		type.setFieldChars("12345");
		type.addCellGroup(
				new CellGroup(set(0, 0), 
						new CellIndex(1, 1)));
		type2 = new Type();
		type2.setFieldChars("12345");
		type2.addCellGroup(
				new CellGroup(2|4|8|16, 
						new CellIndex(0, 0), new CellIndex(2, 3),
						new CellIndex(4, 6), new CellIndex(6, 9)));
		type2.addCellGroup(
				new CellGroup(2|4|1|16, 
						new CellIndex(0, 0), new CellIndex(2, 2),
						new CellIndex(4, 4), new CellIndex(6, 6)));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAddCellGroup() {
		assertEquals(2, type2.getCellGroupCount());
		type2.addCellGroup(new CellGroup(1, new CellIndex(7, 10)));
		assertEquals(3, type2.getCellGroupCount());
		assertEquals(11, type2.getWidth());
		assertEquals(8, type2.getHeight());
	}

	@Test
	public void testIsStrictMonotonic() {
		assertTrue(isStrictMonotonic(""));
		assertTrue(isStrictMonotonic(" "));
		assertTrue(isStrictMonotonic("0123456789"));
		assertTrue(isStrictMonotonic("0123456789ABCabcdefghijklmnopqrstuvwxyz"));
		assertFalse(isStrictMonotonic("00"));
		assertFalse(isStrictMonotonic("10"));
	}
	
	@Test
	public void testRemoveGroup() {
		assertEquals(10, type2.getWidth());
		assertEquals(7, type2.getHeight());
		for (int i = 0; i < type2.getHeight(); i ++)
			for (int j = 0; j < type2.getWidth(); j ++)
				assertEquals(i+","+j, i % 2 == 0 && (j == i || j == (i/2)*3), 
						type2.hasCellIndex(i, j));
		type2.removeGroup(0);
		assertEquals(7, type2.getWidth());
		assertEquals(7, type2.getHeight());
		for (int i = 0; i < type2.getHeight(); i ++)
			for (int j = 0; j < type2.getWidth(); j ++)
				assertEquals(i+","+j, i % 2 == 0 && j == i, 
						type2.hasCellIndex(i, j));
	}

	@Test
	public void testSetCellGroup() {
		assertEquals(10, type2.getWidth());
		assertEquals(7, type2.getHeight());
		assertTrue(type2.isTopLeftAligned());
		type2.setCellGroup(0, new CellGroup(1, new CellIndex(2, 3)));
		type2.removeGroup(1);
		assertEquals(1, type2.getWidth());
		assertEquals(1, type2.getHeight());
		assertFalse(type2.isTopLeftAligned());
	}

	@Test
	public void testGetCellGroup() {
	}

	@Test
	public void testGetCellGroupCount() {
	}

	@Test
	public void testHasCellIndex() {
	}

	@Test
	public void testGetCellGroups() {
	}

	@Test
	public void testGetFieldChars() {
	}

	@Test
	public void testSetFieldChars() {
		type.setFieldChars("a");
		type.setFieldChars("abc");
	}

	@Test
	public void testGetWidth() {
		assertEquals(0, emptyType.getWidth());
		assertEquals(1, type.getWidth());
		assertEquals(10, type2.getWidth());
	}

	@Test
	public void testGetHeight() {
		assertEquals(0, emptyType.getHeight());
		assertEquals(1, type.getHeight());
		assertEquals(7, type2.getHeight());
	}

	@Test
	public void testIsTopLeftAligned() {
		assertTrue(emptyType.isTopLeftAligned());
		assertFalse(type.isTopLeftAligned());
		assertTrue(type2.isTopLeftAligned());
	}

	@Test
	public void testIsEmpty() {
		assertTrue(emptyType.isEmpty());
		assertFalse(type.isEmpty());
		assertFalse(type2.isEmpty());
	}

}
