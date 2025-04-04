package ast.util;

import ast.*;

/**
 * A pretty-printer for ASTs.
 * Note: this implementation is very inefficient due to its heavy use of string
 * concatenation. Should be fine for smallish ASTs.
 */
public class PrettyPrinter extends VisitorAdapter<String> {

    public PrettyPrinter() {}

    @Override
    public String visit(ExpTimes expTimes) {
        return simpleExp(expTimes.left) + " * " + simpleExp(expTimes.right);
    }

    @Override
    public String visit(ExpPlus expPlus) {
        return simpleExp(expPlus.left) + " + " + simpleExp(expPlus.right);
    }

    @Override
    public String visit(ExpMinus expMinus) {
        return simpleExp(expMinus.left) + " - " + simpleExp(expMinus.right);
    }

    @Override
    public String visit(ExpDiv expDiv) {
        return simpleExp(expDiv.left) + " / " + simpleExp(expDiv.right);
    }

    @Override
    public String visit(ExpLessThan expLessThan) {
        return simpleExp(expLessThan.left) + " < " + simpleExp(expLessThan.right);
    }

    @Override
    public String visit(ExpLessThanEqual expLessThanEqual) {
        return simpleExp(expLessThanEqual.left) + " <= " + simpleExp(expLessThanEqual.right);
    }

    @Override
    public String visit(ExpEqual expEqual) {
        return simpleExp(expEqual.left) + " == " + simpleExp(expEqual.right);
    }

    @Override
    public String visit(ExpAnd expAnd) {
        return simpleExp(expAnd.left) + " && " + simpleExp(expAnd.right);
    }

    @Override
    public String visit(ExpOr expOr) {
        return simpleExp(expOr.left) + " || " + simpleExp(expOr.right);
    }

    @Override
    public String visit(ExpInt expInt) {
        return "" + expInt.value;
    }

    @Override
    public String visit(ExpNot expNot) {
        return "!" + simpleExp(expNot.e);
    }

    @Override
    public String visit(ExpVar expVar) {
        return expVar.varName;
    }

    @Override
    public String visit(Program program) {
        String s = "begin";
        for (VarDecl decl: program.varDecls) {
            s += "\n" + indent(decl.accept(this) + ";");
        }
        if (!program.varDecls.isEmpty()) s += "\n";
        for (Stm stm: program.body) {
            s += "\n" + indent(stm.accept(this));
        }
        s += "\nend";
        return s;
    }

    @Override
    public String visit(StmAssign stmAssign) {
        return stmAssign.varName + " = " + stmAssign.exp.accept(this) + ";";
    }

    @Override
    public String visit(StmBlock stmBlock) {
        String s = "{";
        for (Stm stm: stmBlock.stms) {
            s += "\n" + indent(stm.accept(this));
        }
        if (stmBlock.stms.size() > 0) s += "\n";
        s += "}";
        return s;
    }

    @Override
    public String visit(StmIf stmIf) {
        String s = "if (" + stmIf.exp.accept(this) + ")";
        s += "\n";
        if (!(stmIf.trueBranch instanceof StmBlock)) {
            s += indent(stmIf.trueBranch.accept(this));
        } else {
            s += stmIf.trueBranch.accept(this);
        }
        s += "\n" + "else";
        s += "\n";
        if (!(stmIf.falseBranch instanceof StmBlock)) {
            s += indent(stmIf.falseBranch.accept(this));
        } else {
            s += stmIf.falseBranch.accept(this);
        }
        return s;
    }

    @Override
    public String visit(StmNewline stmNewline) {
        return "newline;";
    }

    @Override
    public String visit(StmPrint stmPrint) {
        return "print " + stmPrint.exp.accept(this) + ";";
    }

    @Override
    public String visit(StmPrintChar stmPrintChar) {
        return "printch " + stmPrintChar.exp.accept(this) + ";";
    }

    @Override
    public String visit(StmPrintln stmPrintln) {
        return "println " + stmPrintln.exp.accept(this) + ";";
    }

    @Override
    public String visit(StmSwitch stmSwitch) {
        String s = "switch (" + stmSwitch.caseExp.accept(this) + ") {";
        for (StmSwitch.Case c: stmSwitch.cases) {
            s += "\n" + indent(caseString(c));
        }
        s += "\n" + indent("default:\n" + indent(stmSwitch.defaultCase.accept(this)));
        s += "\n}";
        return s;
    }

    private String caseString(StmSwitch.Case c) {
        return "case " + c.caseNumber + ":\n" + indent(c.stm.accept(this));
    }

    @Override
    public String visit(StmWhile stmWhile) {
        String s = "while (" + stmWhile.condition.accept(this) + ")";
        s += "\n";
        if (!(stmWhile.body instanceof StmBlock)) {
            s += indent(stmWhile.body.accept(this));
        } else {
            s += stmWhile.body.accept(this);
        }
        return s;
    }

    @Override
    public String visit(Type type) {
        return null;
    }

    @Override
    public String visit(TypeInt typeInt) {
        return "int";
    }

    @Override
    public String visit(VarDecl varDecl) {
        return varDecl.type.accept(this) + " " + varDecl.name;
    }

    private String simpleExp(Exp exp) {
        // Note: could use a switch for this in JDK 17-preview or later
        if (exp instanceof ExpTimes ||
                exp instanceof ExpPlus ||
                exp instanceof ExpDiv ||
                exp instanceof ExpMinus ||
                exp instanceof ExpLessThan ||
                exp instanceof ExpLessThanEqual ||
                exp instanceof ExpEqual ||
                exp instanceof ExpAnd ||
                exp instanceof ExpOr) {
            return "(" + exp.accept(this) + ")";
        } else {
            return exp.accept(this);
        }
    }

    /**
     * Utility method for pretty printing ASTs.
     * @param s a multi-line string representation of an AST
     * @return a copy of s with each line indented
     */
    private static String indent(String s) {
        String INDENT = "   ";
        if (s.isEmpty()) return "";
        String[] lines = s.split("\\\n");
        s = INDENT + lines[0];
        for (int i = 1; i < lines.length; ++i) {
            s += "\n" + INDENT + lines[i];
        }
        return s;
    }

}
