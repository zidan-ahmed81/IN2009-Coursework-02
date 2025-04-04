package compile;

import ast.Program;
import ast.Type;
import ast.VarDecl;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SymbolTable {

    private Map<String, Type> globals;
    private int freshNameCounter;
    private Set<String> tempLabels;

    /**
     * Initialise a new symbol table.
     * @param program the program
     */
    public SymbolTable(Program program) {
        this.freshNameCounter = 0;
        this.globals = new HashMap<>();
        this.tempLabels = new HashSet<>();
        for (VarDecl decl : program.varDecls) {
            if (!globals.containsKey(decl.name)) {
                globals.put(decl.name, decl.type);
            }
        }
    }


    public Type getVarType(String name) {
        Type t = globals.get(name);
        if (t == null) {
            throw new StaticAnalysisException("Undeclared variable: " + name);
        }
        return t;
    }

    public Set<String> globalNames() {
        return new HashSet<>(globals.keySet());
    }

    public static String makeVarLabel(String sourceName) {
        return "$_" + sourceName;
    }

    /**
     * Returns a fresh label with the given prefix.
     */
    public String freshLabel(String prefix) {
        String label = "$$_" + prefix + "_" + (freshNameCounter++);
        if (prefix.equals("switchTemp")) {
            tempLabels.add(label);
        }
        return label;
    }

    /**
     * Returns the set of temporary labels generated.
     */
    public Set<String> getTempLabels() {
        return tempLabels;
    }
}
