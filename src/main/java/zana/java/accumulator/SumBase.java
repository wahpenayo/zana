package zana.java.accumulator;

//------------------------------------------------------------------------------
/** Statistics based on the weighted sum of the zs.
 *
 * @author John Alan McDonald
 * @version 2017-01-04
 */

public abstract class SumBase extends AccumulatorBase {

  //----------------------------------------------------------------------------
  // slots
  //----------------------------------------------------------------------------
  /** Mutable! Not Synchronized!
   */
  private double _sum;
  
  /** Mutable! Not Synchronized!
   */
  private double _correction;
  
  public final double sum () { return _sum; }
  
  private final void incrementSum (final double z) { 
    
    final double z0 = z - _correction;
    final double z1 = _sum + z0;
    _correction = (z1 - _sum) - z0;
    _sum = z1; }
  
  private final void decrementSum (final double z) { incrementSum(-z); }

  @Override
  public final void clear () { super.clear(); _sum = 0.0; }
  
  //----------------------------------------------------------------------------
  // Accumulator interface
  //----------------------------------------------------------------------------

  @Override
  public final void add (final double z) {
    incrementNetCount(); 
    incrementSum(z); }
  
  @Override
  public final void add (final Object z) {
    if (z instanceof Number) { add(((Number) z).doubleValue()); }
    else { 
      throw new UnsupportedOperationException(
        getClass().getName() + ".add(" + z.getClass().getName() + ")"); } }

  @Override
  public final void delete (final double z) {
    decrementNetCount(); 
    decrementSum(z); }

  @Override
  public final void delete (final Object z) {
    if (z instanceof Number) { delete(((Number) z).doubleValue()); }
    else { 
      throw new UnsupportedOperationException(
        getClass().getName() + ".delete(" + z.getClass().getName() + ")"); } }

  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  public SumBase () { super(); _sum = 0.0; }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------
