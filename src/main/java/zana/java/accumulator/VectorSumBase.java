package zana.java.accumulator;

import java.util.Arrays;

//------------------------------------------------------------------------------
/** Statistics based on the unweighted sum of the zs.
 *
 * TODO: have an explicit codomain vector space object, used to generate mutable 
 * _sum and _corrrection instances, and determine what add means.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2017-10-31
 */

public abstract class VectorSumBase extends AccumulatorBase {

  //----------------------------------------------------------------------------
  // slots
  //----------------------------------------------------------------------------
  
  private final int _dimension;
  public final int dimension () { return _dimension; }

  /** Mutable! Not synchronized! Public visibility!
   */
  private final double[] _sum;
  public final double[] sum () { return _sum; }

  /** Mutable! Not synchronized! Public visibility!
   */
  private final double[] _correction;

  private final void incrementSum (final double[] z) { 
    for (int i=0;i<_dimension;i++) {
      final double z0 = z[i] - _correction[i];
      final double si = _sum[i];
      final double z1 = si + z0;
      _correction[i] = (z1 - si) - z0;
      _sum[i] = z1; } }

  private final void incrementSum (final float[] z) { 
    for (int i=0;i<_dimension;i++) {
      final double z0 = z[i] - _correction[i];
      final double si = _sum[i];
      final double z1 = si + z0;
      _correction[i] = (z1 - si) - z0;
      _sum[i] = z1; } }

  private final void decrementSum (final double[] z) { 
    for (int i=0;i<_dimension;i++) {
      final double z0 = (-z[i]) - _correction[i];
      final double si = _sum[i];
      final double z1 = si + z0;
      _correction[i] = (z1 - si) - z0;
      _sum[i] = z1; } }

  private final void decrementSum (final float[] z) { 
    for (int i=0;i<_dimension;i++) {
      final double z0 = (-((double) z[i])) - _correction[i];
      final double si = _sum[i];
      final double z1 = si + z0;
      _correction[i] = (z1 - si) - z0;
      _sum[i] = z1; } }

  @Override
  public final void clear () { 
    super.clear(); 
    Arrays.fill(_sum,0.0);
    Arrays.fill(_correction,0.0); }

  //----------------------------------------------------------------------------
  // Accumulator interface
  //----------------------------------------------------------------------------

  @Override
  public final void add (final double z) {
    throw new UnsupportedOperationException(
      getClass().getName() + ".add(double)"); }

  private final void add (final double[] z) {
    incrementNetCount();
    incrementSum(z); }

  private final void add (final float[] z) {
    incrementNetCount();
    incrementSum(z); }

  @Override
  public final void add (final Object z) {
    if (z instanceof double[]) { add((double[]) z); }
    else if (z instanceof float[]) { add((float[]) z); }
    else { 
      throw new UnsupportedOperationException(
        getClass().getName() + ".add(" + z.getClass().getName() + ")"); } }

  //----------------------------------------------------------------------------

  @Override
  public final void delete (final double z) {
    throw new UnsupportedOperationException(
      getClass().getName() + ".delete(double)"); }

  private final void delete (final float[] z) {
    decrementNetCount();
    decrementSum(z); }

  private final void delete (final double[] z) {
    decrementNetCount();
    decrementSum(z); }

  @Override
  public final void delete (final Object z) {
    if (z instanceof double[]) { delete((double[]) z); }
    else if (z instanceof float[]) { delete((float[]) z); }
    else { 
      throw new UnsupportedOperationException(
        getClass().getName() + ".delete(" + z.getClass().getName() + ")"); } }

  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  public VectorSumBase (final int dimension) { 
    super();
    _dimension = dimension;
    _sum = new double[_dimension];
    _correction = new double[_dimension]; }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------
