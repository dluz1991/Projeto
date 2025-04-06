package ConstantPool;


import java.util.*;

public class ConstantPool {
    private final List<Object> constants = new ArrayList<>();

    public int addDouble(double value) {
        // evitar duplicados (opcional)
        for (int i = 0; i < constants.size(); i++) {
            if (constants.get(i) instanceof Double && ((Double) constants.get(i)).equals(value)) {
                return i;
            }
        }
        constants.add(value);
        return constants.size() - 1;
    }

    public int addString(String value) {
        // evitar duplicados (opcional)
        for (int i = 0; i < constants.size(); i++) {
            if (constants.get(i) instanceof String && constants.get(i).equals(value)) {
                return i;
            }
        }
        constants.add(value);
        return constants.size() - 1;
    }

    // devolve e remove o valor da posição
    public Object get(int index) {
        if (index >= 0 && index < constants.size()) {
            return constants.get(index);
        }
        return null;
    }

    public int getStringLength(int index) {
        Object obj = constants.get(index);
        if (obj instanceof String) {
            return ((String) obj).length();
        }
        return -1;
    }

    public int size() {
        return constants.size();
    }

    public void printConstants() {
        System.out.println("*** Constant pool ***");
        for (int i = 0; i < constants.size(); i++) {
            Object obj = constants.get(i);
            if (obj instanceof String) {
                System.out.printf("%d: \"%s\"%n", i, obj);
            } else if (obj instanceof Double) {
                System.out.printf(Locale.US, "%d: %s%n", i, obj);
            }
        }
    }
}

