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
        stack.push(s.hashCode()); //CONSTANT POOL TO BE INSERTED
    }

    private void exec_itod() {
        int v = stack.pop();
        double d = (double) v;
        // push the double to the stack
       // stack.push(Double.doubleToLongBits(d)); //CONSTANT POOL TO BE INSERTED
    }

    private void exec_imod() {
        int right = stack.pop();
        int left = stack.pop();
        if (right != 0) stack.push(left % right);
        else runtime_error("division by 0");
    }

    //___________BOLEAN INSTRUCTIONS___________________________________________________________
    private void exec_fconst() {
    }

    private void exec_not() {
    }

    private void exec_and() {
    }

    private void exec_or() {
    }

    private void exec_beq() {
    }

    private void exec_bneq() {
    }

    private void exec_btos() {
    }

    private void exec_bprint() {
    }

    private void exec_tconst() {
    }

    //___________STRING INSTRUCTIONS_____________________________________________________________
    private void exec_sprint() {
    }

    private void exec_sneq() {
    }

    private void exec_seq() {
    }

    private void exec_sconcat() {
    }

    private void exec_sconst(int v) {
    }

    //___________REAL INSTRUCTIONS_____________________________________________________________
    private void exec_dprint() {
    }

    private void exec_dtos() {
    }

    private void exec_dgreaterequal() {
    }

    private void exec_dgreater() {
    }

    private void exec_dneq() {
    }

    private void exec_deq() {
    }

    private void exec_dleq() {
    }

    private void exec_dlt() {
    }

    private void exec_dmod() {
    }

    private void exec_ddiv() {
    }

    private void exec_dmult() {
    }

    private void exec_dsub() {
    }

    private void exec_dadd() {
    }

    private void exec_duminus() {
    }

    private void exec_dconst(int v) {
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
            default:
                System.out.println("This should never happen! In file vm.java, method exec_inst()");
                System.exit(1);
        }
    }


    private void exec_dinst(Instruction inst) {
        if (trace) {
            System.out.println(String.format("%5s: %-15s Stack: %s", IP, inst, stack));
        }
        OpCode opc = inst.getOpCode();
        int nArgs;
        int v;
        switch (opc) {
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
            default:
                System.out.println("This should never happen! In file vm.java, method exec_dinst()");
                System.exit(1);
        }
    }


    private void exec_sinst(Instruction inst) {
        if (trace) {
            System.out.println(String.format("%5s: %-15s Stack: %s", IP, inst, stack));
        }
        OpCode opc = inst.getOpCode();
        int nArgs;
        int v;
        switch (opc) {
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
            default:
                System.out.println("This should never happen! In file vm.java, method exec_inst()");
                System.exit(1);
        }
    }


    private void exec_binst(Instruction inst) {
        if (trace) {
            System.out.println(String.format("%5s: %-15s Stack: %s", IP, inst, stack));
        }
        OpCode opc = inst.getOpCode();
        int nArgs;
        int v;
        switch (opc) {
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
            default:
                System.out.println("This should never happen! In file vm.java, method exec_inst()");
                System.exit(1);
        }
    }


    public void run() {
        if (trace) {
            System.out.println("Trace while running the code");
            System.out.println("Execution starts at instrution " + IP);
        }
        while (IP < code.length) {
            exec_inst(code[IP]);
            IP++;
        }
        if (trace) System.out.println(String.format("%22s Stack: %s", "", stack));
    }

}
