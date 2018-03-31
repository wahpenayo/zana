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
 * @version 2018-03-30
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
  public final double[] value (final double[] x) {
    //assert domain().contains(x);
    assert ((Dn) domain()).dimension() == x.length :
      this.toString() + "\n" + x;
    final double[] y = new double[((Dn) codomain()).dimension()];
    int i = 0;
    for (final double[] row : _rows) {
      y[i] = zana.java.arrays.Arrays.dot(row,x); 
      i++; }
    return y; }
  
  @Override
  public final Function derivativeAt (final double[] x) {
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
      "[\n" +
      
      Arrays.toString(_rows); }
  
  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  
  private static final double[][] doubleArray2d (final List rows) {
    final int m = rows.size();
    final int n = ((List) rows.get(0)).size();
    final double[][] a = new double[m][n];
    for (int i=0;i<m;i++) {
      final List row = (List) rows.get(i);
      for (int j=0;j<n;j++) {
        a[i][j] = ((Number) row.get(j)).doubleValue(); } }
    return a; }
  //--------------------------------------------------------------

  private LinearRows (final double[][] rows) {
    super(Dn.get(rows[0].length),Dn.get(rows.length));
    final int m = rows.length;
    final int n = rows[0].length;
    final double[][] r = new double[rows.length][];
    for (int i=0;i<m;i++) {
      assert n == rows[i].length;
      r[i] = Arrays.copyOf(rows[i],n); }
    _rows = r;}

  public static final LinearRows make (final double[][] rows) {
    return new LinearRows(rows); }

  public static final LinearRows make (final Object rows) {
    if (rows instanceof double[][]) {
      return new LinearRows((double[][]) rows); }
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