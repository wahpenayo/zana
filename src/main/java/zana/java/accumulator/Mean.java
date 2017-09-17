package zana.java.accumulator;

//------------------------------------------------------------------------------
/** Unweighted mean.
 *
 * @author John Alan McDonald
 * @version 2016-11-29
 */

public final class

Mean extends SumBase {

  //----------------------------------------------------------------------------
  // Accumulator interface
  //----------------------------------------------------------------------------
  /** Default with no data is 0.0.
   */
  
  @Override
  public final double doubleValue () { 

    if (0.0 == netCount()) { // edge case
      assert 0.0 == sum();
      return 0.0; }
    else { // normal case
      assert 0 < netCount();
      return sum() / netCount(); } }
  
  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  public Mean () { super(); }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------
