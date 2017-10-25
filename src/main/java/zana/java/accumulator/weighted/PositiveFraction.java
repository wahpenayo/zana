package zana.java.accumulator.weighted;

//------------------------------------------------------------------------------
/** Fraction of case weights corresponding to positive class for binary 
 * classification/probability.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2017-10-24
 */

public final class

PositiveFraction

extends BinaryAccumulatorBase {

  //----------------------------------------------------------------------------
  // Accumulator interface
  //----------------------------------------------------------------------------
  /** Default fraction with no data is 0.0 --- ('negative') class wins.
   */

  @Override
  public final double doubleValue () {
    
    if (0.0 >= netWeight()) { // edge case
      assert 0 <= netCount();
      assert 0.0 == positiveWeight();
      return 0.0; }
    
    // normal case
    assert 0 < netCount();
    assert 0.0 <= positiveWeight();
    assert positiveWeight() <= netWeight();
    return positiveWeight() / netWeight(); }
  
  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  public PositiveFraction () { super(); }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------
