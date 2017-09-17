package zana.java.geometry.r1;

/** An interval on the real line.
 * @author John Alan McDonald
 * @version 2016-09-13
 */

@SuppressWarnings("unchecked")
public final class
Interval
extends Object
implements Comparable {//, clojure.lang.Counted, clojure.lang.Indexed {
  
  private final double _z0;
  public final double z0 () { return _z0; }
  private final double _z1;
  public final double z1 () { return _z1; }
  //----------------------------------------------------------------------------
  // Interval
  //----------------------------------------------------------------------------
  public final boolean contains (final long x) { 
    return (z0() <= x) && (x < z1()); }

  public final boolean contains (final double x) { 
    return (z0() <= x) && (x < z1()); }

  public final boolean contains (final Object o) {
    if (o instanceof Number) { 
      return contains(((Number) o).doubleValue()); }
    return false; }
  //----------------------------------------------------------------------------
  // Comparable
  //----------------------------------------------------------------------------
  @Override
  public final int compareTo (final Object o) {
    assert (o instanceof Interval);
    final Interval that = (Interval) o;
    if (z0() < that.z0()) { return -1; }
    if (z0() > that.z0()) { return  1; }
    if (z1() < that.z1()) { return -1; }
    if (z1() > that.z1()) { return  1; }
    return 0; }
  //----------------------------------------------------------------------------
  // Object
  //----------------------------------------------------------------------------
  @Override
  public final String toString () {
    return
      String.format("[%2.4g,%2.4g)", Double.valueOf(_z0),Double.valueOf(_z1)); }
  @Override
  public final int hashCode () {
    final long l0  = Double.doubleToLongBits(_z0);
    final int c0 = (int) (l0 ^ (l0 >>> 32));
    final long l1  = Double.doubleToLongBits(_z1);
    final int c1 = (int) (l1 ^ (l1 >>> 32));
    int c = 17;
    c += 37*c0;
    c += 37*c1;
    return c; }
  @Override
  public final boolean equals (final Object o) {
    if (this == o) { return true; }
    if (! (o instanceof Interval)) { return false; }
    final Interval i = (Interval) o;
    return (_z0 == i.z0()) && (_z1 == i.z1()); }
  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------
  private Interval (final double z0, final double z1) {
    assert z0 < z1 : "Error: " + z0 + " not less than " + z1;
    _z0 = z0;
    _z1 = z1;  }
  //----------------------------------------------------------------------------
  public static final Interval make (final double z0, final double z1) {
    return new Interval(z0,z1); }
  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------