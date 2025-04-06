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
        boolean showLexerErrors = false;
        boolean showParserErrors = false;
        boolean showTypeCheckingErrors = false;

        showAsm = contains(args, "-asm");
        runAfterCompile = contains(args, "-run");
        trace = contains(args, "-trace");

        InputStream is = System.in;
        String outputFilename = "bytecodes.bc";

        if (args.length >= 1 && !args[0].startsWith("-")) {
            String inputFilename = args[0];
            if (!inputFilename.endsWith(".tuga")) {
                System.out.println("O ficheiro deve ter a extensão '.tuga'");
                System.exit(0);
            }
            outputFilename = inputFilename.replace(".tuga", ".bc");
            try {
                is = new FileInputStream(inputFilename);
            } catch (FileNotFoundException e) {
                System.out.println("File not found: " + inputFilename);
                return;
            }
        }

        try {
            CharStream input = CharStreams.fromStream(is);
            TugaLexer lexer = new TugaLexer(input);
            MyErrorListener errorListener = new MyErrorListener(showLexerErrors, showParserErrors);

            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);

            CommonTokenStream tokens;
            try {
                tokens = new CommonTokenStream(lexer);
                tokens.fill();
            } catch (RuntimeException lexException) {
                System.out.println("Input has lexical errors");
                return;
            }

            if (errorListener.getNumLexerErrors() > 0) {
                System.out.println("Input has lexical errors");
                return;
            }

            TugaParser parser = new TugaParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            ParseTree tree = parser.prog();

            if (errorListener.getNumParsingErrors() > 0) {
                System.out.println("Input has parsing errors");
                return;
            }

            TypeChecker checker = new TypeChecker();
            checker.visit(tree);
            if (checker.getTypeErrorCount() > 0) {
                System.out.println("Input has type checking errors");
                return;
            }

            ConstantPool constantPool = new ConstantPool();
            CodeGen codeGen = new CodeGen(checker, constantPool);
            codeGen.visit(tree);
            codeGen.saveBytecodes(outputFilename);

            constantPool.printConstants();
            codeGen.dumpCode();

            if (runAfterCompile || args.length == 0) {
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
