
package zana.java.accumulator;

//------------------------------------------------------------------------------
/** Minimum cost class for binary classification/probability, with case weights.
 *
 * @author John Alan McDonald
 * @version 2016-11-29
 */

public final class BinaryMinimumExpectedCostClass 
extends BinaryAccumulatorBase {

  //----------------------------------------------------------------------------
  // slots
  //----------------------------------------------------------------------------
  
  private final double _falsePositiveCost;
  public final double falsePositiveCost () { return _falsePositiveCost; }
  public final double falseNegativeCost () { return 1.0 - falsePositiveCost(); }
  
  //----------------------------------------------------------------------------
  // Accumulator interface
  //----------------------------------------------------------------------------
  /** Default vote with no data is 0.0 --- 'negative' class wins.
   */

  @Override
  public final double doubleValue () {
    
    if (0.0 == netCount()) { // edge case
      assert 0 <= netCount();
      assert 0 == positiveCount();
      return 0.0; }
    
    else { // normal case
      assert 0 < netCount();
      assert 0 <= positiveCount();
      assert positiveCount() <= netCount();
      return 
        (positiveCount() < (falsePositiveCost() * netCount())) ? 0.0 : 1.0; } }
  
  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  public BinaryMinimumExpectedCostClass (final double falsePositiveCost) { 
    super(); 
    assert 0.0 < falsePositiveCost && falsePositiveCost < 1.0; 
    _falsePositiveCost = falsePositiveCost; }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------
