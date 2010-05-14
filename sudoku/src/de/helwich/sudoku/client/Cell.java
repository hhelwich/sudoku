package de.helwich.sudoku.client;

/**
 * Immutable index of a sudoku play field cell.
 * 
 * @author Hendrik Helwich
 */
public class Cell implements Comparable<Cell> {

	private int row;
	private int column;
	
	public Cell(int row, int column) {
		if (row < 0 || column < 0)
			throw new IllegalArgumentException("index must not be negative");
		this.row = row;
		this.column = column;
	}
	
	public int getRow() {
		return row;
	}
	
	public int getColumn() {
		return column;
	}

	@Override
	public int compareTo(Cell index) {
		int rd = row - index.row;
		return rd != 0 ? rd : column - index.column;
	}

	@Override
	public int hashCode() {
		return 31 * (31 + column) + row;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cell other = (Cell) obj;
		return column == other.column && row == other.row;
	}

	@Override
	public String toString() {
		return "("+row+","+column+")";
	}
	
}
