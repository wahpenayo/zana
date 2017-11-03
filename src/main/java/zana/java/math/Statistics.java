package zana.java.math;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Iterator;
import clojure.lang.IFn;

/** Summary statistics.
 *
 * @author wahpenayo at gmail dot com
 * @version 2015-08-23
 */

public final class Statistics {
  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------
  /** Naive sum of elements of <code>z</code>,
   *  from <code>start</code> (inclusive) 
   * up to <code>end</code> (exclusive).
   */
  public static final double naiveSum (final double[] z, 
                                       final int start,
                                       final int end) {
    assert start >= 0;
    assert end <= z.length;
    double s = 0.0;
    for (int i=start;i<end;i++) { s += z[i]; }
    return s; }

  /** Naive sum of elements of <code>z</code>.
   */
  public static final double naiveSum (final double[] z) {
    return naiveSum(z,0,z.length); }
  //--------------------------------------------------------------
  /** Kahan sum of elements of <code>z</code>,
   *  from <code>start</code> (inclusive) 
   * up to <code>end</code> (exclusive).
   */
  public static final double kahanSum (final double[] z, 
                                       final int start,
                                       final int end) {
    assert 0 <= start;
    assert start < end;
    assert end <= z.length;
    double s = 0.0;
    double c = 0.0;
    for (int i=start;i<end;i++) {
      final double zi = z[i] - c;
      final double t = s + zi;
      c = (t - s) - zi;
      s = t; } 
    return s; }

  /** Kahan sum of elements of <code>z</code>.
   */
  public static final double kahanSum (final double[] z) {
    return kahanSum(z,0,z.length); }
  //--------------------------------------------------------------

  public static final boolean approximatelyEqual (
                                                  final double delta,
                                                  final double z0,
                                                  final double z1) {
    return Math.abs(z0-z1) <= delta; }

  public static final boolean approximatelyEqual (final double z0,
                                                  final double z1) {
    final double delta = Math.ulp(10.0 * (Math.abs(z0) + Math.abs(z1)));
    return Math.abs(z0-z1) <= delta; }

  public static final boolean approximatelyEqual (final double delta,
                                                  final double[] z0,
                                                  final double[] z1) {
    final int n = z0.length;
    if (n != z1.length) { return false; }
    for (int i=0;i<n;i++) {
      if (! approximatelyEqual(z0[i],z1[i],delta)) { return false; } }
    return true; }

  public static final boolean approximatelyEqual (final double[] z0,
                                                  final double[] z1) {
    final int n = z0.length;
    if (n != z1.length) { return false; }
    for (int i=0;i<n;i++) {
      if (! approximatelyEqual(z0[i],z1[i])) { return false; } }
    return true; }

