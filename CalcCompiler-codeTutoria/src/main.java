import CodeGenerator.CodeGen;
import ConstantPool.ConstantPool;
import CodeGenerator.TypeChecker;
import VM.VirtualMachine;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.*;

import Calc.*;

public class main {
    static boolean showAsm; // flag para mostrar bytecode gerado
    static boolean runAfterCompile;
    static boolean trace;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java calCompiler <ficheiro>.tuga [-asm] [-run]");
            System.exit(0);
        }

        String inputFilename = args[0];
        showAsm = contains(args, "-asm");
        runAfterCompile = contains(args, "-run");
        trace = contains(args, "-trace");

        if (!inputFilename.endsWith(".tuga")) {
            System.out.println("O ficheiro deve ter a extensão '.tuga'");
            System.exit(0);
        }

        String outputFilename = inputFilename.replace(".tuga", ".tugabc");

        try {
            // Fase de compilação
            InputStream is = new FileInputStream(inputFilename);
            CharStream input = CharStreams.fromStream(is);
            CalcLexer lexer = new CalcLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            CalcParser parser = new CalcParser(tokens);
            ParseTree tree = parser.prog();

            int numParsingErrors = parser.getNumberOfSyntaxErrors();
            if (numParsingErrors != 0) {
                System.out.println(inputFilename + " tem " + numParsingErrors + " erros sintáticos");
                return;
            }

            // Verificação de tipos
            TypeChecker checker = new TypeChecker();
            checker.visit(tree);

            // Geração de bytecodes com constant pool
            ConstantPool constantPool = new ConstantPool();
            CodeGen codeGen = new CodeGen(checker, constantPool);
            codeGen.visit(tree);
            codeGen.saveBytecodes(outputFilename);
//_______PRINT_CONSTANTS___________
            constantPool.printConstants();
            codeGen.dumpCode();


            // Execução na VM (opcional)
            if (runAfterCompile) {
                byte[] bytecodes = loadBytecodes(outputFilename);
                VirtualMachine vm = new VirtualMachine(bytecodes, trace, constantPool);
                vm.run();
            }

        } catch (IOException e) {
            System.out.println("Erro ao ler o ficheiro: " + e.getMessage());
        }
    }

    public static boolean contains(String[] args, String flag) {
        for (String arg : args) {
            if (arg.equals(flag)) return true;
        }
        return false;
    }

    public static byte[] loadBytecodes(String filename) throws IOException {
        File file = new File(filename);
        byte[] bytecodes = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(bytecodes);
        }
        return bytecodes;
    }
}
