package zana.java.accumulator;

//------------------------------------------------------------------------------
/** Gini impurity for binary classification/probability, with case weights.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2017-10-24
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
    assert 0 < netCount();
    assert 0 <= positiveCount();
    assert positiveCount() <= netCount();
      
    return ((double) (positiveCount() * (netCount() - positiveCount())))
      / netCount(); }
  
  @Override
  public final double minimum () { return 0.0; }

  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  public GiniImpurity () { super(); }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------
