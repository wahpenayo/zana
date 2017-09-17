package zana.java.accumulator;

//------------------------------------------------------------------------------
/** Fraction of case weights corresponding to positive class for binary 
 * classification/probability.
 *
 * @author John Alan McDonald
 * @version 2016-11-29
 */

public final class

PositiveFraction extends BinaryAccumulatorBase {

  //----------------------------------------------------------------------------
  // Accumulator interface
  //----------------------------------------------------------------------------
  /** Default fraction with no data is 0.0 --- ('negative') class wins.
   */

  @Override
  public final double doubleValue () {
    
    if (0.0 >= netCount()) { // edge case
      assert 0 <= netCount();
      assert 0.0 == positiveCount();
      return 0.0; }
    
    else { // normal case
      assert 0 < netCount();
      assert 0.0 <= positiveCount();
      assert positiveCount() <= netCount();
      return ((double) positiveCount()) / netCount(); } }
  
  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  public PositiveFraction () { super(); }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------
