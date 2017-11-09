package zana.java.prob;

import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.exception.NumberIsTooLargeException;

import zana.java.math.Statistics;

//----------------------------------------------------------------
/** Translation of another <code>RealDistribution</code>
 * moving the mass <code>delta</code> to the right.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2017-11-08
 */

public final class TranslatedRealDistribution  
implements ApproximatelyEqual, RealDistribution {

  private final double _dz;
  /** Amount of shift.
   */
  public final double getDz () { return _dz; }

  /** Distribution that was shifted.
   * <p>
   * <b>WARNING: <code>RealDistribution</code>s are mutable,
   * in particular when sampled.
   */
  private final RealDistribution _rd;

  // TOD: implement copying via reflection?
  /** Distribution that was shifted.
   * <p>
   * <b>WARNING: <code>RealDistribution</code>s are mutable,
   * in particular when sampled.
   */
  public final RealDistribution getRd () { 
    return _rd; }

  //--------------------------------------------------------------
  // RealDistribution interface
  //--------------------------------------------------------------

  @Override
  public final double probability (final double x) {
    return _rd.probability(x - _dz); }
  @Override
  public final double cumulativeProbability (final double x) {
    return _rd.cumulativeProbability(x -_dz); }
  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public final double cumulativeProbability (final double x0, 
                                             final double x1)
                                               throws NumberIsTooLargeException {
    return _rd.cumulativeProbability(x0 -_dz,x1 -_dz); }
  @Override
  public final double inverseCumulativeProbability (final double p) {
    return _rd.inverseCumulativeProbability(p) + _dz; }
  @Override
  public final double density (final double x) {
    return _rd.density(x - _dz); }
  @Override
  public final double getNumericalMean () {
    return _rd.getNumericalMean() + _dz; }
  @Override
  public final double getNumericalVariance () {
    return _rd.getNumericalVariance(); }
  @Override
  public final double getSupportLowerBound () { 
    return _rd.getSupportLowerBound() + _dz; }
  @Override
  public final double getSupportUpperBound () { 
    return _rd.getSupportUpperBound() + _dz; }
  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public final boolean isSupportLowerBoundInclusive () { 
    return _rd.isSupportLowerBoundInclusive(); }
  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public final boolean isSupportUpperBoundInclusive () { 
    return _rd.isSupportUpperBoundInclusive(); }
  @Override
  public final boolean isSupportConnected () { 
    return _rd.isSupportConnected(); }

  /** <b>WARNING:</b> modifies inner distribution!
   */
  @Override
  public final void reseedRandomGenerator (final long s) {
    _rd.reseedRandomGenerator(s); }

  /** <b>WARNING:</b> modifies inner distribution!
   */
  @Override
  public final double sample () {
    return _rd.sample() + _dz; }

  /** <b>WARNING:</b> modifies inner distribution!
   */
  @Override
  public final double[] sample (final int n) {
    final double[] s = _rd.sample(n);
    for (int i=0;i<s.length;i++) { s[i] += _dz; }
    return s; }

  //--------------------------------------------------------------
  // ApproximatelyEquals interface
  //--------------------------------------------------------------

  @Override
  public final boolean approximatelyEqual (final ApproximatelyEqual that) {
    if (! (that instanceof TranslatedRealDistribution)) { 
      return false; }
    return 
      Statistics.approximatelyEqual(
        _dz,((TranslatedRealDistribution) that)._dz)
      &&
      _rd.equals(((TranslatedRealDistribution) that)._rd); }

  @Override
  public final boolean approximatelyEqual (final double delta,
                                           final ApproximatelyEqual that) {
    if (! (that instanceof TranslatedRealDistribution)) { 
      return false; }
    return 
      Statistics.approximatelyEqual(
        _dz,((TranslatedRealDistribution) that)._dz,delta)
      &&
      _rd.equals(((TranslatedRealDistribution) that)._rd); }

  //--------------------------------------------------------------
  // Object interface
  //--------------------------------------------------------------

  @Override
  public final int hashCode () {
    int h = 17;
    final long l = Double.doubleToLongBits(_dz);
    h = (37*h) + ((int) (l ^ (l >>>32)));
    h = (37*h) + _rd.hashCode();
    return h; }

  @Override
  public final boolean equals (final Object that) {
    if (! (that instanceof TranslatedRealDistribution)) { 
      return false; }
    if (_dz != ((TranslatedRealDistribution) that)._dz) { 
      return false; }
    return _rd.equals(((TranslatedRealDistribution) that)._rd); }

  // TODO: fix for large arrays
  @Override
  public final String toString () {
    return "(TranslatedRealDistribution \n"
      + _dz + "\n" 
      + _rd.toString() + ")"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private TranslatedRealDistribution (final RealDistribution rd,
                                      final double dz) {
    _dz = dz;
    _rd = rd; }

  //--------------------------------------------------------------
  /** Translate the mass of <code>rd</code> <code>dz</code>units 
   * to the right. For example, 
   * <code>this.mean() = rd.mean() + dz</code>.
   */

  public static final TranslatedRealDistribution 
  shift (final RealDistribution rd,
         final double dz) {
    assert (null != rd);
    return new TranslatedRealDistribution(rd,dz); }
  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------
