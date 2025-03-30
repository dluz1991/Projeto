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
    iremainder (0),
    iless   (0),
    igreater (0),
    ilessequal (0),
    igreaterequal (0),
    iequal   (0),
    idifferent (0),
    iand     (0),
    ior      (0),
    inot     (0),
    itos   (0),
    itod  (0),
    iprint   (0),

    //String instructions
    sconst   (1),
    sconcat   (0),
    sprint   (0),

    //Real instructions
    dconst   (1),
    duminus  (0),
    dadd     (0),
    dsub     (0),
    dmult    (0),
    ddiv     (0),
    dremainder (0),
    dless   (0),
    dgreater (0),
    dlessequal (0),
    dgreaterequal (0),
    dequal   (0),
    ddifferent (0),
    dand     (0),
    dor      (0),
    dnot     (0),
    dtos   (0),
    dprint   (0),

    //Boolean instructions
    bconst   (1),
    bnot     (0),
    band     (0),
    bor      (0),
    bequal   (0),
    bdifferent (0),
    btos (0),
    bprint   (0),


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
