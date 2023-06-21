package me.corruptionhades.dreambeard.structure;

public class Function {

    private final String name;
    private final String[] code;

    public Function(String name, String[] code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String[] getCode() {
        return code;
    }
}
