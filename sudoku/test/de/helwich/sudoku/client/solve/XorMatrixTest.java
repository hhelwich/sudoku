package de.helwich.sudoku.client.solve;


import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * 
 * @author Hendrik Helwich
 */
public class XorMatrixTest {

	private static final Random random = new Random();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void testMain() {
		int height = 10; // cell count
		int width = 4; // cell set count

		assert height > 0 && height <= 30 && width > 0;
		
		int[] xorMatrix = createRandomXorMatrix(height, width);
		printXorMatrix(xorMatrix, height);
		List<Integer> dispensableRows = getDispensableRows(xorMatrix, height);
		System.out.println("dispensable rows: " + dispensableRows);
		removeRows(xorMatrix, dispensableRows); // minimize matrix
		printXorMatrix(xorMatrix, height);
	}

	private void removeRows(int[] xorMatrix, List<Integer> rows) {
		int mask = 0;
		for (int i: rows)
			mask |= 1 << i;
		mask = ~mask;
		for (int i = 0; i < xorMatrix.length; i++)
			xorMatrix[i] &= mask;
	}

	private static final int[] createRandomXorMatrix(int height, int width) {
		int[] matrix = new int[width];
		for (int i = 0; i < width; i++)
			matrix[i] = random.nextInt(1 << height);
		return matrix;
	}

	private static final List<Integer> getDispensableRows(int[] xorMatrix, int height) {
		// store indices of all rows which are not empty (they are removable) 
		List<Integer> rows = new LinkedList<Integer>();
		for (int i = 0; i < height; i++)
			if (!isEmptyRow(xorMatrix, i))
				rows.add(i);
		// check every possible assignment and maybe remove row indices from the list 
		for (int i = 0; i < (1 << height); i++) { // iterate over all possible assignments
			if (isValidAssignment(xorMatrix, height, i)) {
				int j = 0;
				int current = i;
				while (current != 0) {
					if ((current & 1) != 0) {
						try {
							rows.remove((Integer) j);
						} catch (IndexOutOfBoundsException e) {
						}
					}
					j++;
					current >>>= 1;
				}
			}
		}
		return rows;
	}

	private static final boolean isEmptyRow(int[] xorMatrix, int row) {
		for (int col = 0; col < xorMatrix.length; col++)
			if ((xorMatrix[col] & (1<<row)) != 0)
				return false;
		return true;
	}

	private static final boolean isValidAssignment(int[] xorMatrix, int height, int assgn) {
		for (int col = 0; col < xorMatrix.length; col++) {
			boolean isEmptyCol = true;
			boolean foundMatch = false;
			for (int row = 0; row < height; row++) {
				if ((xorMatrix[col] & (1 << row)) != 0) { // matrix element (row, col) is set
					isEmptyCol = false;
					if ((assgn & (1 << row)) != 0) { // row is set in assignment
						if (foundMatch)
							return false;
						else
							foundMatch = true;
					}
				}
			}
			if (!isEmptyCol && !foundMatch)
				return false;
		}
		return true;
	}
	
	private static void printXorMatrix(int[] xorMatrix, int height) {
		for (int row = 0; row < height; row++) {
			System.out.print(row+"  | ");
			for (int col = 0; col < xorMatrix.length; col++) {
				boolean set = ((xorMatrix[col] & (1 << row)) != 0);
				System.out.print(set ? "X " : "  ");
			}
			System.out.println("|");
		}
	}

}