  //--------------------------------------------------------------
  public static final void normalize (final double[] w) {
    final int n = w.length;
    final double s = kahanSum(w);
    for (int i=0;i<n;i++ ) { w[i] = w[i] / s; } }
  public static final double[] normalized (final double[] w) {
    final int n = w.length;
    final double[] u = new double[n];
    final double s = kahanSum(w);
    for (int i=0;i<n;i++ ) { u[i] = w[i] / s; } 
    return u; }
  //--------------------------------------------------------------
  public static final boolean isFinite (final double[] w) {
    for (int i=0;i<w.length;i++) {
      if (! Double.isFinite(w[i])) { return false; } }
    return true; }
  //--------------------------------------------------------------
  public static final boolean isNonnegative (final double[] w) {
    for (int i=0;i<w.length;i++) {
      if (0 > w[i]) { return false; } }
    return true; }
  //--------------------------------------------------------------
  public static final boolean isPositive (final double[] w) {
    for (int i=0;i<w.length;i++) {
      if (0 >= w[i]) { return false; } }
    return true; }
  //--------------------------------------------------------------
  public static final boolean isConvex (final double[] w) {
    for (int i=0;i<w.length;i++) {
      final double wi = w[i];
      if (0.0 > wi) { return false; }
      if (wi > 1.0) { return false; } }
    return approximatelyEqual(1.0,kahanSum(w)); }
  //--------------------------------------------------------------
  public static final double presortedCDF (final double[] x,
                                           final double q) {
    final int i = Arrays.binarySearch(x, q);
    final int n = x.length;
    if (0 <= i) { // exact match, check upwards to get max matching sample
      int k = i;
      while (((k + 1) < n) && (q == x[k + 1])) { k++; }
      return (k + 1.0) / n; }
    // no exact match
    return -(i + 1.0) / n; }
  //--------------------------------------------------------------
  /** Used in dealing with rounding errors.
   * See Hyndman's R implementation of quantile().
   */
  private static final double FUZZ = 4.0 * Math.ulp(1.0);
  private static final double ONE_THIRD = 1.0 / 3.0;
  //--------------------------------------------------------------
  /** Return the <code>p</code>th quantile of <code>x</code>,
   * using Hyndman-Fan definition 8.
   * <p>
   * Does not modify <code>x</code>.<br>
   * Assumes inputs are valid: <code>x[i]</code> and <code>p</code> are all
   * finite, <code>x.length</code> is at least 2, and <code>x</code> is sorted
   * increasing.
   * <p>
   * See Hyndman, R. J. and Fan, Y. (1996) <i>Sample quantiles in statistical
   * packages,</i> <b>American Statistician, 50,</b> 361-365.
   */
  public static final double presortedQuantile (final double[] x,
                                                final double p) {
    assert (0.0 <= p) && (p <= 1.0);
    if (0.0 == p) { return x[0]; }
    if (1.0 == p) { return x[x.length - 1]; }
    final int n = x.length;
    final double m = ONE_THIRD + (p * ONE_THIRD);
    final double np = n * p;
    final double nppm = np + m;
    final double floorNppm = Math.floor(nppm + FUZZ);
    final double g0 = nppm - floorNppm;
    final double g = (Math.abs(g0) < FUZZ) ? 0.0 : g0;
    final int j = (int) floorNppm;
    final int jm1 = Math.min(n - 1, Math.max(0, j - 1));
    final int jm = Math.min(n - 1, j);
    return ((1.0 - g) * x[jm1]) + (g * x[jm]); }
  //--------------------------------------------------------------
  /** Return the [xmin,xmax] X [ymin,ymax] rectangle.
   */
  public static final Rectangle2D.Double bounds (final IFn.OD xf,
                                                 final IFn.OD yf,
                                                 final Iterable data) {
    double xmin = Double.POSITIVE_INFINITY;
    double ymin = Double.POSITIVE_INFINITY;
    double xmax = Double.NEGATIVE_INFINITY;
    double ymax = Double.NEGATIVE_INFINITY;
    final Iterator it = data.iterator();
    while (it.hasNext()) {
      final Object datum = it.next();
      final double x = xf.invokePrim(datum);
      if (x < xmin) { xmin = x; }
      if (x > xmax) { xmax = x; }
      final double y = yf.invokePrim(datum);
      if (y < ymin) { ymin = y; }
      if (y > ymax) { ymax = y; } }
    assert xmin <= xmax;
    assert ymin <= ymax;
    return new Rectangle2D.Double(xmin,ymin,xmax-xmin,ymax-ymin); }
  //--------------------------------------------------------------
  /** Return a square: [min(xmin,ymin),max(xmax,ymax)]^2.
   */
  public static final Rectangle2D.Double 
  symmetricBounds (final IFn.OD xf,
                   final IFn.OD yf,
                   final Iterable data) {
    double zmin = Double.POSITIVE_INFINITY;
    double zmax = Double.NEGATIVE_INFINITY;
    final Iterator it = data.iterator();
    while (it.hasNext()) {
      final Object datum = it.next();
      final double x = xf.invokePrim(datum);
      if (x < zmin) { zmin = x; }
      if (x > zmax) { zmax = x; }
      final double y = yf.invokePrim(datum);
      if (y < zmin) { zmin = y; }
      if (y > zmax) { zmax = y; } }
    assert zmin <= zmax;
    final double dz = zmax - zmin;
    return new Rectangle2D.Double(zmin,zmin,dz,dz); }
  //--------------------------------------------------------------
  // disabled constructor
  //--------------------------------------------------------------
  private
  Statistics () {
    super();
    throw new UnsupportedOperationException(
      getClass() + " is not instantiable."); }
  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------