package VM;

/*
  Instruction codes of the virtual machine
*/

public enum OpCode {
    // single-byte instructions, just the OpCode: no arguments
    //Intructions with 1 argument (5 bytes)
    iconst   (1),
    dconst   (1),
    sconst   (1),
    //Integer instructions
    iprint   (0),
    iuminus  (0),
    iadd     (0),
    isub     (0),
    imult    (0),
    idiv     (0),
    imod (0),
    ieq   (0),
    ineq (0),
    ilt   (0),
    ileq (0),
    itod  (0),
    itos   (0),
    //Real instructions
    dprint   (0),
    duminus  (0),
    dadd     (0),
    dsub     (0),
    dmult    (0),
    ddiv     (0),
    deq   (0),
    dneq (0),
    dlt(0),
    dleq(0),
    dtos   (0),
    //String instructions
    sprint   (0),
    sconcat   (0),
    seq   (0),
    sneq(0),
    //Boolean instructions
    tconst   (0),
    fconst   (0),
    bprint   (0),
    beq   (0),
    bneq (0),
    and     (0),
    or      (0),
    not     (0),
    btos (0),
    // Instructions for variables, 1 or 2 arguments (5 bytes)
    jump(2),
    jumpf(2),
    galloc(1),
    gload(1),
    gstore(1),
    // stop execution
    halt     (0),
    //nao existe no op code da virtual machine
    dmod (0),
    ;



    private final int nArgs;    // number of arguments
    OpCode(int nArgs) {
        this.nArgs = nArgs;
    }
    public int nArgs() { return nArgs; }

    // convert byte value into an OpCode
    public static OpCode convert(byte value) {
        return OpCode.values()[value];
    }
}
