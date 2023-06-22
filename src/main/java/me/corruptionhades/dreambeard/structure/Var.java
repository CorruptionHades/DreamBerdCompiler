package me.corruptionhades.dreambeard.structure;

public class Var {

    private final String variableName;
    private final Type type;
    private Object variableValue;

    public Var(String variableName, Object variableValue, Type type) {
        this.variableName = variableName;
        this.type = type;
        this.variableValue = variableValue;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableValue(Object variableValue) {
        this.variableValue = variableValue;
    }

    public Object getVariableValue() {
        return variableValue;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        ArrayVar, ConstConst, ConstVar, VarConst, VarVar;
    }
}
