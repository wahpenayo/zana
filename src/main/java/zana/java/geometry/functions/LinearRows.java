package zana.java.geometry.functions;

import java.util.Arrays;
import java.util.List;

import clojure.lang.IFn;
import zana.java.geometry.Dn;

//----------------------------------------------------------------
/** General linear function represented by canonical dual vectors
 * (that is, matrix rows).
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-04-06
 */

@SuppressWarnings("unchecked")
public final class LinearRows extends Function {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------
  // TODO: list of linear functionals?
  
  private final double[][] _rows;
  
  public final double coordinate (final int i, final int j) {
    return _rows[i][j]; }

  //--------------------------------------------------------------
  // Functional methods
  //--------------------------------------------------------------
  
  @Override
  public final double[] value (final Object x) {
    final double[] xx = (double[]) x;
    final int n = xx.length;
    assert ((Dn) domain()).dimension() == n :
      domain().toString() + "\n" + Arrays.toString(xx);
     final double[] y = new double[((Dn) codomain()).dimension()];
     //System.out.println("x:" + Arrays.toString(xx));
     //System.out.println("row[0]:" + Arrays.toString(_rows[0]));
     
    int i = 0;
    for (final double[] row : _rows) {
      y[i] = zana.java.arrays.Arrays.dot(row,xx); 
      i++; }
    return y; }
  
  @Override
  public final Function derivativeAt (final Object x) {
    // a linear function is its own derivative, independent of x
    return this; }

  //--------------------------------------------------------------
  // Object interface
  //--------------------------------------------------------------

  @Override
  public int hashCode () {
    int h = super.hashCode();
    h += 31*Arrays.hashCode(_rows);
    return h; }

  @Override
  public boolean equals (final Object o) {
    if (! super.equals(o)) { return false; }
    if (! (o instanceof LinearRows)) { return false; }
    final LinearRows that = (LinearRows) o;
    final int m = _rows.length;
    if (m != that._rows.length) { return false; }
    for (int i=0; i<m;i++) {
      if (! Arrays.equals(_rows[i],that._rows[i])) {
        return false; } }
    return true; }
  
  @Override
  public String toString () { 
    return getClass().getSimpleName() + 
      "[" + _rows.length + "," + _rows[0].length + "]"
  + "[" + Arrays.toString(_rows[0])
  //+ ", " + Arrays.toString(_rows[1]) 
  + "...]"; 
  }
  
  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  // TODO: move somewhere else
  
  private static final double[] doubleArray (final Object row) {
    if (row instanceof double[]) {
      final double[] r = (double[]) row;
      return Arrays.copyOf(r,r.length); }
    if (row instanceof List) {
      final List r = (List) row;
      final int n = r.size();
      final double[] rr = new double[n];
      for (int i=0;i<n;i++) {
        rr[i] = ((Number) r.get(i)).doubleValue(); }
    return rr; }
    throw new IllegalArgumentException(
      "can't coerce " + row + " to a double[]."); }

  private static final double[][] copy (final double[][] rows) {
    final int m = rows.length;
    final double[][] a = new double[m][];
    for (int i=0;i<m;i++) {
      a[i] = Arrays.copyOf(rows[i],rows[i].length); }
    return a; }

  private static final double[][] doubleArray2d (final List rows) {
    final int m = rows.size();
    final double[][] a = new double[m][];
    for (int i=0;i<m;i++) {
        a[i] = doubleArray(rows.get(i)); } 
    return a; }

  //--------------------------------------------------------------

  LinearRows (final double[][] rows) {
    super(Dn.get(rows[0].length),Dn.get(rows.length));
    _rows = rows;}

  public static final LinearRows make (final double[][] rows) {
    return new LinearRows(copy(rows)); }

  public static final LinearRows make (final Object rows) {
    if (rows instanceof double[][]) {
      return new LinearRows(copy((double[][]) rows)); }
    if (rows instanceof List) {
      return make(doubleArray2d((List) rows)); }
    throw new IllegalArgumentException(
      "Not a double[][] or list of lists:" + rows); }

  public static final LinearRows generate (final int m,
                                           final int n,
                                           final IFn.D g) {
    final double[][] rows = new double[m][n];
    for (int i=0;i<m;i++) { 
      for (int j=0;j<n;j++) {
        rows[i][j] = g.invokePrim(); } }
    return new LinearRows(rows); }

  //--------------------------------------------------------------
 } // end class
//----------------------------------------------------------------