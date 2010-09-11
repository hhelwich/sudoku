package de.helwich.sudoku.solve;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Hendrik Helwich
 */
public class MatrixNodeTest {

	MatrixNode min;
	MatrixNode plus;
	MatrixNode pl, pr, pu, pd;
	
	@Before
	public void setUp() throws Exception {
		// create minimal matrix
		min = cerateLonerMatrix(2, 3);
		// create small matrix
		plus = new MatrixNode(2, 3);
		pl = cerateLonerMatrix(2, 1);
		pr = cerateLonerMatrix(2, 7);
		pu = cerateLonerMatrix(0, 3);
		pd = cerateLonerMatrix(4, 3);
		plus.setAdjacents(pl, pr, pu, pd);
		pl.setAdjacents(pr, plus, pl, pl);
		pu.setAdjacents(pu, pu, pd, plus);
	}
	
	private static MatrixNode cerateLonerMatrix(int row, int column) {
		MatrixNode node = new MatrixNode(row, column);
		node.setAdjacents(node, node, node, node);
		return node;
	}

	@Test
	public void testMatrixNode() {
		assertEquals(2, min.row);
		assertEquals(3, min.column);
	}

	@Test
	public void testRemove() {
		// nothing should happen
		min.remove();
		assertEquals(min, min.left);
		assertEquals(min, min.right);
		assertEquals(min, min.up);
		assertEquals(min, min.down);
		// 
		plus.remove();
		assertEquals(pl, plus.left);
		assertEquals(pr, plus.right);
		assertEquals(pu, plus.up);
		assertEquals(pd, plus.down);
		assertEquals(pr, pl.right);
		assertEquals(pl, pr.left);
		assertEquals(pd, pu.down);
		assertEquals(pu, pd.up);
		
	}

	@Test
	public void testReInsert() {
		// nothing should happen
		min.remove();
		min.reInsert();
		assertEquals(min, min.left);
		assertEquals(min, min.right);
		assertEquals(min, min.up);
		assertEquals(min, min.down);
		//
		plus.remove();
		plus.reInsert();
		assertEquals(pl, plus.left);
		assertEquals(pr, plus.right);
		assertEquals(pu, plus.up);
		assertEquals(pd, plus.down);
		assertEquals(plus, pl.right);
		assertEquals(plus, pr.left);
		assertEquals(plus, pu.down);
		assertEquals(plus, pd.up);
	}

	@Test
	public void testIsLeftmost() {
		assertTrue(min.isLeftmost());
		min.remove();
		assertTrue(min.isLeftmost());
		min.reInsert();
		assertTrue(min.isLeftmost());
		//
		assertTrue(pl.isLeftmost());
		assertFalse(pr.isLeftmost());
		assertTrue(pu.isLeftmost());
		assertTrue(pd.isLeftmost());
		assertFalse(plus.isLeftmost());
	}

	@Test
	public void testIsRowLoner() {
		assertTrue(min.isRowLoner());
		min.remove();
		assertTrue(min.isRowLoner());
		min.reInsert();
		assertTrue(min.isRowLoner());
		//
		assertFalse(pl.isRowLoner());
		assertFalse(pr.isRowLoner());
		assertTrue(pu.isRowLoner());
		assertTrue(pd.isRowLoner());
		assertFalse(plus.isRowLoner());
	}

}
