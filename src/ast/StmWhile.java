package ast;

import ast.util.Visitor;
import compile.StaticAnalysisException;
import compile.SymbolTable;

public class StmWhile extends Stm {

    public final Exp condition;

    public final Stm body;

    public StmWhile(Exp condition, Stm body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void compile(SymbolTable st) {
        String startLabel = st.freshLabel("whileStart");
        String exitLabel = st.freshLabel("whileExit");
        emit(startLabel + ":");
        condition.compile(st);
        emit("push " + exitLabel);
        emit("jump_z");
        body.compile(st);
        emit("push " + startLabel);
        emit("jump");
        emit(exitLabel + ":");
    }

    @Override
    public <T> T accept(Visitor<T> visitor) { return visitor.visit(this); }
}
