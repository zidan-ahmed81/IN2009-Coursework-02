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
        String skipRight = st.freshLabel("andSkip");
        String endLabel = st.freshLabel("andEnd");

        left.compile(st);                 // Evaluate left side
        emit("dup");                      // Duplicate result for checking
        emit("jumpi_z " + skipRight);    // If zero, skip right side

        emit("pop");                      // Clean up duplicate (left was non-zero)
        right.compile(st);
        emit("test_z");     // Non-zero → 0, zero → 1
        emit("push 1");     // Stack: ..., test_z_result, 1
        emit("swap");       // Stack: ..., 1, test_z_result
        emit("sub");        // 1 - test_z_result
        // 1 - result → normalize to 0 or 1
        emit("jumpi " + endLabel);       // Done

        emit(skipRight + ":");
        emit("pop");                      // Clean up the duplicate
        emit("push 0");                   // Left was 0 → short-circuit to false

        emit(endLabel + ":");
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
