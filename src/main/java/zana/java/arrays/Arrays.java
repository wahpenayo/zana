package zana.java.arrays;
import java.util.List;
import java.util.RandomAccess;

import clojure.lang.PersistentVector;
import clojure.lang.IPersistentVector;
import clojure.lang.IFn;
//----------------------------------------------------------------
/** Numerical array utilities for use with Clojure code.
 *
 * @author wahpenayo at gmail dot com
 * @version 2018-01-29
 */

public final class Arrays extends Object {
  //--------------------------------------------------------------
  public static final boolean isSingular (final double[] a) {
    if (0 >= a.length) { return true; }
    final int n = a.length;
    final double a0 = a[0];
    for (int i=1;i<n;i++) { if (a0 != a[i]) { return false; } }
    return true; }
  //--------------------------------------------------------------
  /** Copy x and y values into the arrays, where x and y are not NaN.
   */

  public static final IPersistentVector filterNaNs (final IFn.OD xf,
                                                    final IFn.OD yf,
                                                    final List records,
                                                    final double[] x,
                                                    final double[] y) {
    assert (null != xf) : "No x attribute!";
    assert (null != yf) : "No y attribute!";
    assert (records instanceof RandomAccess);

    // will throw an exception if x,y are null or too small.
    final int n = records.size();
    int i1=0;
    for (int i0=0; i0<n; i0++) {
      final Object record = records.get(i0);
      final double xi = xf.invokePrim(record);
      final double yi = yf.invokePrim(record);
      if (! (Double.isNaN(xi) || Double.isNaN(yi))) {
        x[i1] = xi; y[i1] = yi; i1++; } }
    
    // for safety, not strictly necessary
    //java.util.Arrays.fill(x,i1,x.length,Double.NaN);
    //java.util.Arrays.fill(y,i1,y.length,Double.NaN);
    
    return PersistentVector.create(Integer.valueOf(i1), x, y); }
  //--------------------------------------------------------------
  public static final IPersistentVector filterNaNs (final IFn.OD xf,
                                                    final IFn.OD yf,
                                                    final List records) {
    assert (null != xf) : "No x attribute!";
    assert (null != yf) : "No y attribute!";
    
    final int n = records.size();
    assert (n > 0) : "No data!";
    return filterNaNs(xf, yf, records, new double[n], new double[n]); }
  
  //--------------------------------------------------------------
  public static final IPersistentVector filterNaNs (final IFn.OL xf,
                                                    final IFn.OD yf,
                                                    final List records,
                                                    final double[] x,
                                                    final double[] y) {
    assert (null != xf) : "No x attribute!";
    assert (null != yf) : "No y attribute!";
    
    final int n = records.size();
    assert (n > 0) : "No data!";

    int i = 0;
    for (final Object record : records) {
      final double xi = xf.invokePrim(record);
      final double yi = yf.invokePrim(record);
      if (!Double.isNaN(yi)) { x[i] = xi; y[i] = yi; i++; } }
    
    // for safety, not strictly necessary
    java.util.Arrays.fill(x,i,n,Double.NaN);
    java.util.Arrays.fill(y,i,n,Double.NaN);
    final IPersistentVector v = 
      PersistentVector.create(Integer.valueOf(i), x, y);
    
    return v; }
  //--------------------------------------------------------------
  public static final IPersistentVector filterNaNs (final IFn.OL xf,
                                                    final IFn.OD yf,
                                                    final List records) {
    assert (null != xf) : "No x attribute!";
    assert (null != yf) : "No y attribute!";
    
    final int n = records.size();
    assert (n > 0) : "No data!";
    return filterNaNs(xf, yf, records, new double[n], new double[n]); }
  
  //--------------------------------------------------------------
  public static final IPersistentVector filterNaNs (final IFn.OD xf,
                                                    final IFn.OD yf,
                                                    final IFn.OD wf,
                                                    final List records,
                                                    final double[] x,
                                                    final double[] y,
                                                    final double[] w) {
    assert (null != xf) : "No x attribute!";
    assert (null != yf) : "No y attribute!";
    assert (null != wf) : "No w attribute!";

    // will throw an exception if x,y,w are null or too small.
    int i = 0;
    for (final Object record : records) {
      final double xi = xf.invokePrim(record);
      final double yi = yf.invokePrim(record);
      final double wi = wf.invokePrim(record);
      assert (! Double.isNaN(wi)) : "weight is missing for " + record;
      if (!(Double.isNaN(xi) || Double.isNaN(yi))) {
        x[i] = xi;
        y[i] = yi;
        w[i] = wi;
        i++; } }
    
    // for safety, not strictly necessary
    java.util.Arrays.fill(x,i,x.length,Double.NaN);
    java.util.Arrays.fill(y,i,y.length,Double.NaN);
    java.util.Arrays.fill(w,i,w.length,Double.NaN);
    final IPersistentVector v = 
      PersistentVector.create(Integer.valueOf(i), x, y, w);

    return v; }
  //--------------------------------------------------------------
  public static final IPersistentVector filterNaNs (final IFn.OD xf,
                                                    final IFn.OD yf,
                                                    final IFn.OD wf,
                                                    final List records) {
    assert (null != xf) : "No x attribute!";
    assert (null != yf) : "No y attribute!";
    assert (null != wf) : "No w attribute!";
    
    final int n = records.size();
    assert (n > 0) : "No data!";
    return filterNaNs(xf, yf, wf, records, 
      new double[n], new double[n], new double[n]); }
  
