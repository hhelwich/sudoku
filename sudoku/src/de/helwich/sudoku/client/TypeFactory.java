package de.helwich.sudoku.client;


/**
 * @author Hendrik Helwich
 */
public class TypeFactory {


	/* possible sudokus (without mirroring and trivial ones) to size 32:
	 *  
	 *  4: 2x2
	 *  6: 2x3
	 *  8: 2x4
	 *  9: 3x3
	 * 10: 2x5
	 * 12: 2x6,  3x4
	 * 14: 2x7
	 * 15: 3x5
	 * 16: 2x8,  4x4
	 * 18: 2x9,  3x6
	 * 20: 2x10, 4x5
	 * 21: 3x7
	 * 22: 2x11
	 * 24: 2x12, 3x8,  4x6
	 * 25: 5x5
	 * 26: 2x13
	 * 27: 3x9
	 * 28: 2x14, 4x7
	 * 30: 2x15, 3x10, 5x6
	 * 32: 2x16, 4x8
	 */
	public static Type createDefaultType(int row, int column, String fieldChars) {
		int size = row * column;
		if (row < 1 || column < 1 || size > BitSetUtil.MAX_INDEX+1)
			throw new IllegalArgumentException("invalid sudoku size");
		Type type = new Type();
		type.setFieldChars(fieldChars);
		int bmap = BitSetUtil.set(0, 0, size);
		CellIndex[] fi;
		for (int i = 0; i < size; i ++) {
			// define row groups
			fi = new CellIndex[size];
			for (int j = 0; j < size; j ++)
				fi[j] = new CellIndex(i, j);
			type.addCellGroup(new CellGroup(bmap, fi));
			// define column groups
			fi = new CellIndex[size];
			for (int j = 0; j < size; j ++)
				fi[j] = new CellIndex(j, i);
			type.addCellGroup(new CellGroup(bmap, fi));
		}
		// define block groups
		for (int i = 0; i < row; i ++) {
			for (int j = 0; j < column; j ++) {
				fi = new CellIndex[size];
				for (int k = 0; k < column; k ++)
					for (int l = 0; l < row; l ++)
						fi[k*row + l] = new CellIndex(i*column + k, j*row + l);
				type.addCellGroup(new CellGroup(bmap, fi));
			}
		}
		type.pack();
		return type;
	}

}
