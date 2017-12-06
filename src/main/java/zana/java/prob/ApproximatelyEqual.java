package zana.java.prob;


/** Objects parameterized with <code>double</code> will often not
 * be exactly equal, but need,at least for testing, some
 * notion of 'close enough'.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2017-12-06
 */
public interface ApproximatelyEqual {

  //--------------------------------------------------------------
  // interface methods
  //--------------------------------------------------------------

  /** Corresponding parameters are close enough.
   */
  boolean approximatelyEqual (ApproximatelyEqual that);

  /** Corresponding parameters are within <code>delta</code>, in
   * some sense.
   */

  boolean approximatelyEqual (float delta,
                              ApproximatelyEqual that);

  /** Corresponding parameters are within <code>delta</code>, in
   * some sense.
   */

  boolean approximatelyEqual (double delta,
                              ApproximatelyEqual that);

  //--------------------------------------------------------------
}