  //--------------------------------------------------------------
  public static final IPersistentVector filterNaNs (final IFn.OL xf,
                                                    final IFn.OD yf,
                                                    final IFn.OD wf,
                                                    final List records,
                                                    final double[] x,
                                                    final double[] y,
                                                    final double[] w) {
    assert (null != xf) : "No x attribute!";
    assert (null != yf) : "No y attribute!";
    assert (null != wf) : "No w attribute!";
    
    final int n = records.size();
    assert (n > 0) : "No data!";

    int i = 0;
    for (final Object record : records) {
      final double xi = xf.invokePrim(record);
      final double yi = yf.invokePrim(record);
      final double wi = wf.invokePrim(record);
      assert (! Double.isNaN(wi)) : "weight is missing for " + record;
      if (!Double.isNaN(yi)) { x[i] = xi; y[i] = yi; w[i] = wi; i++; } }
    
    // for safety, not strictly necessary
    java.util.Arrays.fill(x,i,n,Double.NaN);
    java.util.Arrays.fill(y,i,n,Double.NaN);
    java.util.Arrays.fill(w,i,n,Double.NaN);
    final IPersistentVector v = 
      PersistentVector.create(Integer.valueOf(i), x, y, w);

    return v; }
  //--------------------------------------------------------------
  public static final IPersistentVector filterNaNs (final IFn.OL xf,
                                                    final IFn.OD yf,
                                                    final IFn.OD wf,
                                                    final List records) {
    assert (null != xf) : "No x attribute!";
    assert (null != yf) : "No y attribute!";
    assert (null != wf) : "No w attribute!";
    
    final int n = records.size();
    assert (n > 0) : "No data!";
    return filterNaNs(xf, yf, wf, records, 
      new double[n], new double[n], new double[n]); }
  
  //--------------------------------------------------------------
  public static final double[] dMap (final IFn.OD attribute,
                                     final List records) {
    assert (records instanceof RandomAccess);
    final int n = records.size();
    final double[] a = new double[n];
    for (int i=0; i<n;i++) { 
      final double ai = attribute.invokePrim(records.get(i));
      assert ! Double.isNaN(ai) : records.get(i).toString();
      a[i] = ai; }
    return a; }
  //--------------------------------------------------------------
  public static final double[] dMap (final IFn attribute,
                                     final List records) {
    assert (records instanceof RandomAccess);
    final int n = records.size();
    final double[] a = new double[n];
    for (int i=0; i<n;i++) { 
      a[i] = ((Number) attribute.invoke(records.get(i))).doubleValue(); }
    return a; }
  //--------------------------------------------------------------
  public static final double[] dMap (final Object attribute,
                                     final Object records) {
    if (attribute instanceof IFn.OD) {
      return dMap((IFn.OD) attribute, (List) records); }
    else if (attribute instanceof IFn) {
      return dMap((IFn) attribute, (List) records); }
    else {
      throw new IllegalArgumentException("can't handle " + attribute); } }
  //--------------------------------------------------------------
  // primitive arrays as elements of linear spaces
  //--------------------------------------------------------------
  // TODO: move these to a BLAS-like library.
  // TODO: have a generic version, and test performance overhead.
  // TODO: compensated or exact version?
  
  public static final double dot (final double[] v0,
                                  final double[] v1) {
    final int n = v0.length;
    assert n == v1.length;
    double sum = 0.0;
    // NOTE: fma requires Java 9
    for (int i=0;i<n;i++) { sum = Math.fma(v0[i],v1[i],sum); }
    return sum; }
  
  //--------------------------------------------------------------
  // disabled constructor
  //--------------------------------------------------------------
  private Arrays () {
    throw new UnsupportedOperationException(
      "Can't instantiate " + getClass()); }
  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------