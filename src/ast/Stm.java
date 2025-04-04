package ast;

import compile.SymbolTable;

/**
 * The abstract parent type for all statement AST classes.
 */
public abstract class Stm extends AST {


    /**
     * Emit SSM assembly code which implements this statement.
     * @param st the symbol table for the program being compiled
     */
    public abstract void compile(SymbolTable st);

}
