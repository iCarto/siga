/* Generated By:JJTree: Do not edit this line. ASTSQLCompareExprRight.java */

package com.hardcode.gdbms.parser;

public class ASTSQLCompareExprRight extends SimpleNode {
  public ASTSQLCompareExprRight(int id) {
    super(id);
  }

  public ASTSQLCompareExprRight(SQLEngine p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SQLEngineVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
