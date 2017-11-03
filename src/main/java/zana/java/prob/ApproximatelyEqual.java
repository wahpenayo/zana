package zana.java.prob;


/** Objects parameterized with <code>double</code> will often not
 * be exactly equal, but need,at least for testing, some
 * notion of 'close enough'.
 * 
 * @author wahpenayo at gmail dot com
 * @since 2017-11-02
 * @version 2017-11-02
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

  boolean approximatelyEqual (double delta,
                              ApproximatelyEqual that);

  //--------------------------------------------------------------
}
