/* Generated By:JJTree: Do not edit this line. ASTSQLCustom.java */

package com.hardcode.gdbms.parser;

public class ASTSQLCustom extends SimpleNode {
  public ASTSQLCustom(int id) {
    super(id);
  }

  public ASTSQLCustom(SQLEngine p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SQLEngineVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}