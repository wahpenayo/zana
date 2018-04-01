package zana.test.java.geometry.functions;

import java.util.Arrays;
import java.util.List;

import clojure.lang.IFn;
import zana.java.geometry.Dn;
import zana.java.geometry.functions.Function;
import zana.java.geometry.functions.LinearFunctional;

/** Variance of L2 distance from <code>x</code> over a set of 
 * points.
 * Used for fitting a circle/sphere to a set of points in tests.
 * <p>
 * TODO: could re-write using sums and compositions of more 
 * elementary functions.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-04-01
 */

@SuppressWarnings("unchecked")
public final class L2DistanceVariance  extends Function  {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final double[][] _points;

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------
  // TODO: replace Kahan compensation with fully accurate sum?
  // TODO: these aren't right, even as compensated computation.

  private static final double l2Distance (final double[] p0,
                                          final double[] p1) {
    assert p0.length == p1.length;
    double s = 0.0;
    double c = 0.0;
    for (int i=0;i<p0.length;i++) {
      final double dp = p0[i] - p1[i];
      final double zi = dp*dp - c;
      final double t = s + zi;
      c = (t - s) - zi;
      s = t; }
    return Math.sqrt(s); }

  public final double meanL2Distance (final double[] x) {
    double s = 0.0;
    double c = 0.0;
    for (final double[] p : _points) {
      final double zi = l2Distance(x,p) - c;
      final double t = s + zi;
      c = (t - s) - zi;
      s = t; }
    return s / _points.length; }

  @Override
  public final double doubleValue (final double[] x) {
    final double mean = meanL2Distance(x);
    double s = 0.0;
    double c = 0.0;
    for (final double[] p : _points) {
      final double di = l2Distance(x,p) - mean;
      final double zi =  di*di - c;
      final double t = s + zi;
      c = (t - s) - zi;
      s = t; } 
    return s; }

  @Override
  public final Function derivativeAt (final double[] x) { 
    final double mean = meanL2Distance(x);
    double s0 = 0.0;
    double c0 = 0.0;
    double s1 = 0.0;
    double c1 = 0.0;
    for (final double[] p : _points) {
      final double dp = l2Distance(x,p);
      final double a = (dp - mean) / dp;
      final double z0 = a*(x[0] - p[0]) - c0;
      final double t0 = s0 + z0;
      c0 = (t0 - s0) - z0;
      s0 = t0;
      final double z1 = a*(x[1] - p[1]) - c1;
      final double t1 = s1 + z1;
      c1 = (t1 - s1) - z1;
      s1 = t1; }
    s0 *= 2.0;
    s1 *= 2.0;
        return LinearFunctional.make(new double[] { s0, s1 }); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  // TODO: move somewhere else
  
  private static final double[][] doubleArray2d (final List points) {
    final int m = points.size();
    final int n = ((List) points.get(0)).size();
    final double[][] a = new double[m][n];
    for (int i=0;i<m;i++) {
      final List row = (List) points.get(i);
      for (int j=0;j<n;j++) {
        a[i][j] = ((Number) row.get(j)).doubleValue(); } }
    return a; }

  private static final double[][] copy (final double[][] points) {
    final int m = points.length;
    final double[][] a = new double[m][];
    for (int i=0;i<m;i++) {
      a[i] = Arrays.copyOf(points[i],points[i].length); }
    return a; }

  //--------------------------------------------------------------

  private L2DistanceVariance (final double[][] points) {
    super(Dn.get(points[0].length),Dn.get(1));
    _points  = points; }

  public static final L2DistanceVariance make (final double[][] points) {
    return new L2DistanceVariance(copy(points)); }

  public static final L2DistanceVariance make (final Object points) {
    if (points instanceof double[][]) {
      return new L2DistanceVariance((double[][]) points); }
    if (points instanceof List) {
      return make(doubleArray2d((List) points)); }
    throw new IllegalArgumentException(
      "Not a double[][] or list of lists:" + points); }

  public static final L2DistanceVariance generate (final int m,
                                           final int n,
                                           final IFn.D g) {
    final double[][] points = new double[m][n];
    for (int i=0;i<m;i++) { 
      for (int j=0;j<n;j++) {
        points[i][j] = g.invokePrim(); } }
    return new L2DistanceVariance(points); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
