package ast;

import compile.SymbolTable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StmBlock extends Stm {

    public final List<Stm> stms;

    public StmBlock(List<Stm> stms) {
        this.stms = Collections.unmodifiableList(stms);
    }

    @Override
    public void compile(SymbolTable st) {
        for (Stm stm : stms) {
            stm.compile(st);
        }
    }


    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }
}
