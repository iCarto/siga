/* Generated By:JJTree: Do not edit this line. ASTSQLUpdate.java */

package com.hardcode.gdbms.parser;

public class ASTSQLUpdate extends SimpleNode {
  public ASTSQLUpdate(int id) {
    super(id);
  }

  public ASTSQLUpdate(SQLEngine p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SQLEngineVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
