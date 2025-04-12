package ast;

import compile.SymbolTable;

public class ExpOr extends Exp {
    public final Exp left, right;

    public ExpOr(Exp left, Exp right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void compile(SymbolTable st) {
        String shortCircuitLabel = st.freshLabel("orShortCircuit");
        String endLabel = st.freshLabel("orEnd");

        left.compile(st);                 // Evaluate left
        emit("dup");                      // Duplicate to inspect and preserve
        emit("jumpi_z " + shortCircuitLabel); // If left is zero, go evaluate right

        emit("pop");                      // Discard duplicate (left was non-zero)
        emit("push 1");                   // Result is 1 (true)
        emit("jumpi " + endLabel);        // Done

        emit(shortCircuitLabel + ":");
        emit("pop");                      // Discard duplicate (left was 0)
        right.compile(st);               // Evaluate right
        emit("test_z");                   // Normalize: 0 → 1, non-zero → 0
        emit("push 1");
        emit("swap");
        emit("sub");                      // Final result: 1 - test_z

        emit(endLabel + ":");
    }




    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
