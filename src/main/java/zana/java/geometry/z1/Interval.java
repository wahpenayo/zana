package zana.java.geometry.z1;

/** An immutable interval on the integers.
 * @author John Alan McDonald
 * @version 2016-09-13
 */

@SuppressWarnings("unchecked")
public final class Interval implements Comparable, zana.java.geometry.Interval {
  
  private final long _z0;
  public final long z0 () { return _z0; }
  private final long _z1;
  public final long z1 () { return _z1; }
 
  //----------------------------------------------------------------------------
  // Interval
  //----------------------------------------------------------------------------
  public final boolean contains (final long x) { 
    return (z0() <= x) && (x < z1()); }
  
  // only exact integer values accepted
  public final boolean contains (final double x) { 
    final long l = (long) x;
    return (x == ((double) l)) && contains(l); }
  
  public final boolean contains (final Object o) {
    if (o instanceof Long) { 
      return contains(((Long) o).longValue()); }
    if (o instanceof Double) {
      return contains(((Double) o).doubleValue()); }
    if (o instanceof Integer) {
      return contains(((Integer) o).longValue()); }
    if (o instanceof Float) {
      return contains(((Float) o).doubleValue()); }
    if (o instanceof Short) {
      return contains(((Short) o).longValue()); }
    if (o instanceof Byte) {
      return contains(((Byte) o).longValue()); } 
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
    return String.format("[%2.4g,%2.4g)", Long.valueOf(_z0), Long.valueOf(_z1)); }
  @Override
  public final int hashCode () {
    final int c0 = (int) (_z0 ^ (_z0 >>> 32));
    final int c1 = (int) (_z1 ^ (_z1 >>> 32));
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
  private Interval (final long z0, final long z1) {
    assert z0 < z1 : "Error: " + z0 + " not less than " + z1;
    _z0 = z0;
    _z1 = z1;  }
  //----------------------------------------------------------------------------
  public static final Interval make (final long z0, final long z1) {
    return new Interval(z0,z1); }
  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------