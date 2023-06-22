package me.corruptionhades.dreambeard.structure;

public class Function {

    private final String name;
    private final Statement[] code;

    public Function(String name, Statement[] code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public Statement[] getCode() {
        return code;
    }
}
