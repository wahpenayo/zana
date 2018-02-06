package zana.java.functions;

import clojure.lang.IFn;
import clojure.lang.Named;

//----------------------------------------------------------------
/** Utilities for dealing with Clojure functions 
 * (<code>clojure.lang.IFn</code>) in Java.
 *
 * @author wahpenayo at gmail dot com
 * @version 2018-02-05
 */

public final class Functions extends Object {
  //--------------------------------------------------------------
  /** Adhoc function names.
   */
  public static final String name (final IFn f) {
    if (f instanceof Named) {
      final String name = ((Named) f).getName(); 
      final String namespace = ((Named) f).getNamespace();
      if ((null == namespace) || "".equals(namespace)) {
        return name; }
      return namespace + "/" + name; } 
    
  final String s = f.toString();
  
  if (s.startsWith("clojure.core$constantly$fn")) {
    return "(constantly " + f.invoke().toString() + ")"; }
  
  return s
    .replaceAll("^(.+)\\$([^@]+)(|@.+)$","$2")
    .replace('_','-')
    .replaceAll("--\\d+$",""); }
  //--------------------------------------------------------------
  
  //--------------------------------------------------------------
  // disabled constructor
  //--------------------------------------------------------------
  private Functions () {
    throw new UnsupportedOperationException(
      "Can't instantiate " + getClass()); }
  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------
