package ast;

import compile.SymbolTable;

public class ExpVar extends Exp {

    public final String varName;

    public ExpVar(String varName) {
        this.varName = varName; 
    }

    @Override
    public void compile(SymbolTable st) {
        // force an exception if varName has no declaration
        st.getVarType(varName);
        // global variable, statically allocated
        emit("loadi " + SymbolTable.makeVarLabel(varName));
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }
}
