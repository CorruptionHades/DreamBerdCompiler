package me.corruptionhades.dreambeard.structure;

import java.util.ArrayList;
import java.util.List;

public class Function {

    private final String name;
    private final List<Statement> code = new ArrayList<>();

    public Function(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Statement> getCode() {
        return code;
    }

    public void addStatement(Statement statement) {
        code.add(statement);
    }
}
