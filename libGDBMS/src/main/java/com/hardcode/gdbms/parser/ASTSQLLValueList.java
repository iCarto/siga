/* Generated By:JJTree: Do not edit this line. ASTSQLLValueList.java */

package com.hardcode.gdbms.parser;

public class ASTSQLLValueList extends SimpleNode {
  public ASTSQLLValueList(int id) {
    super(id);
  }

  public ASTSQLLValueList(SQLEngine p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SQLEngineVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
