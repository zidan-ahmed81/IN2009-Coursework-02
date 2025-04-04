package ast;

import compile.SymbolTable;

public class StmIf extends Stm {

    public final Exp exp;

    public final Stm trueBranch, falseBranch;

    public StmIf(Exp exp, Stm trueBranch, Stm falseBranch) {
        this.exp = exp;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    @Override
    public void compile(SymbolTable st) {
        String falseLabel = st.freshLabel("ifFalse");
        String exitLabel = st.freshLabel("ifExit");

        exp.compile(st);
        emit("jumpi_z " + falseLabel);

        trueBranch.compile(st);
        emit("jumpi " + exitLabel);

        emit(falseLabel + ":");
        falseBranch.compile(st);
        emit(exitLabel + ":");
    }




    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }
}
