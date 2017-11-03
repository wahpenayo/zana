package zana.java.accumulator;

//----------------------------------------------------------------
/** Kahan summation.
 *
 * @author wahpenayo at gmail dot com
 * @version 2017-11-02
 */

public final class

Sum extends SumBase {

  //--------------------------------------------------------------
  // Accumulator interface
  //--------------------------------------------------------------
  /** Default with no data is 0.0.
   */
  
  @Override
  public final double doubleValue () { 
    if (0.0 == netCount()) { // edge case
      assert 0.0 == sum();
      return 0.0; }
    assert 0 < netCount();
    return sum(); }
  
  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  public Sum () { super(); }

  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------
