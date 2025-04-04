package ast;

import compile.StaticAnalysisException;
import compile.SymbolTable;

import java.util.*;

public class StmSwitch extends Stm {

    public final Exp caseExp;
    public final Stm defaultCase;
    public final List<Case> cases;

    public StmSwitch(Exp caseExp, Stm defaultCase, List<Case> cases) {
        this.caseExp = caseExp;
        this.defaultCase = defaultCase;
        this.cases = cases;
    }

    @Override
    public void compile(SymbolTable st) {
        caseExp.compile(st);
        String tempLabel = st.freshLabel("switchTemp");
        emit("storei " + tempLabel);
        String defaultLabel = st.freshLabel("default");
        String exitLabel = st.freshLabel("switchExit");
        List<String> caseLabels = new ArrayList<>();
        for (Case c : cases) {
            String caseLabel;
            if (c.caseNumber < 0) {
                caseLabel = st.freshLabel("case_neg" + (-c.caseNumber));
            } else {
                caseLabel = st.freshLabel("case" + c.caseNumber);
            }
            caseLabels.add(caseLabel);
            emit("loadi " + tempLabel);
            emit("push " + c.caseNumber);
            emit("sub");
            emit("test_z");
            emit("push 1");
            emit("sub");
            emit("jumpi_z " + caseLabel);
        }
        emit("loadi " + tempLabel);
        emit("jumpi " + defaultLabel);
        for (int i = 0; i < cases.size(); i++) {
            String caseLabel = caseLabels.get(i);
            emit(caseLabel + ":");
            cases.get(i).stm.compile(st);
            emit("jumpi " + exitLabel);
        }
        emit(defaultLabel + ":");
        defaultCase.compile(st);
        emit(exitLabel + ":");
    }

    public static class Case {
        public final int caseNumber;
        public final Stm stm;
        public String label;

        public Case(int caseNumber, Stm stm) {
            this.caseNumber = caseNumber;
            this.stm = stm;
        }
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }

}
