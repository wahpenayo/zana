package zana.java.geometry.functions;

import java.io.PrintWriter;
import java.util.Arrays;

//----------------------------------------------------------------
/** Wrapper class that writes values and derivatives to
 * a PrintWriter.
 *
 * @author wahpenayo at gmail dot com
 * @version 2018-04-09
 */

@SuppressWarnings("unchecked")
public final class Tracer extends Function {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final Function _inner;
  private final PrintWriter _out;

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------

  private static final String toString (final Object y) {
    if (y instanceof double[]) {
      return Arrays.toString((double[]) y); }
    return y.toString(); }
  
  //--------------------------------------------------------------
  // Function methods
  //--------------------------------------------------------------

  @Override
  public final double doubleValue (final Object x) {
    final double y = _inner.doubleValue(x);
    System.out.println(_inner + " doubleValue at\n" + toString(x) + "\n= " + y + "\n"); 
    return y; }

  @Override
  public final double doubleValue (final double x) {
    final double y = _inner.doubleValue(x);
    System.out.println(_inner + " doubleValue at\n" + x + "\n= " + y + "\n"); 
    return y; }

  //--------------------------------------------------------------
  @Override
  public final Object value (final Object x) {
    final Object y = _inner.value(x);
    System.out.println(_inner + " value at\n" + toString(x) + "\n= " + toString(y) + "\n"); 
    return y; }

  @Override
  public final Object value (final double x) {
    final Object y = _inner.value(x);
    System.out.println(_inner + " value at\n" + x + "\n= " + toString(y) + "\n"); 
    return y; }

  //--------------------------------------------------------------
  @Override
  public final Function derivativeAt (final double x) {
    final Function df = _inner.derivativeAt(x);
    System.out.println(_inner + " derivativeAt\n" + x + "\n= " + df  + "\n"); 
    return df; }

  @Override
  public final Function derivativeAt (final Object x) {
    final Function df = _inner.derivativeAt(x);
    System.out.println(_inner + " derivativeAt\n" + toString(x) + "\n= " + df + "\n"); 
    return df; }

  @Override
  public final double slopeAt (final double x) {
    final double df = _inner.slopeAt(x);
    System.out.println(_inner + " slopeAt at\n" + x + "\n= " + df + "\n"); 
    return df; }

  //--------------------------------------------------------------
  // Object interface
  //--------------------------------------------------------------

  @Override
  public final int hashCode () {
    int h = 17;
    h += 31*_inner.hashCode();
    h += 31*_out.hashCode();
    return h; }

  @Override
  public final boolean equals (final Object o) {
    // necessary but not sufficient
    // TODO: should this be left to subclasses?
    return
      (o instanceof Tracer)
      &&
      _inner.equals(((Tracer) o)._inner)
      &&
      _out.equals(((Tracer) o)._out); }

    @Override
    public String toString () {
      return "Tracer[\n" + _inner.toString() +
        "\n->\n" + _out + "]"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private Tracer (final Function inner,
                  final PrintWriter out) {
    super(inner.domain(),inner.codomain());
    _inner = inner;
    _out = out; }

  public static final Tracer wrap (final Function inner,
                                   final PrintWriter out) {
    return new Tracer(inner,out); }

  public static final Tracer wrap (final Function inner) {
    return wrap(inner,new PrintWriter(System.out)); }

  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------