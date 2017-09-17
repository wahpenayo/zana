package zana.java.arrays;
//----------------------------------------------------------------------------
/** Quicksort of multiple arrays together: First array is the sort key, rest
 * just tag along for the ride.
 * <code>(xxx[],int[])</code> can be used to generate permutation vectors.
 * <code>(xxx[],yyy[],...)</code> are quicker for sorting yyy by the values in
 * xxx.
 *
 * @author John Alan McDonald
 * @version 2016-08-29
 */

public final class Sorter extends Object {

  //----------------------------------------------------------------------------
  // permutation related stuff.
  //----------------------------------------------------------------------------
  /** @param perm first <code>n</code> elements of <code>perm</code> are set to
   * <code>0,1,...,n-1</code>.
   * @param n length of returned array
   */
  public static final void iota (final int[] perm, final int n) {
    for (int i=0;i<n;i++) { perm[i] = i; } }
  //----------------------------------------------------------------------------
  /** @param n length of returned array
   * @return an array containing the ints 0, ..., n-1
   */
  public static final int[] iota (final int n) {
    final int[] ia = new int[n];
    for (int i=0;i<n;i++) { ia[i] = i; }
    return ia; }
  //----------------------------------------------------------------------------
  /** Special case partial permutation of 2 input arrays into 2 output arrays.
   *  @param ain array to be permuted
   *  @param bin array to be permuted
   *  @param p permutation, but no check for validity
   *  @param n permute using the first <code>n</code> elements of <code>p</code>.
   *  @param aout <code>aout[i] = ain[p[i]]</code>.
   *  @param bout <code>bout[i] = bin[p[i]]</code>.
   */
  public static final void permute (final double[] ain,
                                    final double[] bin,
                                    final int[] p,
                                    final int n,
                                    final double[] aout,
                                    final double[] bout) {
    for (int i=0;i<n;i++) { 
      final int pi = p[i];
      aout[i] = ain[pi]; 
      bout[i] = bin[pi]; } }
  //----------------------------------------------------------------------------
  /** @return <code>b[i] = a[p[i]]</code>.
   *  No check that <code>p</code> is really a permutation of
   *  <code>0</code>...<code>n-1</code>.
   *  @param a array to be permuted
   *  @param p permutation
   */
  public static final double[] permute (final double[] a,
                                        final int[] p) {
    assert (a.length == p.length);
    final int n = a.length;
    final double[] b = new double[n];
    for (int i=0;i<n;i++) { b[i] = a[p[i]]; }
    return b; }
  //----------------------------------------------------------------------------
  /** @return <code>b[p[i]] = a[i]</code>
   *  No check that <code>p</code> is really a permutation of
   *  <code>0</code>...<code>n-1</code>.
   *  @param a array to be permuted
   *  @param p inverse of permutation applied to <code>a</code>.
   */
  public static final double[] inversePermute (final double[] a,
                                               final int[] p) {
    assert (a.length == p.length);
    final int n = a.length;
    final double[] b = new double[n];
    for (int i=0;i<n;i++) { b[p[i]] = a[i]; }
    return b; }
  //----------------------------------------------------------------------------
  /** @return <code>b[i] = a[p[i]]</code>.
   *  No check that <code>p</code> is really a permutation of
   *  <code>0</code>...<code>n-1</code>.
   *  @param a array to be permuted
   *  @param p permutation
   */
  public static final Object[] permute (final Object[] a,
                                        final int[] p) {
    assert (a.length == p.length);
    final int n = a.length;
    final Object[] b = new Object[n];
    for (int i=0;i<n;i++) { b[i] = a[p[i]]; }
    return b; }
  //----------------------------------------------------------------------------
  /** @return <code>b[p[i]] = a[i]</code>
   *  No check that <code>p</code> is really a permutation of
   *  <code>0</code>...<code>n-1</code>.
   *  @param a array to be permuted
   *  @param p inverse of permutation applied to <code>a</code>.
   */
  public static final Object[] inversePermute (final Object[] a,
                                               final int[] p) {
    assert (a.length == p.length);
    final int n = a.length;
    final Object[] b = new Object[n];
    for (int i=0;i<n;i++) { b[p[i]] = a[i]; }
    return b; }
  //----------------------------------------------------------------------------
  // (double[],int[])
  //----------------------------------------------------------------------------
  /** Swap <code>i</code>th and <code>j</code>th elements of <code>a</code> and
   * <code>p</code>.
   * @param a modified by swap.
   * @param p modified by swap.
   * @param i index of element to swap.
   * @param j index of element to swap.
   */
  private static final void swap (final double[] a,
                                  final int[] p,
                                  final int i,
                                  final int j) {
    final double ai = a[i]; a[i] = a[j]; a[j] = ai;
    final int pi = p[i]; p[i] = p[j]; p[j] = pi; }
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
  private static final void swap (final double[] x,
                                  final int[] p,
                                  final int i0,
                                  final int i1,
                                  final int n) {
    int j = i0;
    int k = i1;
    for (int i=0;i<n;i++) { swap(x, p, j, k); j++; k++; } }
  //----------------------------------------------------------------------------
  /** @return the index of the median of the three indexed doubles.
   */
  private static final int indexOfMedian (final double[] a,
                                          final int i,
                                          final int j,
                                          final int k) {
    final double ai = a[i];
    final double aj = a[j];
    final double ak = a[k];
    if (ai < aj) {
      if (aj < ak) { return j; }
      if (ai < ak) { return k; }
      return i; }
    if (aj > ak) { return j; }
    if (ai > ak) { return k; }
    return i; }
  //----------------------------------------------------------------------------
  /** For length < 7. */
  private static final void smallQuicksort (final double[] a,
                                            final int[] p,
                                            final int start,
                                            final int length) {
    final int end = start + length;
    for (int i=start; i<end; i++) {
      for (int j=i; (j > start) && (a[j-1] > a[j]); j--) {
        swap(a,p,j,j-1); } } }
  //----------------------------------------------------------------------------
  /** For length < 7. */
  private static final double initialPivot (final double[] a,
                                            final int start,
                                            final int length) {
    int i = start + (length >> 1);
    if (length > 7) {
      int i0 = start;
      int i1 = (start + length) - 1;
      if (length > 40) {
        final int s = length/8;
        i0 = indexOfMedian(a, i0, i0+s, i0+(2*s));
        i = indexOfMedian(a, i-s, i, i+s);
        i1 = indexOfMedian(a, i1-(2*s), i1-s, i1); }
      i = indexOfMedian(a, i0, i, i1); }
    return a[i]; }
  //----------------------------------------------------------------------------
  //NOPMD -- hard to reduce complexity.
  private static final void innerQuicksort (final double[] a,
                                            final int[] p,
                                            final int start,
                                            final int length) {
    final int end = start + length;
    if (length < 7) { smallQuicksort(a,p,start,length); return; }
    final int endm1 = end - 1;
    final double v = initialPivot(a,start,length);
    int j0 = start;
    int j1 = start;
    int j2 = endm1;
    int j3 = endm1;
    for (;;) {
      while ((j1 <= j2) && (a[j1] <= v)) {
        if (a[j1] == v) { swap(a, p, j0++, j1); }
        j1++; }
      while ((j2 >= j1) && (a[j2] >= v)) {
        if (a[j2] == v) { swap(a, p, j2, j3--); }
        j2--; }
      if (j1 > j2) { break; }
      swap(a, p, j1++, j2--); }
    final int k0 = Math.min(j0-start,j1-j0);
    swap(a, p, start, j1-k0, k0);
    final int k1 = Math.min(j3-j2,end-j3-1);
    swap(a, p, j1, end-k1, k1);
    final int k2 = j1-j0;
    if (k2 > 1) { innerQuicksort(a, p, start, k2); }
    final int k3 = j3-j2;
    if (k3 > 1) { innerQuicksort(a, p, end-k3, k3); } }
  //----------------------------------------------------------------------------
  /** Like java {@link java.util.Arrays#sort(double[])}, but doesn't sort
   * negative/positive zeros and throws {@link IllegalArgumentException}
   * if there are any NaNs.
   *
   * @param a array to (partially) sort
   * @param p elements are subject to the same permutation as the elements of
   *          <code>a</code>. Usually starts as integers in order, so it records
   *          the permutation of <code>a</code>.
   * @param start start of range of elements to sort
   * @param length number of elements to sort
   */
  public static final void quicksort (final double[] a,
                                      final int[] p,
                                      final int start,
                                      final int length) {
    final int n = start + length;
    for (int i=start;i<n;i++) {
      if (a[i] != a[i]) {
        throw new IllegalArgumentException("Can't sort NaN."); } }
    innerQuicksort(a, p, start, length); }
  //----------------------------------------------------------------------------
  /** Like java {@link java.util.Arrays#sort(double[])}, but doesn't sort
   *  negative/positive zeros and throws {@link IllegalArgumentException}
   *  if there are any NaNs.
   *
   * @param a array to sort
   * @param p elements are subject to the same permutation as the elements of
   *          <code>a</code>. Usually starts as integers in order, so it records
   *          the permutation of <code>a</code>.
   */
  public static final void quicksort (final double[] a,
                                      final int[] p) {
    assert a.length == p.length;
    if (a.length <= 1) { return; }
    quicksort(a, p, 0, a.length); }
  //----------------------------------------------------------------------------
  // (double[],double[])
  //----------------------------------------------------------------------------
  private static final void swap (final double[] a,
                                  final double[] p,
                                  final int i,
                                  final int j) {
    final double ai = a[i]; a[i] = a[j]; a[j] = ai;
    final double pi = p[i]; p[i] = p[j]; p[j] = pi; }
  //----------------------------------------------------------------------------
  /** Swap n items starting at i0 with n items starting at i1 in x and p.
   */
  private static final void swap (final double[] x,
                                  final double[] p,
                                  final int i0,
                                  final int i1,
                                  final int n) {
    int j = i0;
    int k = i1;
    for (int i=0;i<n;i++) { swap(x, p, j, k); j++; k++; } }
  //----------------------------------------------------------------------------
  /** For length < 7. */
  private static final void smallQuicksort (final double[] a,
                                            final double[] p,
                                            final int start,
                                            final int length) {
    final int end = start + length;
    for (int i=start; i<end; i++) {
      for (int j=i; (j > start) && (a[j-1] > a[j]); j--) {
        swap(a,p,j,j-1); } } }
  //----------------------------------------------------------------------------
  //NOPMD -- hard to reduce complexity.
  private static final void innerQuicksort (final double[] a,
                                            final double[] p,
                                            final int start,
                                            final int length) {
    final int end = start + length;
    if (length < 7) { smallQuicksort(a,p,start,length); return; }
    final int endm1 = end - 1;
    final double v = initialPivot(a,start,length);
    int j0 = start;
    int j1 = start;
    int j2 = endm1;
    int j3 = endm1;
    for (;;) {
      while ((j1 <= j2) && (a[j1] <= v)) {
        if (a[j1] == v) { swap(a, p, j0++, j1); }
        j1++; }
      while ((j2 >= j1) && (a[j2] >= v)) {
        if (a[j2] == v) { swap(a, p, j2, j3--); }
        j2--; }
      if (j1 > j2) { break; }
      swap(a, p, j1++, j2--); }
    final int k0 = Math.min(j0-start,j1-j0);
    swap(a, p, start, j1-k0, k0);
    final int k1 = Math.min(j3-j2,end-j3-1);
    swap(a, p, j1, end-k1, k1);
    final int k2 = j1-j0;
    if (k2 > 1) { innerQuicksort(a, p, start, k2); }
    final int k3 = j3-j2;
    if (k3 > 1) { innerQuicksort(a, p, end-k3, k3); } }
  //----------------------------------------------------------------------------
  /** Like java {@link java.util.Arrays#sort(double[])}, but doesn't sort
   *  negative/positive zeros and throws {@link IllegalArgumentException}
   *  if there are any NaNs.
   *
   * @param a array to (partially) sort
   * @param p elements are subject to the same permutation as the elements of
   *          <code>a</code>. Usually starts as integers in order, so it records
   *          the permutation of <code>a</code>.
   * @param start start of range of elements to sort
   * @param length number of elements to sort
   */
  public static final void quicksort (final double[] a,
                                      final double[] p,
                                      final int start,
                                      final int length) {
    final int n = start + length;
    for (int i=start;i<n;i++) {
      if (a[i] != a[i]) {
        throw new IllegalArgumentException("Can't sort NaN."); } }
    innerQuicksort(a, p, start, length); }
  //----------------------------------------------------------------------------
  /** Like java {@link java.util.Arrays#sort(double[])}, but doesn't sort
   *  negative/positive zeros and throws {@link IllegalArgumentException}
   *  if there are any NaNs.
   * @param a array to sort
   * @param p elements are subject to the same permutation as the elements of
   *          <code>a</code>. Usually starts as integers in order, so it records
   *          the permutation of <code>a</code>.
   */
  public static final void quicksort (final double[] a,
                                      final double[] p) {
    assert a.length == p.length;
    if (a.length <= 1) { return; }
    quicksort(a, p, 0, a.length); }
  //----------------------------------------------------------------------------
  // (float[],float[])
  //----------------------------------------------------------------------------
  private static final void swap (final float[] a,
                                  final float[] p,
                                  final int i,
                                  final int j) {
    final float ai = a[i]; a[i] = a[j]; a[j] = ai;
    final float pi = p[i]; p[i] = p[j]; p[j] = pi; }
  //----------------------------------------------------------------------------
  /** Swap n items starting at i0 with n items starting at i1 in x and p.
   */
  private static final void swap (final float[] x,
                                  final float[] p,
                                  final int i0,
                                  final int i1,
                                  final int n) {
    int j = i0;
    int k = i1;
    for (int i=0;i<n;i++) { swap(x, p, j, k); j++; k++; } }
  //----------------------------------------------------------------------------
  /** @return the index of the median of the three indexed floats.
   */
  private static final int indexOfMedian (final float[] a,
                                          final int i,
                                          final int j,
                                          final int k) {
    final float ai = a[i];
    final float aj = a[j];
    final float ak = a[k];
    if (ai < aj) {
      if (aj < ak) { return j; }
      if (ai < ak) { return k; }
      return i; }
    if (aj > ak) { return j; }
    if (ai > ak) { return k; }
    return i; }
  //----------------------------------------------------------------------------
  /** For length < 7. */
  private static final void smallQuicksort (final float[] a,
                                            final float[] p,
                                            final int start,
                                            final int length) {
    final int end = start + length;
    for (int i=start; i<end; i++) {
      for (int j=i; (j > start) && (a[j-1] > a[j]); j--) {
        swap(a,p,j,j-1); } } }
  //----------------------------------------------------------------------------
  /** For length < 7. */
  private static final float initialPivot (final float[] a,
                                           final int start,
                                           final int length) {
    int i = start + (length >> 1);
    if (length > 7) {
      int i0 = start;
      int i1 = (start + length) - 1;
      if (length > 40) {
        final int s = length/8;
        i0 = indexOfMedian(a, i0, i0+s, i0+(2*s));
        i = indexOfMedian(a, i-s, i, i+s);
        i1 = indexOfMedian(a, i1-(2*s), i1-s, i1); }
      i = indexOfMedian(a, i0, i, i1); }
    return a[i]; }
  //----------------------------------------------------------------------------
  //NOPMD -- hard to reduce complexity.
  private static final void innerQuicksort (final float[] a,
                                            final float[] p,
                                            final int start,
                                            final int length) {
    final int end = start + length;
    if (length < 7) { smallQuicksort(a,p,start,length); return; }
    final int endm1 = end - 1;
    final float v = initialPivot(a,start,length);
    int j0 = start;
    int j1 = start;
    int j2 = endm1;
    int j3 = endm1;
    for (;;) {
      while ((j1 <= j2) && (a[j1] <= v)) {
        if (a[j1] == v) { swap(a, p, j0++, j1); }
        j1++; }
      while ((j2 >= j1) && (a[j2] >= v)) {
        if (a[j2] == v) { swap(a, p, j2, j3--); }
        j2--; }
      if (j1 > j2) { break; }
      swap(a, p, j1++, j2--); }
    final int k0 = Math.min(j0-start,j1-j0);
    swap(a, p, start, j1-k0, k0);
    final int k1 = Math.min(j3-j2,end-j3-1);
    swap(a, p, j1, end-k1, k1);
    final int k2 = j1-j0;
    if (k2 > 1) { innerQuicksort(a, p, start, k2); }
    final int k3 = j3-j2;
    if (k3 > 1) { innerQuicksort(a, p, end-k3, k3); } }
  //----------------------------------------------------------------------------
  /** Like java {@link java.util.Arrays#sort(float[])}, but doesn't sort
   *  negative/positive zeros and throws {@link IllegalArgumentException}
   *  if there are any NaNs.
   *
   * @param a array to (partially) sort
   * @param p elements are subject to the same permutation as the elements of
   *          <code>a</code>. Usually starts as integers in order, so it records
   *          the permutation of <code>a</code>.
   * @param start start of range of elements to sort
   * @param length number of elements to sort
   */
  public static final void quicksort (final float[] a,
                                      final float[] p,
                                      final int start,
                                      final int length) {
    final int n = start + length;
    for (int i=start;i<n;i++) {
      if (a[i] != a[i]) {
        throw new IllegalArgumentException("Can't sort NaN."); } }
    innerQuicksort(a, p, start, length); }
  //----------------------------------------------------------------------------
  /** Like java {@link java.util.Arrays#sort(float[])}, but doesn't sort
   *  negative/positive zeros and throws {@link IllegalArgumentException}
   *  if there are any NaNs.
   *
   * @param a array to sort
   * @param p elements are subject to the same permutation as the elements of
   *          <code>a</code>. Usually starts as integers in order, so it records
   *          the permutation of <code>a</code>.
   */
  public static final void quicksort (final float[] a,
                                      final float[] p) {
    assert a.length == p.length;
    if (a.length <= 1) { return; }
    quicksort(a, p, 0, a.length); }
  //----------------------------------------------------------------------------
  // disabled constructor
  //----------------------------------------------------------------------------
  private
  Sorter () {
    super();
    throw new UnsupportedOperationException(
      "Can't instantiate " + getClass()); }
  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------