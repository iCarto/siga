package org.gvsig.spatialindex.b2dtree;

 /**
  * KeySizeException is thrown when a KDTree method is invoked on a
  * key whose size (array length) mismatches the one used in the that
  * KDTree's constructor.
  *
  * @author      Simon Levy
  * @version     %I%, %G%
  * @since JDK1.2 
  */

public class KeySizeException extends Exception {

    protected KeySizeException() {
	super("Key size mismatch");
    }
}
