package me.corruptionhades.dreambeard.structure;

public class Var {

    private final String variableName;
    private Object variableValue;

    public Var(String variableName, Object variableValue) {
        this.variableName = variableName;
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
}
