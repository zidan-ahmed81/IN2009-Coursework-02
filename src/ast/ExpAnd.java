package ast;

import compile.SymbolTable;

public class ExpAnd extends Exp {
    public final Exp left, right;

    public ExpAnd(Exp left, Exp right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void compile(SymbolTable st) {
        left.compile(st);
        emit("test_z");
        emit("push 1");
        emit("sub");
        right.compile(st);
        emit("test_z");
        emit("push 1");
        emit("sub");
        emit("mul");
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
