import CodeGenerator.CodeGen;
import ConstantPool.ConstantPool;
import CodeGenerator.TypeChecker;
import VM.VirtualMachine;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.*;

import Tuga.*;

public class TugaCompileAndRun {
    static boolean showAsm; // flag para mostrar bytecode gerado
    static boolean runAfterCompile;
    static boolean trace;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java calCompiler <ficheiro>.tuga [-asm] [-run]");
            System.exit(0);
        }
        //Flags para mostrar erros
        boolean showLexerErrors = false; //Mensagem de erro "Input has lexical errors"
        boolean showParserErrors = false;   //Mensagem de erro "Input has parsing errors"
        boolean showTypeCheckingErrors = false; //Mensagem de erro"Input has type checking errors"
        //Após mensagem de erro é suposto o programa terminar


        String inputFilename = args[0];
        showAsm = contains(args, "-asm");
        runAfterCompile = contains(args, "-run");
        trace = contains(args, "-trace");

        if (!inputFilename.endsWith(".tuga")) {
            System.out.println("O ficheiro deve ter a extensão '.tuga'");
            System.exit(0);
        }

        String outputFilename = inputFilename.replace(".tuga", ".bc");

        try {
            // Fase de compilação
            InputStream is = new FileInputStream(inputFilename);
            CharStream input = CharStreams.fromStream(is);
            // Lexer setup with error catching
            TugaLexer lexer = new TugaLexer(input);
            MyErrorListener errorListener = new MyErrorListener(false, false);

            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);

            CommonTokenStream tokens;
            try {
                tokens = new CommonTokenStream(lexer);
                tokens.fill();  // Force lexer to process all tokens to catch lexer exceptions here
                if (errorListener.getNumLexerErrors() > 0) {
                    System.out.println("Input has lexical errors");
                    return;
                }
            } catch (RuntimeException lexException) {
                System.out.println("Input has lexical errors");
                return;
            }

// Parser setup
            TugaParser parser = new TugaParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            ParseTree tree = parser.prog();

            if (errorListener.getNumParsingErrors() > 0) {
                System.out.println("Input has parsing errors");
                return;
            }

            // Verificação de tipos
            TypeChecker checker = new TypeChecker();
            checker.visit(tree);
            if (checker.getTypeErrorCount() > 0) {
                // Found at least one type error => print the required message & stop
                System.out.println("Input has type checking errors");
                return;
            }

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
