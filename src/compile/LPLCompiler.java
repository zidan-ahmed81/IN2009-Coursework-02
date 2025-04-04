package compile;

import ast.AST;
import ast.Program;
import parse.LPLParser;
import sbnf.ParseException;

import java.io.IOException;
import java.nio.file.Paths;

/** Compile an LPL program to SSM assembly code.  */
public class LPLCompiler {

    /**
     * Parse and compile an LPL source file and output the generated
     * assembly code to a file (if the file exists it will be
     * overwritten). The two files are specified by command line arguments.
     * @param args command-line arguments
     * @throws ParseException if the source file contains syntax errors
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: compile.LPLCompiler <source-file> <SSM-assembly-file>");
            System.exit(1);
        }
        LPLParser parser = new LPLParser();
        Program program = parser.parse(args[0]);
        program.compile();
        AST.write(Paths.get(args[1]));
        System.out.println("Assembly code written to " + args[1]);
    }
}