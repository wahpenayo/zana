package zana.java.accumulator.weighted;

//------------------------------------------------------------------------------
/** Gini impurity for binary classification/probability, with case weights.
 *
 * @author John Alan McDonald
 * @version 2016-11-29
 */

public final class GiniImpurity extends BinaryAccumulatorBase {

  //----------------------------------------------------------------------------
  // Accumulator interface
  //----------------------------------------------------------------------------

  @Override
  public final double doubleValue () {
    
    
    if (0.0 >= netWeight()) { // edge case
      assert 0 <= netCount();
      assert 0.0 >= positiveWeight();
      return 0.0; }
    
    else { // usual case
      assert 0 < netCount();
      assert NEGATIVE_WEIGHT_BOUND <= positiveWeight()
        : ":positiveWeight is too negative: " + positiveWeight();
      assert NEGATIVE_WEIGHT_BOUND <= (netWeight() - positiveWeight())
        : "netWeight - positiveWeight is too negative: " + 
        (netWeight() - positiveWeight());
        
      return 
        (Math.max(0.0, positiveWeight()) 
          * Math.max(0.0, netWeight() - positiveWeight())) 
        / netWeight(); } }
  
  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  public GiniImpurity () { super(); }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------
