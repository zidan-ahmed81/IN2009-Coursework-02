package ast;

import compile.SymbolTable;

public class ExpInt extends Exp {
    public final int value;

    public ExpInt(int value) {
        this.value = value;
    }

    @Override
    public void compile(SymbolTable st) {
        emit("push " + value);
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }
}
