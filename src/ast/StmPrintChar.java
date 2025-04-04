package ast;

import compile.SymbolTable;

public class StmPrintChar extends Stm {

    public final Exp exp;

    public StmPrintChar(Exp exp) {
        this.exp = exp;
    }

    @Override
    public void compile(SymbolTable st) {
        exp.compile(st);
        emit("sysc 1");
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }

}
