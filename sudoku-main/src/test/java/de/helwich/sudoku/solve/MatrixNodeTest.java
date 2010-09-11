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
		assertFalse(min.isRemoved());
		assertFalse(min.remove()); // single node => remove does nothing
		assertFalse(min.remove()); // single node => remove does nothing
		assertFalse(min.isRemoved());
		assertEquals(min, min.left);
		assertEquals(min, min.right);
		assertEquals(min, min.up);
		assertEquals(min, min.down);
		// 
		assertFalse(plus.isRemoved());
		assertFalse(pl.isRemoved());
		assertFalse(pr.isRemoved());
		assertFalse(pu.isRemoved());
		assertFalse(pd.isRemoved());
		
		assertTrue(plus.remove());
		assertTrue(plus.isRemoved());
		assertFalse(pl.isRemoved());
		assertFalse(pr.isRemoved());
		assertFalse(pu.isRemoved());
		assertFalse(pd.isRemoved());
		assertFalse(plus.remove());
		
		assertEquals(pl, plus.left);
		assertEquals(pr, plus.right);
		assertEquals(pu, plus.up);
		assertEquals(pd, plus.down);
		assertEquals(pr, pl.right);
		assertEquals(pl, pr.left);
		assertEquals(pd, pu.down);
		assertEquals(pu, pd.up);
		
		assertTrue(pl.remove());
		assertTrue(plus.isRemoved());
		assertTrue(pl.isRemoved());
		assertFalse(pr.isRemoved());
		assertFalse(pu.isRemoved());
		assertFalse(pd.isRemoved());
		assertFalse(plus.remove());
		assertFalse(pl.remove());
		
		assertFalse(pr.remove()); // pr is single now
		assertTrue(pr.isSingle());
		assertTrue(plus.isRemoved());
		assertTrue(pl.isRemoved());
		assertFalse(pr.isRemoved());
		assertFalse(pu.isRemoved());
		assertFalse(pd.isRemoved());
		assertFalse(plus.remove());
		assertFalse(pl.remove());
		assertFalse(pr.remove());

		assertTrue(pu.remove());
		assertTrue(plus.isRemoved());
		assertTrue(pl.isRemoved());
		assertFalse(pr.isRemoved());
		assertTrue(pu.isRemoved());
		assertFalse(pd.isRemoved());
		assertFalse(plus.remove());
		assertFalse(pl.remove());
		assertFalse(pr.remove());
		assertFalse(pu.remove());
		
		assertTrue(pd.isSingle());
		assertFalse(pd.remove()); // pd is single now

		assertFalse(pd.reInsert());
		assertTrue(pu.reInsert());
		assertFalse(pd.isSingle());
		assertFalse(pr.reInsert());
		assertTrue(pl.reInsert());
		assertTrue(plus.reInsert());
		
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

}
