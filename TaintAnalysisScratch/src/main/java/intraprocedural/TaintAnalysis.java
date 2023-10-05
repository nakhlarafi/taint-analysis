package intraprocedural;
import java.nio.file.*;
import java.io.IOException;

import soot.Body;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.*;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLnPosTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.util.Chain;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * The class extending ForwardFlowAnalysis. This is where you need to implement your taint analysis
 * TODO:
 *  Use a proper data structure for the program states so that you can support multiple taint sources and report the location of the taint source at the sinks.
 *  Replace FlowSet<Value> with the data structure you chose. You will also need to change the type of several parameters and return values of the methods
 *
 */
public class TaintAnalysis extends ForwardFlowAnalysis<Unit, FlowSet<TaintValue>> {

    private Set<String> sinks = new HashSet<>();
    private Set<String> sources = new HashSet<>();
    private Body body;
    private UnitGraph unitGraph;
    //Constructor of the class
    public TaintAnalysis(DirectedGraph<Unit> graph) {
        super(graph);
        if(graph instanceof UnitGraph) {
            this.unitGraph = (UnitGraph) graph;
        } else {
            throw new IllegalArgumentException("Expected a UnitGraph");
        }
        this.body = body;
        // Load sinks and sources from files
        try {
            sinks.addAll(Files.readAllLines(Paths.get("/Users/tahminaakter/Desktop/ECSE688/PA1/TestPrograms/InputFiles/sink.txt")));
            sources.addAll(Files.readAllLines(Paths.get("/Users/tahminaakter/Desktop/ECSE688/PA1/TestPrograms/InputFiles/source.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Perform the analysis
        doAnalysis();

        // Print out the analysis results
        UnitGraph unitGraph = (UnitGraph) graph;
        Chain<Unit> unitChain = unitGraph.getBody().getUnits();

        for (Unit unit : unitChain) {
            Stmt stmt = (Stmt) unit;

            // Retrieve the IN state of the unit after the analysis
            FlowSet<TaintValue> inState = this.getFlowBefore(unit);
            // Check if the unit is a sink
            if (stmt.containsInvokeExpr()) {
                String methodSignature = stmt.getInvokeExpr().getMethodRef().getSignature();

                if (sinks.contains(methodSignature)) {
                    // Retrieve values used in the unit
                    Set<Value> usedValues = new HashSet<>();
                    for (ValueBox usedValueBoxes : unit.getUseBoxes()) {
                        usedValues.add(usedValueBoxes.getValue());
                    }
                    // Check whether any of the used variables are tainted.
                    for (TaintValue taintedValue : inState) {
                        if (usedValues.contains(taintedValue.getValue())) {
                            // If a variable is tainted, report a leak
                            int line = getLineNumber(unit);
                            int sourceLine = getLineNumber(taintedValue.getSource());
                            System.out.println("——————————————————");
                            System.out.println("Found a Leak in " + unitGraph.getBody().getMethod().getSignature());
                            System.out.println("Source: line " + (sourceLine == -1 ? "UNKNOWN" : sourceLine) + ": " + taintedValue.getSource());
                            System.out.println("Leak: line " + (line == -1 ? "UNKNOWN" : line) + ": " + unit);
                            System.out.println("——————————————————");
                        }
                    }
                }
            }
        }
    }

    public int getLineNumber(Unit unit) {
        for (Tag tag : unit.getTags()) {
            if (tag instanceof LineNumberTag) {
                return ((LineNumberTag) tag).getLineNumber();
            } else if (tag instanceof SourceLnPosTag) {
                return ((SourceLnPosTag) tag).startLn();
            }
        }
        return -1; // or throw an exception if you prefer
    }


    @Override
    protected void flowThrough(FlowSet<TaintValue> inState, Unit unit, FlowSet<TaintValue> outState) {
        Stmt stmt = (Stmt) unit;

        // Copy the incoming state to the outgoing state as the base.
        inState.copy(outState);
        if (stmt instanceof AssignStmt && !isInsideDecisionBlock(stmt)) {
            AssignStmt assignStmt = (AssignStmt) stmt;
            Value rightOp = assignStmt.getRightOp();
            Value leftOp = assignStmt.getLeftOp();

            // Initialize a set to keep track of all taint sources related to rightOp.
            Set<Unit> taintSources = new HashSet<>();

            // Check if rightOp is a binary operation and handle taint propagation.
            if (rightOp instanceof BinopExpr) {
                BinopExpr binOp = (BinopExpr) rightOp;
                Value op1 = binOp.getOp1();
                Value op2 = binOp.getOp2();

                // Check if op1 or op2 is tainted and add the source to taintSources.
                for (TaintValue taintValue : inState) {
                    if (taintValue.getValue().equivTo(op1) || taintValue.getValue().equivTo(op2)) {
                        taintSources.add(taintValue.getSource());
                    }
                }
            }

            // Check if the statement is a source statement.
            if (stmt.containsInvokeExpr() &&
                    sources.contains(stmt.getInvokeExpr().getMethodRef().getSignature())) {
                taintSources.add(unit);  // The current statement is also a source.
            }

            // Handle taint propagation for leftOp based on taintSources.
            if (!taintSources.isEmpty()) {
                // If taintSources is not empty, leftOp should be tainted.
                // If multiple taint sources, each taint source will be associated with leftOp.
                for (Unit taintSource : taintSources) {
                    TaintValue newTaintValue = new TaintValue(leftOp, taintSource);
                    outState.add(newTaintValue);
                }
            } else {
                // If taintSources is empty, ensure leftOp is not tainted.
                for (TaintValue taintValue : inState) {
                    if (taintValue.getValue().equivTo(leftOp)) {
                        outState.remove(taintValue);
                    }
                }
            }
        }
        if (stmt instanceof IfStmt) {
            // Handle implicit flows using the refactored methods
//            System.out.println("Statment: "+stmt);
            handleImplicitFlows(inState, stmt, outState);
        }

//        System.out.println("OutState outside loop after merging in the FlowThrough: "+outState);
    }


    boolean isInsideDecisionBlock(Stmt stmt) {
        // Logic to determine whether stmt is inside an if-else block.
        // May involve inspecting the CFG, surrounding units, etc.
        // Note: Implementing this accurately could be complex.
        List<Unit> predecessors = unitGraph.getPredsOf(stmt);
        for (Unit pred : predecessors) {
            if (pred instanceof IfStmt) {
                return true;
            }
        }
        return false;
    }

    // Handle implicit flows
    void handleImplicitFlows(FlowSet<TaintValue> inState, Unit stmt, FlowSet<TaintValue> outState) {
        if (stmt instanceof IfStmt) {
            IfStmt ifStmt = (IfStmt) stmt;
            Value conditionValue = ifStmt.getCondition();

            for (TaintValue taintValue : inState) {
                if (conditionValue instanceof BinopExpr && isConditionTainted((BinopExpr) conditionValue, taintValue)) {
                    Unit trueBranchTarget = ifStmt.getTarget();
                    List<Unit> successors = unitGraph.getSuccsOf(ifStmt);
                    Unit falseBranchTarget = (successors.isEmpty()) ? null : successors.get(0);

                    // Propagate taint through true and false branches
                    propagateTaintThroughBranch(trueBranchTarget, taintValue, outState);
                    propagateTaintThroughBranch(falseBranchTarget, taintValue, outState);
                }
            }
        }
    }

    // Check if condition is tainted
    boolean isConditionTainted(BinopExpr binopExpr, TaintValue taintValue) {
        return taintValue.getValue().equivTo(binopExpr.getOp1()) || taintValue.getValue().equivTo(binopExpr.getOp2());
    }

    // Propagate taint through a branch
    void propagateTaintThroughBranch(Unit startUnit, TaintValue taintValue, FlowSet<TaintValue> outState) {
        FlowSet<TaintValue> branchState = new ArraySparseSet<>();
        Unit currentUnit = startUnit;

        while (currentUnit != null && !(currentUnit instanceof IfStmt)) {
            if (currentUnit instanceof AssignStmt) {
                AssignStmt assign = (AssignStmt) currentUnit;
                TaintValue newTaint = new TaintValue(assign.getLeftOp(), taintValue.getSource());
                branchState.add(newTaint);
                System.out.println("New taint: " + newTaint);
            }

            List<Unit> successors = unitGraph.getSuccsOf(currentUnit);
            currentUnit = (successors.isEmpty()) ? null : successors.get(0);
        }

        // Merge the taint information obtained from this branch into `outState`
        outState.union(branchState, outState);
    }

    @Override
    protected FlowSet<TaintValue> newInitialFlow() {
        // Initialize each program state
        // TODO: Initialize your own data structure
        return new ArraySparseSet<TaintValue>();
    }

    @Override
    protected void merge(FlowSet<TaintValue> out1, FlowSet<TaintValue> out2, FlowSet<TaintValue> in) {
        // Merge program state out1 and out2 into in
        // TODO: Change the merge function accordingly for your data structure
        out1.union(out2, in);
    }

    @Override
    protected void copy(FlowSet<TaintValue> src, FlowSet<TaintValue> dest) {
        // Copy from src to dest
        // TODO: Change the copy function accordingly for your data structure
        src.copy(dest);
    }

    @Override
    protected FlowSet<TaintValue> entryInitialFlow() {
        // Initialize the initial program state
        // TODO: Initialize your own data structure
        return new ArraySparseSet<TaintValue>();
    }
}