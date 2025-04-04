package ast;

import ast.util.Visitor;
import stackmachine.machine.OpCode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public abstract class AST {

    private static List<String> emitted;

    private static ast.util.PrettyPrinter pp;

    static {
        emitted = new LinkedList<>();
        pp = new ast.util.PrettyPrinter();
    }

    private static String formatEmit(String s) {
        String[] parts = s.split(" |\t");
        if (parts.length > 0 && isMnemonic(parts[0])) {
            return "\t" + s;
        } else {
            return s;
        }
    }

    private static boolean isMnemonic(String s) {
        for (OpCode opcode: OpCode.values()) {
            if (opcode.mnemonic.equals(s)) return true;
        }
        return false;
    }

    /**
     * Emit a sequence of SSM assembly instructions.
     * @param ss the instructions.
     */
    protected static void emit(String ...ss) {
        for (String s: ss) emitted.add(s);
    }

    /**
     * Write the emitted SSM assembly code to a file and clear the emitted
     * code list.
     * @param path a path to the file where the assembly code is to be written
     */
    public static void write(Path path) throws IOException {
        try(BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"))) {
            for(String s: emitted) {
                writer.write(formatEmit(s));
                writer.write("\n");
            }
        }
        emitted.clear();
    }

    public <T> T accept(Visitor<T> visitor) {
        System.err.println("Warning: Missing override for accept(Visitor) in class " + this.getClass().getSimpleName());
        return null;
    }

    @Override
    public String toString() {
        return accept(pp);
    }

}
