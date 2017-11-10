package zana.java.prob;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.MathArrays;
import org.apache.commons.math3.util.MathArrays.OrderDirection;

import zana.java.arrays.Sorter;
import zana.java.math.Statistics;

//----------------------------------------------------------------
/** Weighted empirical probability density, a collection of point
 * masses.
 * <ul>
 * <li><code>z</code> sorted unique domain values.
 * <li><code>w</code> normalized weights.
 * </ul>
 * Probability of <code>z==z[i]</code> is <code>w[i]</code>.
 * <p>
 * TODO: could use 
 * org.apache.commons.math3.distribution.EnumeratedRealDistribution,
 * but that converts data to 
 * <code>List&lt;Pair&lt;Double,Double&gt;&gt;</code>, which
 * like hurts significantly in space and time.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2017-10-27
 * @version 2017-11-09
 */

public final class WEPDF extends AbstractRealDistribution 
implements ApproximatelyEqual {

  private static final long serialVersionUID = 1L;

  private final double[] z;
  /** Internal locations of point masses.
   */
  public final double[] getZ () { 
    return Arrays.copyOf(z,z.length); }
  
  private final double[] w;
  /** Weight of each point mass.
   */
  public final double[] getW () { 
    return Arrays.copyOf(w,w.length); }

  //--------------------------------------------------------------
  // RealDistribution interface
  //--------------------------------------------------------------

  @Override
  public final double probability (final double x) {
    final int i = Arrays.binarySearch(z, x);
    if (0 > i) { return 0.0; }
    return w[i]; }

  @Override
  public final double cumulativeProbability (final double x) {
    final int i = Arrays.binarySearch(z, x);
    final int k;
    if (0 <= i) { k = i+1; }
    else { k = -1 - i; }
    assert (0 <= k);
    if (0 == k) { return 0.0; }
    return 
      Math.min(
        Math.max(0.0, Statistics.kahanSum(w,0,k)), 
        1.0); }

  @Override
  public final double inverseCumulativeProbability (final double p) {
    assert ((0.0 <= p) && (p <= 1.0));
    if (0.0 == p) { return Double.NEGATIVE_INFINITY; }
    final int n = z.length;
    if (1.0 == p) { return z[n-1]; }
    double s = w[0];
    for (int i=0;;) {
      // shouldn't be possible to go outside array bounds 
      // with valid <code>z,w</code>.
      assert (i < n);
      if ((p - s) < Math.ulp(1.0)) { return z[i]; }
      i++;
      s += w[i];
      s = Math.min(Math.max(0.0, s), 1.0); } }

  @Override
  public final double density (final double x) {
    throw new UnsupportedOperationException(
      "density" + " unsupported for " + getClass()); }

  @Override
  public final double getNumericalMean () {
    // TODO: use fused-multiply-add in JDK9 for more accurate value
    // TODO: cache value?
    double s = 0.0;
    double c = 0.0;
    for (int i=0;i<z.length;i++) {
      final double zi = z[i]*w[i] - c;
      final double t = s + zi;
      c = (t - s) - zi;
      s = t; } 
    return s; }

  @Override
  public final double getNumericalVariance () {
    // TODO: Chan algorithm?
    // TODO: cache value?
    final double mean = getNumericalMean();
    double s = 0.0;
    double c = 0.0;
    for (int i=0;i<z.length;i++) {
      final double dz = z[i] - mean;
      final double zi = dz*dz*w[i] - c;
      final double t = s + zi;
      c = (t - s) - zi;
      s = t; } 
    return s; }

  @Override
  public final double getSupportLowerBound () { return z[0]; }
  @Override
  public final double getSupportUpperBound () { return z[z.length-1]; }
  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public final boolean isSupportLowerBoundInclusive () { return true; }
  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public final boolean isSupportUpperBoundInclusive () { return true; }
  @Override
  public final boolean isSupportConnected () { return false; }

  //--------------------------------------------------------------
  // ApproximatelyEquals interface
  //--------------------------------------------------------------

  @Override
  public final boolean approximatelyEqual (final ApproximatelyEqual that) {
    if (! (that instanceof WEPDF)) { return false; }
    return 
      Statistics.approximatelyEqual(z,((WEPDF) that).z) 
      &&
      Statistics.approximatelyEqual(w,((WEPDF) that).w); }

  @Override
  public final boolean approximatelyEqual (final double delta,
                                           final ApproximatelyEqual that) {
    if (! (that instanceof WEPDF)) { return false; }
    return 
      Statistics.approximatelyEqual(delta,z,((WEPDF) that).z) 
      &&
      Statistics.approximatelyEqual(delta,w,((WEPDF) that).w); }

  //--------------------------------------------------------------
  // Object interface
  //--------------------------------------------------------------

  @Override
  public final int hashCode () {
    return 
      (37 * ((37*17) + Arrays.hashCode(w))) 
      + Arrays.hashCode(z); }

  @Override
  public final boolean equals (final Object that) {
    if (! (that instanceof WEPDF)) { return false; }
    if (! Arrays.equals(z,((WEPDF) that).z)) { return false; }
    if (! Arrays.equals(w,((WEPDF) that).w)) { return false; }
    return true; }

  // TODO: fix for large arrays
  @Override
  public final String toString () {
    return "(WEPDF \n"
      + Arrays.toString(z) + "\n" 
      + Arrays.toString(w) + ")"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  /** Constructor requires that <code>zz</code> and 
   * <code>w<z/code> 
   * satisfy:
   * <ul>
   * <li> <code>zz</code> and <code>ww</code> are the same length.
   * <li> Elements of <code>zz</code> are strictly increasing.
   * <li> Elements of <code>zz</code> are finite 
   * (not <code>NaN</code> or infinite).
   * <li> Elements of <code>ww</code> are between 0.0 and 1.0 ,
   * both inclusive.
   * <li> Elements of <code>ww</code> sum to 1.0 (within about
   * <code>Math.ulp(2.0)</code>.
   */
  
  private WEPDF (final RandomGenerator rng,
                 final double[] zz,
                 final double[] ww) { 
    super(rng); 
    // TODO: better messages with large arrays
    assert zz.length == ww.length :
      "Unequal lengths: zz=" + zz.length + ", ww=" + ww.length;
    assert Statistics.isFinite(zz) : Arrays.toString(zz);
    assert MathArrays.isMonotonic(
      zz, OrderDirection.INCREASING, true) :
        "not increasing:\n" + Arrays.toString(zz);
    assert Statistics.isConvex(ww) :
      "not convex: " + Arrays.toString(ww);

    z = zz;
    w = ww; }

  //--------------------------------------------------------------
  /** Create a weighted empirical distribution from
   * locations of point masses.
   * 
   * @param rng source of randomness for sampling. May be null.
   * @param z locations of point masses. May have duplicates;
   * need not be sorted.
   * @param w weight of each point mass. Need not be normalized.
   */

  public static final WEPDF make (final RandomGenerator rng,
                                  final double[] z,
                                  final double[] w) { 
    final int n = z.length;
    assert n == w.length;
    assert Statistics.isPositive(w) : Arrays.toString(w);
    final double[] z1 = Arrays.copyOf(z,n);
    final double[] w1 = Arrays.copyOf(w,n);
    Sorter.quicksort(z1,w1);
    // TODO: better messages with large arrays
    assert MathArrays.isMonotonic(
      z1, OrderDirection.INCREASING, false) :
        "not non-decreasing:\n" + Arrays.toString(z1);
    // compact ties in z
    int i = 0;
    double zi = z1[i];
    double wi = w1[i];
    for (int j=1;j<n;j++) {
      // check for continuing ties in z
      final double zj = z1[j];
      final double wj = w1[j];
      if (zi == zj) {
        // tie, increment weight, move right counter
        wi += wj;
        w1[i] = wi; }
      else {
        // no tie, increment both counters
        // copy to left counter if needed
        i++;
        if (i != j) { 
          zi = zj; wi = wj; z1[i] = zj; w1[i] = wj; } } }

    // copy into shorter arrays if needed
    final double[] z2;
    final double[] w2;
    final int nn = i + 1;
    if (nn ==  n) { 
      z2 = z1; w2 = w1; }
    else { 
      z2 = Arrays.copyOf(z1,nn); w2 = Arrays.copyOf(w1,nn); } 
    
    Statistics.normalize(w2); 
    return new WEPDF(rng,z2,w2); }

  /** Create a weighted empirical distribution from
   * locations of point masses.
   * 
   * @param z locations of point masses
   * @param w weight of each point mass.
   */
  public static final WEPDF make (final double[] z,
                                  final double[] w) { 
    return make((RandomGenerator) null,z,w); }

  /** Create a weighted empirical distribution from
   * locations of point masses. Each point mass gets equal weight.
   * 
   * @param z locations of point masses
   */
  public static final WEPDF make (final double[] z) {
    final double[] w = new double[z.length];
    Arrays.fill(w,1.0);
    return make(z,w); }

  /** Create a weighted empirical distribution from
   * a weighted empirical cumulative distribution.
   */
  public static final WEPDF make (final RandomGenerator rng,
                                  final WECDF d) {
    return make(rng,d.getZ(),Statistics.differences(d.getW())); }

  /** Create a weighted cumulative empirical distribution from
   * a weighted empirical (non-cumulative) distribution.
   */
  public static final WEPDF make (final WECDF d) {
    return make((RandomGenerator) null,d); }

  /** Create a weighted cumulative empirical distribution from
   * locations of point masses.
   * 
   * @param z sorted unique locations of point masses.
   * @param w normalized weight of each point mass. 
   */
  public static final WEPDF 
  sortedAndNormalized (final RandomGenerator rng,
                       final double[] z,
                       final double[] w) { 
    return new WEPDF(rng,z,w); }

  /** Create a weighted cumulative empirical distribution from
   * locations of point masses.
   * 
   * @param z sorted unique locations of point masses.
   * @param w normalized weight of each point mass. 
   */
  public static final WEPDF 
  sortedAndNormalized (final double[] z,
                       final double[] w) { 
    return sortedAndNormalized((RandomGenerator) null,z,w); }
  
  /** Return the average distribution (convex sum with equal 
   * weight for each distribution, not equal weight for each 
   * datum on each distribution).
   */
  public static final WEPDF average (final List wepdfs) {
    int n=0;
    for (final Object x : wepdfs) {
      n += ((WEPDF) x).z.length; }
    final double[] zz = new double[n];
    final double[] ww = new double[n];
    int i=0;
    for (final Object x : wepdfs) {
      final WEPDF wepdf = (WEPDF) x;
      final double[] zi = wepdf.z;
      final double[] wi = wepdf.w;
      final int ni = zi.length;
      System.arraycopy(zi,0,zz,i,ni);
      System.arraycopy(wi,0,ww,i,ni);
      i += ni; }
    return make(zz,ww); }
  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------
