package zana.java.accumulator.weighted;

//------------------------------------------------------------------------------
/** Weighted mean.
 * <p>
 * TODO: at least offer a more numerically stable, if slower, version.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2017-10-24
 */

public final class

Mean

extends WeightedSumBase {

  //----------------------------------------------------------------------------
  // Accumulator interface
  //----------------------------------------------------------------------------
  /** Default with no data is 0.0.
   */
  
  @Override
  public final double doubleValue () { 

    if (0.0 == netWeight()) { // edge case
      assert 0 <= netCount();
      assert 0.0 == sum();
      return 0.0; }
    // normal case
    assert 0 < netCount();
    assert 0.0 <= netWeight();
    return sum() / netWeight(); }
  
  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  public Mean () { super(); }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------
