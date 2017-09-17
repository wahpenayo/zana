package zana.java.comparator;

import java.util.List;

//----------------------------------------------------------------------------
/** Quicksort of List by clojure.lang.IFn.OD, etc.
 *
 * @author John Alan McDonald
 * @version 2017-01-03
 */

@SuppressWarnings("unchecked") 
public final class SortBy extends Object {

  //----------------------------------------------------------------------------
  /** Swap <code>i</code>th and <code>j</code>th elements of <code>a</code> and
   * <code>p</code>.
   * @param a modified by swap.
   * @param p modified by swap.
   * @param i index of element to swap.
   * @param j index of element to swap.
   */
  private static final void swap (final List a,
                                  final int i,
                                  final int j) {
    final Object ai = a.get(i); 
    a.set(i,a.get(j)); 
    a.set(j,ai); }
  //----------------------------------------------------------------------------
  /** Swap <code>n</code> items starting at <code>i0</code> with <code>n</code>
   * items starting at <code>i1</code> in <code>x</code> and <code>p</code>.
   * No check that ranges aren't overlapping.
   * @param x modified by swap.
   * @param p modified by swap.
   * @param i0 starting index of 'source' elements.
   * @param i1 starting index of 'destination' elements.
   * @param n number of elements to swap.
   */
  private static final void swap (final List a,
                                  final int i0,
                                  final int i1,
                                  final int n) {
    int j = i0;
    int k = i1;
    for (int i=0;i<n;i++) { swap(a, j, k); j++; k++; } }
  //----------------------------------------------------------------------------
  /** @return the index of the median of the three indexed doubles.
   */
  private static final int indexOfMedian (final clojure.lang.IFn.OD z,
                                          final List a,
                                          final int i,
                                          final int j,
                                          final int k) {
    final double zi = z.invokePrim(a.get(i));
    final double zj = z.invokePrim(a.get(j));
    final double zk = z.invokePrim(a.get(k));
    if (Double.compare(zi,zj) < 0) {
      if (Double.compare(zj,zk) < 0) { return j; }
      if (Double.compare(zi,zk) < 0) { return k; }
      return i; }
    if (Double.compare(zj,zk) > 0) { return j; }
    if (Double.compare(zi,zk) > 0) { return k; }
    return i; }
  //----------------------------------------------------------------------------
  /** For length < 7. */
  private static final void smallQuicksort (final clojure.lang.IFn.OD z,
                                            final List a,
                                            final int start,
                                            final int length) {
    final int end = start + length;
    for (int i=start; i<end; i++) {
      double z1 = z.invokePrim(a.get(i));
      for (int j=i;j > start;j--) {
        final double z0 = z.invokePrim(a.get(j-1));
        if (Double.compare(z0,z1)<0) { break; }
        z1 = z0;
        swap(a,j,j-1); } } }
  //----------------------------------------------------------------------------
  /** For length < 7. */
  private static final double initialPivot (final clojure.lang.IFn.OD z,
                                            final List a,
                                            final int start,
                                            final int length) {
    int i = start + (length >> 1);
    if (length > 7) {
      int i0 = start;
      int i1 = (start + length) - 1;
      if (length > 40) {
        final int s = length/8;
        i0 = indexOfMedian(z, a, i0, i0+s, i0+(2*s));
        i = indexOfMedian(z, a, i-s, i, i+s);
        i1 = indexOfMedian(z, a, i1-(2*s), i1-s, i1); }
      i = indexOfMedian(z, a, i0, i, i1); }
    return z.invokePrim(a.get(i)); }
  //----------------------------------------------------------------------------
  private static final void innerQuicksort (final clojure.lang.IFn.OD z,
                                            final List a,
                                            final int start,
                                            final int length) {
    final int end = start + length;
    if (length < 7) { smallQuicksort(z,a,start,length); return; }
    final int endm1 = end - 1;
    final double v = initialPivot(z,a,start,length);
    int j0 = start;
    int j1 = start;
    int j2 = endm1;
    int j3 = endm1;
    for (;;) {
      while (j1 <= j2) { 
        final double z1 = z.invokePrim(a.get(j1));
        final int c = Double.compare(z1,v);
        if (c > 0) { break; }
        else if (c == 0) { swap(a, j0++, j1); }
        j1++; }
      while (j2 >= j1) {
        final double z2 = z.invokePrim(a.get(j2));
        final int c = Double.compare(z2,v);
        if (c < 0) { break; }
        else if (c == 0) { swap(a, j2, j3--); }
        j2--; }
      if (j1 > j2) { break; }
      swap(a, j1++, j2--); }
    final int k0 = Math.min(j0-start,j1-j0);
    swap(a, start, j1-k0, k0);
    final int k1 = Math.min(j3-j2,end-j3-1);
    swap(a, j1, end-k1, k1);
    final int k2 = j1-j0;
    if (k2 > 1) { innerQuicksort(z, a, start, k2); }
    final int k3 = j3-j2;
    if (k3 > 1) { innerQuicksort(z, a, end-k3, k3); } }
  //----------------------------------------------------------------------------
  /** @param a list to (partially) sort
   * @param start start of range of elements to sort
   * @param length number of elements to sort
   */
  public static final void quicksort (final clojure.lang.IFn.OD z,
                                      final List a,
                                      final int start,
                                      final int length) {
    assert (0 <= start) && (0 <= length) && (length <= (a.size() - start));
    if (length <= 1) { return; }
    innerQuicksort(z, a, start, length); }
  //----------------------------------------------------------------------------
  /** @param a List to sort
   * @param p elements are subject to the same permutation as the elements of
   *          <code>a</code>. Usually starts as integers in order, so it records
   *          the permutation of <code>a</code>.
   */
  public static final void quicksort (final clojure.lang.IFn.OD z,
                                      final List a) {
    if (a.size() <= 1) { return; }
    quicksort(z, a, 0, a.size()); }
  //----------------------------------------------------------------------------
  // disabled constructor
  //----------------------------------------------------------------------------
  private SortBy () {
    super();
    throw new UnsupportedOperationException("Can't instantiate " + getClass()); }
  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------