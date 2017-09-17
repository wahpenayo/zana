package zana.java.accumulator;

//------------------------------------------------------------------------------
/** Gini impurity for binary classification/probability, with case weights.
 *
 * @author John Alan McDonald
 * @version 2016-12-16
 */

public final class GiniImpurity extends BinaryAccumulatorBase {

  //----------------------------------------------------------------------------
  // Accumulator interface
  //----------------------------------------------------------------------------

  @Override
  public final double doubleValue () {
    
    
    if (0 == netCount()) { // edge case
      assert 0 == positiveCount();
      return 0.0; }
    
    else { // usual case
      assert 0 < netCount();
      assert 0 <= positiveCount();
      assert positiveCount() <= netCount();
        
      return ((double) (positiveCount() * (netCount() - positiveCount())))
        / netCount(); } }
  
  @Override
  public final double minimum () { return 0.0; }

  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  public GiniImpurity () { super(); }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------
