package ast;

public class VarDecl extends AST {
    public final Type type;
    public final String name;

    public VarDecl(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }
}
