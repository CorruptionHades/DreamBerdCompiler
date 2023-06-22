package me.corruptionhades.dreambeard.structure;

public class Statement {

    private final String code;
    private final int line;

    public Statement(String code, int line) {
        this.code = code;
        this.line = line;
    }

    public String getCode() {
        return code;
    }

    public int getLine() {
        return line;
    }
}
