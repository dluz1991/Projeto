package VM;

import ConstantPool.ConstantPool;
import VM.Instruction.*;

import java.util.*;
import java.io.*;


public class VirtualMachine {
    private boolean trace;       // trace flag
    private byte[] bytecodes;    // the bytecodes, storing just for displaying them. Not really needed
    private Instruction[] code;        // instructions (converted from the bytecodes)
    private int IP;                    // instruction pointer
    private Stack<Integer> stack = new Stack<>();// runtime stack
    private ConstantPool constantPool = new ConstantPool();  // runtime stack for the second operand


    public VirtualMachine(byte[] bytecodes, boolean trace, ConstantPool CP) {
        this.trace = trace;
        this.bytecodes = bytecodes;
        this.constantPool = CP;
        decode(bytecodes);
        this.IP = 0;
    }


    // decode the bytecodes into instructions and store them in this.code
    private void decode(byte[] bytecodes) {
        ArrayList<Instruction> inst = new ArrayList<>();
        try {
            // feed the bytecodes into a data input stream
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(bytecodes));
            // convert them into intructions
            while (true) {
                byte b = din.readByte();
                OpCode opc = OpCode.convert(b);
                switch (opc.nArgs()) {
                    case 0:
                        inst.add(new Instruction(opc));
                        break;
                    case 1:
                        int val = din.readInt();
                        inst.add(new Instruction1Arg(opc, val));
                        break;
                    default:
                        System.out.println("This should never happen! In file vm.java, method decode(...)");
                        System.exit(1);
                }
            }
        } catch (java.io.EOFException e) {
            // System.out.println("reached end of input stream");
            // reached end of input stream, convert arraylist to array
            this.code = new Instruction[inst.size()];
            inst.toArray(this.code);
            if (trace) {
                System.out.println("Disassembled instructions");
                //dumpInstructions();
                dumpInstructionsAndBytecodes();
            }
        } catch (java.io.IOException e) {
            System.out.println(e);
        }
    }

    // dump the instructions, along with the corresponding bytecodes
    public void dumpInstructionsAndBytecodes() {
        int idx = 0;
        for (int i = 0; i < code.length; i++) {
            StringBuilder s = new StringBuilder();
            s.append(String.format("%02X ", bytecodes[idx++]));
            if (code[i].nArgs() == 1) for (int k = 0; k < 4; k++)
                s.append(String.format("%02X ", bytecodes[idx++]));
            System.out.println(String.format("%5s: %-15s // %s", i, code[i], s));
        }
    }

    // dump the instructions to the screen
    public void dumpInstructions() {
        for (int i = 0; i < code.length; i++)
            System.out.println(i + ": " + code[i]);
    }

    private void runtime_error(String msg) {
        System.out.println("runtime error: " + msg);
        if (trace) System.out.println(String.format("%22s Stack: %s", "", stack));
        System.exit(1);
    }

    //___________INTEGER INSTRUCTIONS___________________________________________________________
    private void exec_iconst(Integer v) {
        stack.push(v);
    }

    private void exec_iuminus() {
        int v = stack.pop();
        stack.push(-v);
    }

    private void exec_iadd() {
        int right = stack.pop();
        int left = stack.pop();
        stack.push(left + right);
    }

    private void exec_isub() {
        int right = stack.pop();
        int left = stack.pop();
        stack.push(left - right);
    }

    private void exec_imult() {
        int right = stack.pop();
        int left = stack.pop();
        stack.push(left * right);
    }

    private void exec_idiv() {
        int right = stack.pop();
        int left = stack.pop();
        if (right != 0) stack.push(left / right);
        else runtime_error("division by 0");
    }

    private void exec_iprint() {
        int v = stack.pop();
        System.out.println(v);
    }

    private void exec_ilt() {
        int right = stack.pop();
        int left = stack.pop();
        if (left < right) stack.push(1);
        else stack.push(0);
    }

    private void exec_ileq() {
        int right = stack.pop();
        int left = stack.pop();
        if (left <= right) stack.push(1);
        else stack.push(0);
    }

    private void exec_igreater() {
        int right = stack.pop();
        int left = stack.pop();
        if (right < left) stack.push(1);
        else stack.push(0);
    }

    private void exec_igreaterequal() {
        int right = stack.pop();
        int left = stack.pop();
        if (right <= left) stack.push(1);
        else stack.push(0);

    }

    private void exec_ieq() {
        int right = stack.pop();
        int left = stack.pop();
        if (left == right) stack.push(1);
        else stack.push(0);
    }

    private void exec_ineq() {
        int right = stack.pop();
        int left = stack.pop();
        if (left != right) stack.push(1);
        else stack.push(0);
    }

    private void exec_itos() {
        int v = stack.pop();
        String s = String.valueOf(v);
        // push the string to the stack
        stack.push(constantPool.addString(s)); //CONSTANT POOL TO BE INSERTED
    }

    private void exec_itod() {
        int v = stack.pop();
        double d = (double) v;
        stack.push(constantPool.addDouble(v));
    }

    private void exec_imod() {
        int right = stack.pop();
        int left = stack.pop();
        if (right != 0) stack.push(left % right);
        else runtime_error("division by 0");
    }

    //___________BOLEAN INSTRUCTIONS___________________________________________________________
    private void exec_fconst() {
        stack.push(0);
    }

    private void exec_not() {
        int v = stack.pop();
        if (v == 0) stack.push(1);
        else stack.push(0);
    }

    private void exec_and() {
        int right = stack.pop();
        int left = stack.pop();
        if (left != 0 && right != 0) stack.push(1);
        else stack.push(0);
    }

    private void exec_or() {
        int right = stack.pop();
        int left = stack.pop();
        if (left != 0 || right != 0) stack.push(1);
        else stack.push(0);
    }

    private void exec_beq() {
        int right = stack.pop();
        int left = stack.pop();
        if (left == right) stack.push(1);
        else stack.push(0);
    }

    private void exec_bneq() {
        int right = stack.pop();
        int left = stack.pop();
        if (left != right) stack.push(1);
        else stack.push(0);
    }

    private void exec_btos() {
        int v = stack.pop();
        String s = v==0?"falso":"verdadeiro";
        // push the string to the stack
        stack.push(constantPool.addString(s)); //CONSTANT POOL TO BE INSERTED
    }

    private void exec_bprint() {
        int v = stack.pop();
        if (v == 0) System.out.println("falso");
        else System.out.println("verdadeiro");
    }

    private void exec_tconst() {
        stack.push(1);
    }

    //___________STRING INSTRUCTIONS_____________________________________________________________
    private void exec_sprint() {
        int v = stack.pop();
        String s = String.valueOf(constantPool.get(v));
        System.out.println(s);
    }

    private void exec_sneq() {
        String right = String.valueOf(constantPool.get(stack.pop()));
        String left = String.valueOf(constantPool.get(stack.pop()));
        if (!left.equals(right)) stack.push(1);
        else stack.push(0);
    }

    private void exec_seq() {
        String right = String.valueOf(constantPool.get(stack.pop()));
        String left = String.valueOf(constantPool.get(stack.pop()));
        if (left.equals(right)) stack.push(1);
        else stack.push(0);
    }

    private void exec_sconcat() {
        String right = String.valueOf(constantPool.get(stack.pop()));
        String left = String.valueOf(constantPool.get(stack.pop()));
        String s = left + right;
        // push the string to the stack
        stack.push(constantPool.addString(s)); //CONSTANT POOL TO BE INSERTED
    }

    private void exec_sconst(int v) {
        String s = String.valueOf(constantPool.get(v));
        // push the string to the stack
        stack.push(constantPool.addString(s)); //CONSTANT POOL TO BE INSERTED
    }

    //___________REAL INSTRUCTIONS_____________________________________________________________
    private void exec_dprint() {
        int v = stack.pop();
        Double s = (Double) constantPool.get(v);
        System.out.println(s);
    }

    private void exec_dtos() {
        int v = stack.pop();
        String s = String.valueOf((Double) constantPool.get(v));
        // push the string to the stack
        stack.push(constantPool.addString(s)); //CONSTANT POOL TO BE INSERTED
    }

    private void exec_dgreaterequal() {
        double right = (Double) constantPool.get(stack.pop());
        double left = (Double) constantPool.get(stack.pop());
        if (left>=right) stack.push(1);
        else stack.push(0);
    }

    private void exec_dgreater() {
        double right = (Double) constantPool.get(stack.pop());
        double left = (Double) constantPool.get(stack.pop());
        if (left>right) stack.push(1);
        else stack.push(0);
    }

    private void exec_dneq() {
        double right = (Double) constantPool.get(stack.pop());
        double left = (Double) constantPool.get(stack.pop());
        if (left != right) stack.push(1);
        else stack.push(0);
    }

    private void exec_deq() {
        double right = (Double) constantPool.get(stack.pop());
        double left = (Double) constantPool.get(stack.pop());
        if (left == right) stack.push(1);
        else stack.push(0);

    }

    private void exec_dleq() {
        double right = (Double) constantPool.get(stack.pop());
        double left = (Double) constantPool.get(stack.pop());
        if (left <= right) stack.push(1);
        else stack.push(0);
    }

    private void exec_dlt() {
        double right = (Double) constantPool.get(stack.pop());
        double left = (Double) constantPool.get(stack.pop());
        if (left < right) stack.push(1);
        else stack.push(0);
    }

    private void exec_dmod() {
        double right = (Double) constantPool.get(stack.pop());
        double left = (Double) constantPool.get(stack.pop());
        if (right != 0) stack.push(constantPool.addDouble(left % right));
        else runtime_error("division by 0");
    }

    private void exec_ddiv() {
        double right = (Double) constantPool.get(stack.pop());
        double left = (Double) constantPool.get(stack.pop());
        if (right != 0) stack.push(constantPool.addDouble(left / right));
        else runtime_error("division by 0");
    }

    private void exec_dmult() {
        double right = (Double) constantPool.get(stack.pop());
        double left = (Double) constantPool.get(stack.pop());
        stack.push(constantPool.addDouble(left * right));
    }

    private void exec_dsub() {
        double right = (Double) constantPool.get(stack.pop());
        double left = (Double) constantPool.get(stack.pop());
        stack.push(constantPool.addDouble(left - right));
    }

    private void exec_dadd() {
        double right = (Double) constantPool.get(stack.pop());
        double left = (Double) constantPool.get(stack.pop());
        stack.push(constantPool.addDouble(left + right));
    }

    private void exec_duminus() {
        double v = (Double) constantPool.get(stack.pop());
        stack.push(constantPool.addDouble(-v));
    }

    private void exec_dconst(int v) {
        double d = (double) constantPool.get(v);
        stack.push(constantPool.addDouble(d));
    }

    //___________TYPE INSTRUCTION_____________________________________________________________
    private void exec_inst(Instruction inst) {
        if (trace) {
            System.out.println(String.format("%5s: %-15s Stack: %s", IP, inst, stack));
        }
        OpCode opc = inst.getOpCode();
        int nArgs;
        int v;
        switch (opc) {
            //__________INTEGER___________________________________________
            case iconst:
                v = ((Instruction1Arg) inst).getArg();
                exec_iconst(v);
                break;
            case iuminus:
                exec_iuminus();
                break;
            case iadd:
                exec_iadd();
                break;
            case isub:
                exec_isub();
                break;
            case imult:
                exec_imult();
                break;
            case idiv:
                exec_idiv();
                break;
            case imod:
                exec_imod();
                break;
            case ilt:
                exec_ilt();
                break;
            case ileq:
                exec_ileq();
                break;
            case igreater:
                exec_igreater();
                break;
            case igreaterequal:
                exec_igreaterequal();
                break;
            case ieq:
                exec_ieq();
                break;
            case ineq:
                exec_ineq();
                break;
            case itos:
                exec_itos();
                break;
            case itod:
                exec_itod();
                break;
            case iprint:
                exec_iprint();
                break;
            //__________REAL____________________________________________________
            case dconst:
                v = ((Instruction1Arg) inst).getArg();
                exec_dconst(v);
                break;
            case duminus:
                exec_duminus();
                break;
            case dadd:
                exec_dadd();
                break;
            case dsub:
                exec_dsub();
                break;
            case dmult:
                exec_dmult();
                break;
            case ddiv:
                exec_ddiv();
                break;
            case dmod:
                exec_dmod();
                break;
            case dlt:
                exec_dlt();
                break;
            case dleq:
                exec_dleq();
                break;
            case deq:
                exec_deq();
                break;
            case dneq:
                exec_dneq();
                break;
            case dgreater:
                exec_dgreater();
                break;
            case dgreaterequal:
                exec_dgreaterequal();
                break;
            case dtos:
                exec_dtos();
                break;
            case dprint:
                exec_dprint();
                break;
            //__________STRINGS__________________________________________
            case sconst:
                v = ((Instruction1Arg) inst).getArg();
                exec_sconst(v);
                break;
            case sconcat:
                exec_sconcat();
                break;
            case seq:
                exec_seq();
                break;
            case sneq:
                exec_sneq();
                break;
            case sprint:
                exec_sprint();
                break;
            //__________BOOLEAN__________________________________________
            case tconst:
                exec_tconst();
                break;
            case fconst:
                exec_fconst();
                break;
            case not:
                exec_not();
                break;
            case and:
                exec_and();
                break;
            case or:
                exec_or();
                break;
            case beq:
                exec_beq();
                break;
            case bneq:
                exec_bneq();
                break;
            case btos:
                exec_btos();
                break;
            case bprint:
                exec_bprint();
                break;
        }
    }


    public void run() {
        if (trace) {
            System.out.println("Trace while running the code");
            System.out.println("Execution starts at instrution " + IP);
        }
        System.out.println("*** VM output ***");
        while (IP < code.length) {
            exec_inst(code[IP]);
            IP++;
        }
        if (trace) System.out.println(String.format("%22s Stack: %s", "", stack));
    }

}
