package test;

// Maven: org.junit.jupiter:junit-jupiter:5.9.3

import ast.AST;
import ast.Program;
import compile.StaticAnalysisException;
import parse.LPLParser;
import sbnf.ParseException;
import stackmachine.assembler.Assembler;
import stackmachine.machine.SSM;
import staticanalysis.LPLTypeChecker;

import java.io.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Utils {



    public static final String TEST_FILES_ROOT = "data/tests";

    /**
     * Parse, compile, assemble and execute an LPL program.
     *
     * @param sourceFilePath a path to the LPL source file
     * @return the output of the program when executed
     * @throws IOException
     */
    public static String exec(String sourceFilePath) throws IOException {
        LPLParser parser = new LPLParser();
        ast.Program program = parser.parse(sourceFilePath);
        return exec(program);
    }

    /**
     * Compile, assemble and execute an LPL program.
     *
     * @param program the AST of the program to be executed
     * @return the output of the program when executed
     * @throws IOException
     */
    public static String exec(Program program) throws IOException {
        File ssmaFile = File.createTempFile("LPLtesting-", ".ssma");
        File ssmFile = File.createTempFile("LPLtesting-", ".ssm");
        program.compile();
        AST.write(ssmaFile.toPath());
        Assembler ass = new Assembler();
        boolean verbose = false;
        String charsetName = null;
        ass.assemble(ssmaFile.getAbsolutePath(), charsetName, ssmFile.getAbsolutePath(), verbose);
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream(); PrintStream out = new PrintStream(bout);) {
            boolean ranmem = true;
            String ssmFilePath = ssmFile.getAbsolutePath();
            SSM ssm = new SSM(ssmFilePath, new String[]{}, ranmem);
            ssm.setOut(out);
            ssm.run();
            out.flush();
            bout.close();
            String string = new String(bout.toByteArray());
            string = string.replaceAll("\\R", System.lineSeparator());
            return string;
        }
    }

    /**
     * Read the expected output from the initial comment block in an
     * LPL source file.
     * Note that this assumes line-structured output using native line
     * endings (as produced by SSM syscall number OUT_LN). It is not appropriate
     * for testing of programs intended to produce binary output.
     *
     * @param sourceFilePath a path to the test file
     * @return the expected output for this test
     * @throws IOException
     */
    public static String readExpected(String sourceFilePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFilePath))) {
            String expected = "";
            String line = reader.readLine();
            if (line != null) {
                if (line.startsWith("//")) {
                    expected += line.substring(2);
                    line = reader.readLine();
                } else {
                    line = null;
                }
            }
            while (line != null) {
                if (line.startsWith("//")) {
                    expected += System.lineSeparator() + line.substring(2);
                    line = reader.readLine();
                } else {
                    break;
                }
            }
            return expected;
        }
    }

    /**
     * Extract the expected result from a test-program file, then
     * compile, assemble and execute the test-program, asserting
     * the expected result.
     * @param testFilePath path to the test-program
     * @throws IOException
     */
    public static void doCompileTest(String testFilePath) throws IOException {
        String expected = readExpected(testFilePath);
        assertEquals(expected, exec(testFilePath));
    }

    /**
     * Run a type-checker test. Each test consists of two files, one containing a
     * well-typed LPL program, and a "mutant twin" where a type-error
     * error has been deliberately introduced. The mutant twin should
     * have the same file name but with the suffix ".mutant" appended.
     * @param testFilePath a path to a well-typed LPL source file
     * @return "ok" if the well-typed input is accepted by LPLTypeChecker and its mutant
     * twin is rejected; returns "mutant file was not rejected" if both files
     * are accepted by LPLTypeChecker
     * @throws IOException
     */
    public static String typeCheck(String testFilePath) throws IOException {
        LPLTypeChecker.typeCheck(testFilePath);
        try {
            LPLTypeChecker.typeCheck(testFilePath + ".mutant");
            return "mutant file was not rejected";
        } catch (StaticAnalysisException e) {
            return "ok";
        }
    }

    /**
     * Run a parser test. Each test consists of two files, one containing a
     * syntactically valid LPL program, and a "mutant twin" where a syntax
     * error has been deliberately introduced. The mutant twin should
     * have the same file name but with the suffix ".mutant" appended.
     * @param testFilePath a path to a syntactically valid LPL source file
     * @return "ok" if the valid input is accepted by LPLParser and its mutant
     * twin is rejected; returns "mutant file was not rejected" if both files
     * are accepted by LPLParser
     * @throws IOException
     */
    public static String parse(String testFilePath) throws IOException {
        LPLParser parser = new LPLParser();
        parser.parse(testFilePath);
        parser = new LPLParser();
        try {
            parser.parse(testFilePath + ".mutant");
            return "mutant file was not rejected";
        } catch (ParseException e) {
            return "ok";
        }
    }

    /**
     * List the path names of all the "valid input" test files in a directory.
     * @param dir a path to the directory
     * @return a stream of path names for all the files in the given directory,
     * excluding any sub-directories or files with names ending in ".mutant".
     */
    public static Stream<String> testFilePaths(String dir) {
        return Stream.of(new File(dir).listFiles())
                .filter(f -> !f.isDirectory())
                .filter(f -> !f.getName().endsWith(".mutant"))
                .map(f -> dir + "/" + f.getName())
                .sorted();
    }
}
