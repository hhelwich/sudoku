package de.helwich.sudoku.client;

/**
 * Static operations to operate with int bitsets (32 bit).
 * 
 * @author Hendrik Helwich
 */
public class BitSetUtil {

	private BitSetUtil() {
	}

	/** maximum index of a bitset (minimum index is 0) */
	public static final int MAX_INDEX = 31;

	/** int with all bits set */
	private static final int INT_MASK = 0xffffffff;

	/**
	 * Sets the bit at the specified index to <code>true</code>.
	 * 
	 * @param  bitset
	 *         a bitset.
	 * @param  bitIndex
	 *         a bit index.
	 * @return the bitset with the set bit.
	 * @throws IndexOutOfBoundsException
	 *         if the specified index is negative or greater than
	 *         {@link #MAX_INDEX}
	 */
	public static int set(int bitset, int bitIndex)
			throws IndexOutOfBoundsException {
		checkIndexRange(bitIndex, true);
		return bitset | (1 << bitIndex);
	}

	/**
	 * Sets the bit specified by the index to <code>false</code>.
	 * 
	 * @param  bitset
	 *         a bitset.
	 * @param  bitIndex
	 *         the index of the bit to be cleared.
	 * @return the bitset with the cleared bit.
	 * @throws IndexOutOfBoundsException
	 *         if the specified index is negative or greater than
	 *         {@link #MAX_INDEX}
	 */
	public static int clear(int bitset, int bitIndex)
			throws IndexOutOfBoundsException {
		checkIndexRange(bitIndex, true);
		return bitset & ~(1 << bitIndex);
	}

	/**
	 * Returns the value of the bit with the specified index. The value is
	 * <code>true</code> if the bit with the index <code>bitIndex</code> is
	 * currently set in this <code>BitSet</code>; otherwise, the result is
	 * <code>false</code>.
	 * 
	 * @param  bitset
	 *         a bitset.
	 * @param  bitIndex
	 *         the bit index.
	 * @return the value of the bit with the specified index.
	 * @throws IndexOutOfBoundsException
	 *         if the specified index is negative or greater than
	 *         {@link #MAX_INDEX}
	 */
	public static boolean get(int bitset, int bitIndex)
			throws IndexOutOfBoundsException {
		checkIndexRange(bitIndex, true);
		return (bitset & (1 << bitIndex)) != 0;
	}

	/**
	 * Returns the index of the first bit that is set to <code>true</code> that
	 * occurs on or after the specified starting index. If no such bit exists
	 * then -1 is returned.
	 * 
	 * To iterate over the <code>true</code> bits in a bitset, use the following
	 * loop:
	 * 
	 * <pre>
	 * for (int i = nextSetBit(bitset, 0); i &gt;= 0; i = nextSetBit(bitset, i + 1))
	 *     ; // do something
	 * </pre>
	 * 
	 * @param  bitset
	 *         a bitset.
	 * @param  fromIndex
	 *         the bit index after which the first bit that is set to
	 *         <code>true</code> is returned.
	 * @return the index of the first bit that is set to <code>true</code> that
	 *         occurs on or after the specified starting index
	 * @throws IndexOutOfBoundsException
	 *         if the specified index is negative
	 */
	public static int nextSetBit(int bitset, int fromIndex)
			throws IndexOutOfBoundsException {
		checkIndexRange(fromIndex, false);
		if (fromIndex > MAX_INDEX)
			return -1;
		bitset &= (INT_MASK << fromIndex);
		if (bitset != 0)
			return Integer.numberOfTrailingZeros(bitset);
		return -1;
	}

	/**
	 * Throws an {@link IndexOutOfBoundsException} if the given bit index is
	 * negative or if it the second argument is <code>true</code> also if the
	 * index is greater than {@link #MAX_INDEX}.
	 * 
	 * @param  index
	 * @param  checkMax
	 * @throws IndexOutOfBoundsException
	 */
	private static void checkIndexRange(int index, boolean checkMax)
			throws IndexOutOfBoundsException {
		if (index < 0 || (checkMax && index > MAX_INDEX))
			throw new IndexOutOfBoundsException("bitIndex [0..." + MAX_INDEX
					+ "]: " + index);
	}

	/**
	 * Returns the number of bits set to <tt>true</tt> in the bitset.
	 * 
	 * @param  bitset
	 *         the bitset.
	 * @return the number of bits set to <tt>true</tt> in the bitset.
	 */
	public static int cardinality(int bitset) {
		return Integer.bitCount(bitset);
	}

	/**
	 * For the given list of bitsets return all possible permutations of the bit
	 * indices.
	 * 
	 * @param  bitsets
	 * @return
	 */
	public static int[] getPossiblePermutations(int[] bitsets) {
		int[] nsets = new int[bitsets.length];
		getPossiblePermutations(bitsets, nsets, 0, 0);
		return nsets;
	}

	private static boolean getPossiblePermutations(int[] bitsets,
			int[] newbitsets, int bitsetIndex, int usedBits) {
		// TODO optimize
		int bitset = bitsets[bitsetIndex];
		bitset = bitset & (~usedBits);
		if (bitset == 0)
			return false;
		if (bitsetIndex + 1 == bitsets.length)
			newbitsets[bitsetIndex] |= bitset;
		else
			for (int i = nextSetBit(bitset, 0); i >= 0; i = nextSetBit(bitset,
					i + 1)) {
				int used = set(usedBits, i);
				if (getPossiblePermutations(bitsets, newbitsets,
						bitsetIndex + 1, used))
					newbitsets[bitsetIndex] = set(newbitsets[bitsetIndex], i);
			}
		return newbitsets[bitsetIndex] != 0;
	}

}
