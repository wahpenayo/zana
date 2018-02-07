package zana.java.data;

import java.io.Serializable;

//----------------------------------------------------------------------------
/** AKA one-hot encoding.
 *  
 * @author wahpenayo at gmail dot com
 * @version 2018-02-05
 */

@SuppressWarnings("unchecked")
public interface AttributeEmbedding 
extends Serializable {


  int dimension ();

  int updateCoordinates (final Object value,
                         final double[] coords,
                         final int start);

} 