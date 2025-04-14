package parse;

import ast.*;
import compile.SymbolTable;
import sbnf.ParseException;
import sbnf.lex.Lexer;

import java.io.IOException;
import java.util.*;

public class LPLParser {

    public static final String LPL_SBNF_FILE = "data/LPL-C.sbnf";

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

        var first = lex.tok();
        lex.next();
        var second = lex.tok();
        lex.next();
        var third = lex.tok();

        boolean isMethod = second.isType("ID") && third.isType("LBR");

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

        // Eat any number of []
        while (lex.tok().isType("LSQBR")) {
            lex.eat("LSQBR");
            lex.eat("RSQBR");
        }

        if (!lex.tok().isType("ID")) {
            throw new ParseException(lex.tok(),
                    "Expected ID after INT_TYPE and brackets in global declaration, but found: " +
                            lex.tok().type + " (\"" + lex.tok().image + "\")");
        }

        String id = lex.tok().image;
        lex.next();

        // Optional: explicit array size like int a[5];
        if (lex.tok().isType("LSQBR")) {
            lex.eat("LSQBR");
            parseExp();
            lex.eat("RSQBR");
        }

        lex.eat("SEMIC");
        return new VarDecl(new TypeInt(), id); // Placeholder, treat all as TypeInt
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

            if (!lex.tok().isType("ID")) {
                throw new ParseException(lex.tok(), "Expected ID after INT_TYPE in local declaration, but found: " + lex.tok().type + " (\"" + lex.tok().image + "\")");
            }

            String id = lex.tok().image;
            lex.next();

            if (lex.tok().isType("LSQBR")) {
                lex.eat("LSQBR");
                parseExp();
                lex.eat("RSQBR");
            }

            lex.eat("SEMIC");
            return new StmPrint(new ExpInt(0));
        }
        if (lex.tok().isType("INT_TYPE")) {
            lex.eat("INT_TYPE");

            if (!lex.tok().isType("ID")) {
                throw new ParseException(lex.tok(), "Expected ID after INT_TYPE in local declaration");
            }

            String id = lex.tok().image;
            lex.next();

            if (lex.tok().isType("LSQBR")) {
                lex.eat("LSQBR");
                parseExp();
                lex.eat("RSQBR");
            }

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

                if (lex.tok().isType("LSQBR")) {
                    lex.eat("LSQBR");
                    parseExp(); // index
                    lex.eat("RSQBR");
                    lex.eat("ASSIGN");
                    parseExp(); // value
                    lex.eat("SEMIC");
                    return new StmPrint(new ExpInt(0)); // placeholder for array assignment
                }

                if (lex.tok().isType("ASSIGN")) {
                    lex.eat("ASSIGN");
                    parseExp();
                    lex.eat("SEMIC");
                    return new StmAssign(id, new ExpInt(0)); // placeholder
                }

                if (lex.tok().isType("LBR")) {
                    parseCall(id);
                    lex.eat("SEMIC");
                    return new StmPrint(new ExpInt(0));
                }

                throw new ParseException(lex.tok(), "Expected ASSIGN, LSQBR or LBR after ID");

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

        boolean isNegative = false;
        if (lex.tok().isType("MINUS")) {
            isNegative = true;
            lex.eat("MINUS");
        }

        if (!lex.tok().isType("INTLIT")) {
            throw new ParseException(lex.tok(), "Expected integer literal in CASE label, but got: " + lex.tok().type + " (\"" + lex.tok().image + "\")");
        }

        String numText = lex.tok().image;
        lex.eat("INTLIT");

        int value;
        try {
            value = Integer.parseInt(numText);
        } catch (NumberFormatException e) {
            throw new ParseException(lex.tok(), "Invalid number format in CASE label: " + numText);
        }

        if (isNegative) value = -value;

        lex.eat("COLON");
        Stm stm = parseStm();
        return new StmSwitch.Case(value, stm);
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

            // Handle method call: ID followed by (
            if (lex.tok().isType("LBR")) {
                return parseCall(id);

                // Handle array access: ID followed by [
            } else if (lex.tok().isType("LSQBR")) {
                lex.eat("LSQBR");
                parseExp(); // index expression
                lex.eat("RSQBR");
                return new ExpInt(0); // Placeholder for ExpArrayAccess
            }

            // Normal variable
            return new ExpVar(id);
        }

        else if (lex.tok().isType("INTLIT")) {
            String image = lex.tok().image;
            lex.next();
            try {
                return new ExpInt(Integer.parseInt(image));
            } catch (NumberFormatException e) {
                throw new ParseException(lex.tok(), "Invalid integer literal: " + image);
            }
        }

        else if (lex.tok().isType("MINUS")) {
            lex.eat("MINUS");
            if (!lex.tok().isType("INTLIT")) {
                throw new ParseException(lex.tok(), "Expected integer after minus sign, got: " + lex.tok().type + " (" + lex.tok().image + ")");
            }
            String image = lex.tok().image;
            lex.next();
            try {
                return new ExpInt(-Integer.parseInt(image));
            } catch (NumberFormatException e) {
                throw new ParseException(lex.tok(), "Invalid negative number: -" + image);
            }
        }

        else if (lex.tok().isType("NOT")) {
            lex.eat("NOT");
            return new ExpNot(parseSimpleExp());
        }

        else if (lex.tok().isType("LBR")) {
            lex.eat("LBR");
            Exp e = parseExp();
            lex.eat("RBR");
            return e;
        }

        else if (lex.tok().isType("NULL")) {
            lex.eat("NULL");
            return new ExpInt(0); // Placeholder for ExpNull
        }

        else {
            throw new ParseException(lex.tok(), "Expected expression");
        }
    }



    private Exp parseCall(String id) {
        lex.eat("LBR");
        List<Exp> args = new ArrayList<>();

        if (!lex.tok().isType("RBR")) {
            do {
                args.add(parseExp());
                if (!lex.tok().isType("COMMA")) break;
                lex.eat("COMMA");
            } while (true);
        }

        lex.eat("RBR");

        return new ExpInt(0); // Placeholder for ExpCall(id, args)
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
