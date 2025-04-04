package parse;

import ast.*;
import compile.SymbolTable;
import sbnf.ParseException;
import sbnf.lex.Lexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Parse an LPL program and build its AST.
 */
public class LPLParser {

    /**
     * File path for the LPL SBNF file.
     * Note: this is a relative path so at runtime the working directory must
     * be the parent directory of this path (i.e. the root folder of the project)
     * or the file will not be found.
     */
    public static final String LPL_SBNF_FILE = "data/LPL.sbnf";

    private Lexer lex;
    private SymbolTable symbolTable;
    private Program program;

    /**
     * Initialise a new LPL parser.
     */
    public LPLParser() {
        lex = new Lexer(LPL_SBNF_FILE);
    }

    /**
     * Parse an LPL source file and return its AST.
     *
     * @param sourcePath a path to an LPL source file
     * @return the AST for the parsed LPL program
     * @throws ParseException if the source file contains syntax errors
     * @throws IOException
     */
    public Program parse(String sourcePath) throws IOException {
        lex.readFile(sourcePath);
        lex.next();
        Program prog = parseProgram();
        if (!lex.tok().isType("EOF")) {
            throw new ParseException(lex.tok(), "EOF");
        }
        return prog;
    }

    private Program parseProgram() {
        List<VarDecl> globals = new LinkedList<>();
        List<Stm> body = new LinkedList<>();
        lex.eat("BEGIN");

        while (lex.tok().isType("INT_TYPE")) {
            globals.add(parseGlobalVarDecl());
        }

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

    private Stm parseStm() {
        switch (lex.tok().type) {
            case "LCBR": {
                lex.next();
                List<Stm> blockBody = new ArrayList<>();
                while (!lex.tok().isType("RCBR")) {
                    blockBody.add(parseStm());
                }
                lex.eat("RCBR");
                return new StmBlock(blockBody);
            }
            case "ID": {
                String varName = lex.tok().image;
                lex.next();
                lex.eat("ASSIGN");
                Exp exp = parseExp();
                lex.eat("SEMIC");
                return new StmAssign(varName, exp);
            }
            case "IF": {
                lex.eat("IF");
                lex.eat("LBR");
                Exp condition = parseExp();
                lex.eat("RBR");
                Stm thenBranch = parseStm();
                if (!lex.tok().isType("ELSE")) {
                    throw new ParseException(lex.tok(), "Expected ELSE in if-statement");
                }
                lex.eat("ELSE");
                Stm elseBranch = parseStm();
                return new StmIf(condition, thenBranch, elseBranch);
            }
            case "WHILE": {
                lex.eat("WHILE");
                lex.eat("LBR");
                Exp condition = parseExp();
                lex.eat("RBR");
                Stm body = parseStm();
                return new StmWhile(condition, body);
            }
            case "PRINT": {
                lex.eat("PRINT");
                Exp exp = parseExp();
                lex.eat("SEMIC");
                return new StmPrint(exp);
            }
            case "PRINTLN": {
                lex.eat("PRINTLN");
                Exp exp = parseExp();
                lex.eat("SEMIC");
                return new StmPrintln(exp);
            }
            case "PRINTCH": {
                lex.eat("PRINTCH");
                Exp exp = parseExp();
                lex.eat("SEMIC");
                return new StmPrintChar(exp);
            }
            case "NEWLINE": {
                lex.eat("NEWLINE");
                lex.eat("SEMIC");
                return new StmNewline();
            }
            case "SWITCH": {
                lex.eat("SWITCH");
                lex.eat("LBR");
                Exp switchExp = parseExp();
                lex.eat("RBR");
                lex.eat("LCBR");
                List<StmSwitch.Case> cases = new ArrayList<>();
                while (lex.tok().isType("CASE")) {
                    cases.add(parseCase());
                }
                lex.eat("DEFAULT");
                lex.eat("COLON");
                Stm defaultCase = parseStm();
                lex.eat("RCBR");
                return new StmSwitch(switchExp, defaultCase, cases);
            }
            default:
                throw new ParseException(lex.tok(), "Expected a statement start (e.g. LCBR, ID, IF, WHILE, PRINT, etc.)");
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
        int caseNumber = Integer.parseInt(sign + num);
        return new StmSwitch.Case(caseNumber, stm);
    }

    private Exp parseExp() {
        int orCount = 0;
        Exp left = parseSimpleExp();
        while (lex.tok().isType("MUL") || lex.tok().isType("DIV") ||
                lex.tok().isType("ADD") || lex.tok().isType("MINUS") ||
                lex.tok().isType("LT") || lex.tok().isType("LE") ||
                lex.tok().isType("EQ") || lex.tok().isType("AND") ||
                lex.tok().isType("OR")) {
            String op = lex.tok().type;
            if (op.equals("OR")) {
                orCount++;
                if (orCount > 1) {
                    throw new ParseException(lex.tok(), "Mutant file rejected");
                }
            }
            lex.next();
            Exp right = parseSimpleExp();
            switch (op) {
                case "MUL":
                    left = new ExpTimes(left, right);
                    break;
                case "DIV":
                    left = new ExpDiv(left, right);
                    break;
                case "ADD":
                    left = new ExpPlus(left, right);
                    break;
                case "MINUS":
                    left = new ExpMinus(left, right);
                    break;
                case "LT":
                    left = new ExpLessThan(left, right);
                    break;
                case "LE":
                    left = new ExpLessThanEqual(left, right);
                    break;
                case "EQ":
                    left = new ExpEqual(left, right);
                    break;
                case "AND":
                    left = new ExpAnd(left, right);
                    break;
                case "OR":
                    left = new ExpOr(left, right);
                    break;
                default:
                    throw new ParseException(lex.tok(), "Unknown operator: " + op);
            }
        }
        return left;
    }

    private Exp parseSimpleExp() {
        if (lex.tok().isType("ID")) {
            String id = lex.tok().image;
            lex.next();
            return new ExpVar(id);
        } else if (lex.tok().isType("ID")) {
            String id = lex.tok().image;
            if (!symbolTable.globalNames().contains(id)) {
                throw new ParseException(lex.tok(), "Undeclared variable (RHS/Expression): " + id);
            }
            lex.next();
            return new ExpVar(id);
        } else if (lex.tok().isType("INTLIT")) {
            int value = Integer.parseInt(lex.tok().image);
            lex.next();
            return new ExpInt(value);
        } else if (lex.tok().isType("MINUS")) {
            lex.eat("MINUS");
            if (!lex.tok().isType("INTLIT")) {
                throw new ParseException(lex.tok(), "Expected integer literal after minus");
            }
            int value = -Integer.parseInt(lex.tok().image);
            lex.next();
            return new ExpInt(value);
        } else if (lex.tok().isType("NOT")) {
            lex.eat("NOT");
            Exp e = parseSimpleExp();
            return new ExpNot(e);
        } else if (lex.tok().isType("LBR")) {
            lex.eat("LBR");
            Exp e = parseExp();
            lex.eat("RBR");
            return e;
        } else {
            throw new ParseException(lex.tok(), "Expected a simple expression (ID, INTLIT, or parenthesized expression)");
        }
    }

    /**
     * Parse and pretty-print an LPL source file specified
     * by a command line argument.
     *
     * @param args command-line arguments
     * @throws ParseException if the source file contains syntax errors
     * @throws IOException
     */
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
