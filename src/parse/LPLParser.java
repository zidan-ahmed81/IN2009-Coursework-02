package parse;

import ast.*;
import compile.SymbolTable;
import sbnf.ParseException;
import sbnf.lex.Lexer;

import java.io.IOException;
import java.util.*;

public class LPLParser {

    public static final String LPL_SBNF_FILE = "data/LPL-B.sbnf";

    private Lexer lex;
    private SymbolTable symbolTable;
    private Program program;
    private String sourcePath;

    public LPLParser() {
        lex = new Lexer(LPL_SBNF_FILE);
    }

    public Program parse(String sourcePath) throws IOException {
        this.sourcePath = sourcePath;
        lex.readFile(sourcePath);
        lex.next();
        Program prog = parseProgram();
        while (!lex.tok().isType("EOF")) {
            if (isMethodDeclStart()) {
                parseMethodDecl();
            } else {
                throw new ParseException(lex.tok(), "Expected EOF or method declaration after END");
            }
        }
        return prog;
    }

    private boolean isMethodDeclStart() {
        if (lex.tok().isType("FUN") || lex.tok().isType("PROC")) return true;
        if (!lex.tok().isType("INT_TYPE")) return false;

        List<String> snapshot = new ArrayList<>();
        snapshot.add(lex.tok().type);
        lex.next();
        String second = lex.tok().type;
        lex.next();
        String third = lex.tok().type;

        boolean isMethod = second.equals("ID") && third.equals("LBR");

        try {
            lex.readFile(sourcePath);
            lex.next();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return isMethod;
    }

    private Program parseProgram() {
        List<VarDecl> globals = new LinkedList<>();
        List<Object> methods = new LinkedList<>();
        List<Stm> body = new LinkedList<>();

        while (!lex.tok().isType("BEGIN")) {
            if (isMethodDeclStart()) {
                methods.add(parseMethodDecl());
            } else if (lex.tok().isType("INT_TYPE")) {
                if (isMethodDeclStart()) {
                    methods.add(parseMethodDecl());
                } else {
                    globals.add(parseGlobalVarDecl());
                }
            } else {
                throw new ParseException(lex.tok(), "Expected method or global variable declaration");
            }
        }

        lex.eat("BEGIN");
        while (!lex.tok().isType("END")) {
            body.add(parseStm());
        }
        lex.eat("END");

        this.program = new Program(globals, body);
        this.symbolTable = new SymbolTable(this.program);
        return this.program;
    }

    private VarDecl parseGlobalVarDecl() {
        lex.eat("INT_TYPE");
        String id = lex.tok().image;
        lex.next();
        lex.eat("SEMIC");
        return new VarDecl(new TypeInt(), id);
    }

    private Object parseMethodDecl() {
        if (lex.tok().isType("FUN")) {
            lex.eat("FUN");
            lex.eat("INT_TYPE");
        } else if (lex.tok().isType("PROC")) {
            lex.eat("PROC");
        } else if (lex.tok().isType("INT_TYPE")) {
            lex.eat("INT_TYPE");
        } else {
            throw new ParseException(lex.tok(), "Expected FUN, PROC, or INT_TYPE method declaration");
        }

        lex.eat("ID");
        lex.eat("LBR");
        if (!lex.tok().isType("RBR")) {
            do {
                lex.eat("INT_TYPE");
                lex.eat("ID");
                if (!lex.tok().isType("COMMA")) break;
                lex.eat("COMMA");
            } while (true);
        }
        lex.eat("RBR");
        lex.eat("LCBR");
        while (!lex.tok().isType("RCBR")) {
            parseStm();
        }
        lex.eat("RCBR");
        return null;
    }

    private Stm parseStm() {
        if (lex.tok().isType("INT_TYPE")) {
            lex.eat("INT_TYPE");
            String id = lex.tok().image;
            lex.next();
            lex.eat("SEMIC");
            return new StmPrint(new ExpInt(0));
        }

        switch (lex.tok().type) {
            case "LCBR":
                lex.eat("LCBR");
                List<Stm> block = new ArrayList<>();
                while (!lex.tok().isType("RCBR")) block.add(parseStm());
                lex.eat("RCBR");
                return new StmBlock(block);

            case "ID":
                String id = lex.tok().image;
                lex.next();
                if (lex.tok().isType("ASSIGN")) {
                    lex.eat("ASSIGN");
                    Exp exp = parseExp();
                    lex.eat("SEMIC");
                    return new StmAssign(id, exp);
                } else if (lex.tok().isType("LBR")) {
                    parseCall(id);
                    lex.eat("SEMIC");
                    return new StmPrint(new ExpInt(0));
                }
                throw new ParseException(lex.tok(), "Expected ASSIGN or LBR after ID");

            case "RETURN":
                lex.eat("RETURN");
                if (lex.tok().isType("SEMIC")) {
                    lex.eat("SEMIC");
                    return new StmNewline();
                } else {
                    Exp exp = parseExp();
                    lex.eat("SEMIC");
                    return new StmPrintln(exp);
                }

            case "IF":
                lex.eat("IF");
                lex.eat("LBR");
                Exp cond = parseExp();
                lex.eat("RBR");
                Stm thenStm = parseStm();
                lex.eat("ELSE");
                Stm elseStm = parseStm();
                return new StmIf(cond, thenStm, elseStm);

            case "WHILE":
                lex.eat("WHILE");
                lex.eat("LBR");
                Exp whileCond = parseExp();
                lex.eat("RBR");
                return new StmWhile(whileCond, parseStm());

            case "SWITCH":
                lex.eat("SWITCH");
                lex.eat("LBR");
                Exp switchExp = parseExp();
                lex.eat("RBR");
                lex.eat("LCBR");
                List<StmSwitch.Case> cases = new ArrayList<>();
                while (lex.tok().isType("CASE")) cases.add(parseCase());
                lex.eat("DEFAULT");
                lex.eat("COLON");
                Stm defaultCase = parseStm();
                lex.eat("RCBR");
                return new StmSwitch(switchExp, defaultCase, cases);

            case "PRINT":
                lex.eat("PRINT");
                Exp e1 = parseExp();
                lex.eat("SEMIC");
                return new StmPrint(e1);

            case "PRINTLN":
                lex.eat("PRINTLN");
                Exp e2 = parseExp();
                lex.eat("SEMIC");
                return new StmPrintln(e2);

            case "PRINTCH":
                lex.eat("PRINTCH");
                Exp e3 = parseExp();
                lex.eat("SEMIC");
                return new StmPrintChar(e3);

            case "NEWLINE":
                lex.eat("NEWLINE");
                lex.eat("SEMIC");
                return new StmNewline();

            default:
                throw new ParseException(lex.tok(), "Expected a statement start");
        }
    }

    private StmSwitch.Case parseCase() {
        lex.eat("CASE");
        String sign = "";
        if (lex.tok().isType("MINUS")) {
            sign = "-";
            lex.eat("MINUS");
        }
        String num = lex.tok().image;
        lex.eat("INTLIT");
        lex.eat("COLON");
        Stm stm = parseStm();
        return new StmSwitch.Case(Integer.parseInt(sign + num), stm);
    }

    private Exp parseExp() {
        Exp left = parseSimpleExp();
        while (Set.of("MUL", "DIV", "ADD", "MINUS", "LT", "LE", "EQ", "AND", "OR").contains(lex.tok().type)) {
            String op = lex.tok().type;
            lex.next();
            Exp right = parseSimpleExp();
            switch (op) {
                case "ADD": left = new ExpPlus(left, right); break;
                case "MINUS": left = new ExpMinus(left, right); break;
                case "MUL": left = new ExpTimes(left, right); break;
                case "DIV": left = new ExpDiv(left, right); break;
                case "LT": left = new ExpLessThan(left, right); break;
                case "LE": left = new ExpLessThanEqual(left, right); break;
                case "EQ": left = new ExpEqual(left, right); break;
                case "AND": left = new ExpAnd(left, right); break;
                case "OR": left = new ExpOr(left, right); break;
            }
        }
        return left;
    }

    private Exp parseSimpleExp() {
        if (lex.tok().isType("ID")) {
            String id = lex.tok().image;
            lex.next();
            if (lex.tok().isType("LBR")) return parseCall(id);
            return new ExpVar(id);
        } else if (lex.tok().isType("INTLIT")) {
            int value = Integer.parseInt(lex.tok().image);
            lex.next();
            return new ExpInt(value);
        } else if (lex.tok().isType("MINUS")) {
            lex.eat("MINUS");
            int value = -Integer.parseInt(lex.tok().image);
            lex.next();
            return new ExpInt(value);
        } else if (lex.tok().isType("NOT")) {
            lex.eat("NOT");
            return new ExpNot(parseSimpleExp());
        } else if (lex.tok().isType("LBR")) {
            lex.eat("LBR");
            Exp e = parseExp();
            lex.eat("RBR");
            return e;
        } else {
            throw new ParseException(lex.tok(), "Expected expression");
        }
    }

    private Exp parseCall(String id) {
        lex.eat("LBR");
        while (!lex.tok().isType("RBR")) {
            parseExp();
            if (lex.tok().isType("COMMA")) lex.eat("COMMA");
        }
        lex.eat("RBR");
        return new ExpInt(0); // Placeholder
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: parse.LPLParser <source-file>");
            System.exit(1);
        }
        LPLParser parser = new LPLParser();
        Program program = parser.parse(args[0]);
        System.out.println(program);
    }
}
