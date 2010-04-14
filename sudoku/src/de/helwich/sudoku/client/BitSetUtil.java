package de.helwich.sudoku.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Static operations to operate with int bitsets (32 bit).
 * 
 * @author Hendrik Helwich
 */
public class BitSetUtil {

	private BitSetUtil() {}

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
		checkIndexRange(bitIndex, MAX_INDEX);
		return bitset | (1 << bitIndex);
	}

    /**
     * Sets the bits from the specified <tt>fromIndex</tt> (inclusive) to the
     * specified <tt>toIndex</tt> (exclusive) to <code>true</code>.
     * 
     * @param  bitset
     * @param  fromIndex
     *         index of the first bit to be set.
     * @param  toIndex
     *         index after the last bit to be set.
     * @return
     * @throws IndexOutOfBoundsException
     *         if <tt>fromIndex</tt> is negative,
     *         or <tt>toIndex</tt> is negative, or <tt>fromIndex</tt> is
     *         larger than <tt>toIndex</tt>.
     * @throws IllegalArgumentException
     */
    public static int set(int bitset, int fromIndex, int toIndex)
			throws IndexOutOfBoundsException, IllegalArgumentException {
		checkIndexRange(fromIndex, MAX_INDEX);
		checkIndexRange(toIndex, MAX_INDEX+1);
		if (fromIndex > toIndex)
			throw new IllegalArgumentException("fromIndex parameter must be lower or equal to toIndex parameter");
		int mask = INT_MASK << fromIndex;
		mask &= INT_MASK >>> -toIndex;
		return bitset | mask;
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
		checkIndexRange(bitIndex, MAX_INDEX);
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
		checkIndexRange(bitIndex, MAX_INDEX);
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
		checkIndexRange(fromIndex, Integer.MAX_VALUE);
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
	private static void checkIndexRange(int index, int maxIndex)
			throws IndexOutOfBoundsException {
		if (index < 0 || index > maxIndex)
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
	 * Returns <code>true</code> if the first parameter bitset is a subset of
	 * the second parameter bitset.
	 * 
	 * @param  subbitset
	 * @param  bitset
	 * @return
	 */
	public static boolean subset(int subbitset, int bitset) {
		return (bitset & subbitset) == subbitset;
	}
	
	/**
	 * Faster try of {@link #getPossiblePermutations(int[])}
	 * 
	 * @param  bitsets
	 */
	public static void getPossiblePermutationsFast(int[] bitsets) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0 ; i < bitsets.length; i++) {
			if (bitsets[i] == 0) {
				for (i = 0 ; i < bitsets.length; i++)
					bitsets[i] = 0;
				return;
			}
			for (int key : map.keySet())
				if (subset(bitsets[i], key))
					map.put(key, map.get(key)+1);
			Integer set = map.get(bitsets[i]);
			if (set == null) {
				int cnt = 1;
				for (int j = 0 ; j < i; j++) 
					if (subset(bitsets[j], bitsets[i]))
						cnt ++;
				map.put(bitsets[i], cnt);
			}
			for (int key : new HashSet<Integer>(map.keySet())) {
				int union = bitsets[i] | key;
				if (!map.containsKey(union)) {
					int count = 0;
					for (int j = 0 ; j <= i; j++) 
						if (subset(bitsets[j], union))
							count ++;
					map.put(union, count);
				}
			}
		}
		for (int s : map.keySet()) {
			int v = map.get(s);
			if (cardinality(s) == v) {
				boolean change = false;
				for (int i = 0 ; i < bitsets.length; i++)
					if (!subset(bitsets[i], s)) {
						int n = bitsets[i] & (~s);
						change |= n != bitsets[i];
						bitsets[i] = n;
					}
				if (change) {
					getPossiblePermutationsFast(bitsets);
					return;
				}
			} else if (cardinality(s) < v)
				for (int i = 0 ; i < bitsets.length; i++)
					bitsets[i] = 0;
		}
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

	/**
	 * Recursively check all possible permutations.
	 * 
	 * @param  bitsets
	 *         static input bitset sets
	 * @param  newbitsets
	 *         the new bitset sets which are filled by this operation
	 * @param  bitsetIndex
	 *         test bitset in the <code>bitsets</code> parameter with this index
	 * @param  usedBits
	 *         the indices given by this bitset are already used before and must
	 *         be ignored
	 * @return <code>true</code> if a permutation exists from the given
	 *         <code>bitsetIndex</code>.
	 */
	private static boolean getPossiblePermutations(int[] bitsets,
			int[] newbitsets, int bitsetIndex, int usedBits) {
		// TODO optimize
		int bitset = bitsets[bitsetIndex];
		bitset = bitset & (~usedBits);
		if (bitset == 0)
			return false;
		if (bitsetIndex + 1 == bitsets.length) { // last bitset
			newbitsets[bitsetIndex] |= bitset; // add all remaining indices to the last new bitset
			return true;
		} else { // inner bitset
			// iterate over indices of current bitset
			boolean ret = false;
			for (int i = nextSetBit(bitset, 0); i >= 0; i = nextSetBit(bitset, i + 1)) {
				int used = set(usedBits, i); // current index is used now
				if (getPossiblePermutations(bitsets, newbitsets,
						bitsetIndex + 1, used)) {
					newbitsets[bitsetIndex] = set(newbitsets[bitsetIndex], i);
					ret = true;
				}
			}
			return ret;
		}
	}

}
