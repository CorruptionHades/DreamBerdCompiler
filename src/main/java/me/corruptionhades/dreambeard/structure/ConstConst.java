package me.corruptionhades.dreambeard.structure;

public class ConstConst extends Var {

    public ConstConst(String variableName, Object variableValue) {
        super(variableName, variableValue);
    }

    @Override
    public void setVariableValue(Object variableValue) {
        throw new UnsupportedOperationException("Constant Constant cant be set!");
    }
}
