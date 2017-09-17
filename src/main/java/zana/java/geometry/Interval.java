package zana.java.geometry;

/** An generic interval (incomplete).
 * @author John Alan McDonald
 * @version 2016-07-29
 */

public interface Interval {

  boolean contains (Object o);
  boolean contains (double x);
  boolean contains (long x);
}
