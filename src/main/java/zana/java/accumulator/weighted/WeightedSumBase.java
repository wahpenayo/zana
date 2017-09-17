package zana.java.accumulator.weighted;

//------------------------------------------------------------------------------
/** Statistics based on the weighted sum of the zs.
 *
 * @author John Alan McDonald
 * @version 2017-01-03
 */

public abstract class

WeightedSumBase

extends AccumulatorBase {

  //----------------------------------------------------------------------------
  // slots
  //----------------------------------------------------------------------------
  /** Mutable!
   */
  private double _sum;
  
  /** Mutable!
   */
  private double _correction;
  
  public final double sum () { return _sum; }
  
  private final void incrementSum (final double wz) { 
    
    final double wz0 = wz - _correction;
    final double wz1 = _sum + wz0;
    _correction = (wz1 - _sum) - wz0;
    _sum = wz1; }
  
  private final void decrementSum (final double wz) { incrementSum(-wz); }

  @Override
  public final void clear () { super.clear(); _sum = 0.0; }
  
  //----------------------------------------------------------------------------
  // Accumulator interface
  //----------------------------------------------------------------------------

  @Override
  public final void add (final double z) {
    incrementNetCount();
    incrementNetWeight();
    incrementSum(z); }
  
  @Override
  public final void add (final double z, final double w) {
    incrementNetCount();
    incrementNetWeight(w);
    incrementSum(w*z); }
  
  @Override
  public final void add (final Object z) {
    throw new UnsupportedOperationException(
      getClass().getName() + ".add(" + z.getClass().getName() + ")"); } 

  @Override
  public final void delete (final double z) {
    decrementNetCount();
    decrementNetWeight();
    decrementSum(z); }

  @Override
  public final void delete (final double z, final double w) {
    decrementNetCount();
    decrementNetWeight(w);
    decrementSum(w*z); }

  @Override
  public final void delete (final Object z) {
    throw new UnsupportedOperationException(
      getClass().getName() + ".delete(" + z.getClass().getName() + ")"); } 

  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  public WeightedSumBase () { super(); _sum = 0.0; }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------
