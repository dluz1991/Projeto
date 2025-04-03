package VM;

/*
  Instruction codes of the virtual machine
*/

public enum OpCode {
    // single-byte instructions, just the OpCode: no arguments
    //Integer instructions
    iconst   (1),
    iuminus  (0),
    iadd     (0),
    isub     (0),
    imult    (0),
    idiv     (0),
    imod (0),
    ilt   (0),
    igreater (0),
    ilessequal (0),
    igreaterequal (0),
    ieq   (0),
    idifferent (0),
    itos   (0),
    itod  (0),
    iprint   (0),

    //String instructions
    sconst   (1),
    sconcat   (0),
    sprint   (0),
    seq   (0),
    sdifferent (0),

    //Real instructions
    dconst   (1),
    duminus  (0),
    dadd     (0),
    dsub     (0),
    dmult    (0),
    ddiv     (0),
    dmod (0),
    dlt(0),
    dgreater (0),
    dlessequal (0),
    dgreaterequal (0),
    deq   (0),
    ddifferent (0),
    dtos   (0),
    dprint   (0),

    //Boolean instructions
    tconst   (0),
    fconst   (0),
    bnot     (0),
    and     (0),
    bor      (0),
    beq   (0),
    bdifferent (0),
    btos (0),
    bprint   (0),

    halt     (0) // stop execution
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
