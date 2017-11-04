package zana.java.prob;

import java.util.Arrays;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.MathArrays;
import org.apache.commons.math3.util.MathArrays.OrderDirection;

import zana.java.arrays.Sorter;
import zana.java.math.Statistics;

//----------------------------------------------------------------
/** Weighted empirical probability density, a collection of point
 * masses, represented by the cdf, a non-decreasing right
 * continuous step function.
 * <ul>
 * <li><code>z</code> sorted unique domain values.
 * <li><code>w</code> normalized cumulative weights.
 * </ul>
 * Probability of <code>z==z[i]</code> is <code>w[i]</code>.
 * <p>
 * TODO: could use 
 * org.apache.commons.math3.distribution.EnumeratedRealDistribution,
 * but that converts data to List<Pair<Double,Double>>, which
 * like hurts significantly in space and time.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2017-11-04
 */

public final class WECDF extends AbstractRealDistribution 
implements ApproximatelyEqual {

  private static final long serialVersionUID = 1L;

  private final double[] z;
  /** Locations of point masses.
   */
  public final double[] getZ () { 
    return Arrays.copyOf(z,z.length); }

  private final double[] w;
  /** Cumulative weight: <code>w[i] = \sum_0^i mass[i]</CODE>.
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
    if (0 == i) { return w[0]; }
    return w[i] - w[i-1]; }

  @Override
  public final double cumulativeProbability (final double x) {
    final int i = Arrays.binarySearch(z, x);
    final int j;
    if (0 <= i) { j = i; }
    else { j = -2 - i; }
    if (-1 == j) { return 0.0; }
    return w[j]; }

  @Override
  public final double inverseCumulativeProbability (final double p) {
    assert ((0.0 <= p) && (p <= 1.0));
    if (0.0 == p) { return Double.NEGATIVE_INFINITY; }
    if (1.0 == p) { return z[z.length-1]; }
    final int i = Arrays.binarySearch(w,p);
    if (0 <= i) { return z[i]; }
    final int j = -1 - i; 
    if (0 > j) { return Double.NEGATIVE_INFINITY; }
    if (0 == j) { return z[0]; }
    if ((p - w[j-1]) < Math.ulp(1.0)) { return z[j-1]; }
    return z[j]; }

  @Override
  public final double density (final double x) {
    throw new UnsupportedOperationException(
      "density" + " unsupported for " + getClass()); }

  @Override
  public final double getNumericalMean () {
    // TODO: use fused-multiply-add in JDK9 for more accurate value
    // TODO: cache value?
    double w0 = w[0];
    double s = z[0]*w0;
    double c = 0.0;
    for (int i=1;i<z.length;i++) {
      final double w1 = w[i];
      final double zi = z[i]*(w1 - w0) - c;
      w0 = w1;
      final double t = s + zi;
      c = (t - s) - zi;
      s = t; 
    } 
    return s; }

  @Override
  public final double getNumericalVariance () {
    // TODO: Chan algorithm?
    // TODO: cache value?
    final double mean = getNumericalMean();
    double w0 = w[0];
    double s = w0*(z[0]-mean)*(z[0]-mean);
    double c = 0.0;
    for (int i=0;i<z.length;i++) {
      final double dz = z[i] - mean;
      final double w1 = w[i];
      final double zi = dz*dz*(w1-w0) - c;
      w0 = w1;
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
    if (! (that instanceof WECDF)) { return false; }
    return 
      Statistics.approximatelyEqual(z,((WECDF) that).z) 
      &&
      Statistics.approximatelyEqual(w,((WECDF) that).w); }

  @Override
  public final boolean approximatelyEqual (final double delta,
                                           final ApproximatelyEqual that) {
    if (! (that instanceof WECDF)) { return false; }
    return 
      Statistics.approximatelyEqual(delta,z,((WECDF) that).z) 
      &&
      Statistics.approximatelyEqual(delta,w,((WECDF) that).w); }

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
    if (! (that instanceof WECDF)) { return false; }
    if (! Arrays.equals(z,((WECDF) that).z)) { return false; }
    if (! Arrays.equals(w,((WECDF) that).w)) { return false; }
    return true; }

  // TODO: fix for large arrays
  @Override
  public final String toString () {
    return "(WECDF \n"
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
   * <li> Elements of <code>zz</code>  and <code>ww</code> are 
   * strictly increasing.
   * <li> Elements of <code>zz</code> are finite 
   * (not <code>NaN</code> or infinite).
   * <li> Elements of <code>ww</code> are between 0.0 and 1.0 ,
   * both inclusive.
   * <li> <code>ww[ww.length-1]) == 1.0</code> (within about
   * <code>Math.ulp(1.0)</code>.
   */

  private WECDF (final RandomGenerator rng,
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
      assert MathArrays.isMonotonic(
        ww, OrderDirection.INCREASING, true) :
          "not increasing:\n" + Arrays.toString(ww);
        assert Statistics.hasConvexElements(ww) :
          "not convex: " + Arrays.toString(ww);

        z = zz;
        w = ww; }

  //--------------------------------------------------------------
  /** Create a weighted cumulative empirical distribution from
   * locations of point masses.
   * 
   * @param rng source of randomness for sampling. May be null.
   * @param z0 locations of point masses. May have duplicates;
   * need not be sorted.
   * @param w0 weight of each point mass. Need not be normalized.
   */
  public static final WECDF make (final RandomGenerator rng,
                                  final double[] z0,
                                  final double[] w0) {
    // sort on z
    final int n = z0.length;
    assert n == w0.length;
    assert Statistics.isPositive(w0) : Arrays.toString(w0);
    final double[] z1 = Arrays.copyOf(z0,n);
    final double[] w1 = Arrays.copyOf(w0,n);
    Sorter.quicksort(z1,w1);
    // TODO: better messages with large arrays
    assert MathArrays.isMonotonic(z1, OrderDirection.INCREASING, false) :
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

    Statistics.normalizeCumulativeSums(w2); 
    return new WECDF(rng,z2,w2); }

  /** Create a weighted cumulative empirical distribution from
   * locations of point masses.
   * 
   * @param z0 locations of point masses. May have duplicates;
   * need not be sorted.
   * @param w0 weight of each point mass. Need not be normalized.
   */
  public static final WECDF make (final double[] z,
                                  final double[] w) { 
    return make((RandomGenerator) null,z,w); }

  /** Create a weighted cumulative empirical distribution from
   * locations of point masses. Each point mass gets equal weight.
   * 
   * @param z0 locations of point masses. May have duplicates;
   * need not be sorted.
   */
  public static final WECDF make (final double[] z) {
    final double[] w = new double[z.length];
    Arrays.fill(w,1.0);
    return make(z,w); }

  /** Create a weighted cumulative empirical distribution from
   * a weighted empirical (non-cumulative) distribution.
   */
  public static final WECDF make (final RandomGenerator rng,
                                  final WEPDF d) {
    return make(rng,d.getZ(),d.getW()); }

  /** Create a weighted cumulative empirical distribution from
   * a weighted empirical (non-cumulative) distribution.
   */
  public static final WECDF make (final WEPDF d) { 
    return make(null,d); }

  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------
