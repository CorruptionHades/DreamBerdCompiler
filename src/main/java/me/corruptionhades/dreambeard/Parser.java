package me.corruptionhades.dreambeard;

import me.corruptionhades.dreambeard.structure.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {

    private static final String INDENTATION = "   ";
    private static final boolean debug = false;
    private static final List<Var> variables = new ArrayList<>();
    private static final List<Function> functions = new ArrayList<>();
    private static final List<String> deletionList = new ArrayList<>();

    public static void parse(File inputFile) {

        getFunctions(inputFile);

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.isEmpty()) continue;
                if(line.endsWith("!") || line.endsWith("?") || line.endsWith("{") || line.endsWith("}")) executeStatement(line);
                else System.out.println("ERROR: \"!\" or \"?\" expected!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void executeStatement(String line) {

        for (String s : deletionList) {
            if(line.contains(s)) {
                System.out.println("ERROR: ->" + s + "<- is invalid due to deletion!");
                return;
            }
        }

        if (line.endsWith("?")) {
            debugStatement(line);
        }
        else if (line.startsWith("var var ")) {
            processVarVar(line);
        }
        else if (line.startsWith("const const ")) {
            processConstConst(line);
        }
        else if (line.startsWith("var const ")) {
            processVarConst(line);
        }
        else if (line.startsWith("const var ")) {
            processConstVar(line);
        }
        else if (line.startsWith("when ")) {
            //processWhenStatement(line);
        }
        else if (line.startsWith("print(")) {
            processPrintStatement(line);
        }
        else if(line.startsWith("delete")) {
            processDeletion(line);
        }

        for (String varName : getVarNames()) {
            if(line.startsWith(varName) && !line.startsWith(varName + ".")) {
                processVarReassign(line);
            }
            else if(line.startsWith(varName + ".")) {
                processVarChange(line);
            }
        }

        for (Function function : functions) {
            if(line.startsWith(function.getName() + "(")) {
                processFunction(line);
            }
        }
    }

    private static void debugStatement(String line) {
        String debugMessage = line.substring(1).trim();
        System.out.println("Debug: " + debugMessage);
    }

    private static void processConstConst(String line) {
        String name = line.substring(line.indexOf("const const ") + 12, line.indexOf("=")).trim();
        String value = line.substring(line.indexOf("=") + 1, line.lastIndexOf("!")).trim().replace("\"", "");
        variables.add(new ConstConst(name, value));
        if(debug) System.out.println("Created constant variable: " + name + " = " + value);
    }

    private static void processVarConst(String line) {
        String name = line.substring(line.indexOf("var const ") + 10, line.indexOf("=")).trim();
        String value = line.substring(line.indexOf("=") + 1, line.lastIndexOf("!")).trim().replace("\"", "");
        variables.add(new VarConst(name, value));
        if(debug) System.out.println("Created variable: " + name + " = " + value);
    }

    private static void processVarVar(String line) {
        String name = line.substring(line.indexOf("var var ") + 8, line.indexOf("=")).trim();
        String value = line.substring(line.indexOf("=") + 1, line.lastIndexOf("!")).trim().replace("\"", "");
        variables.add(new VarVar(name, value));
        if(debug) System.out.println("Created variable: " + name + " = " + value);
    }

    private static void processConstVar(String line) {
        String name = line.substring(line.indexOf("const var ") + 10, line.indexOf("=")).trim();
        String value = line.substring(line.indexOf("=") + 1, line.lastIndexOf("!")).trim().replace("\"", "");
        variables.add(new ConstVar(name, value));
        if(debug) System.out.println("Created constant: " + name + " = " + value);
    }

    private static void processWhenStatement(String line) {
        String condition = line.substring(line.indexOf("when (") + 6, line.indexOf(")")).trim();
        String action = line.substring(line.indexOf(")") + 2, line.lastIndexOf("!")).trim();
        executeStatement(action + "!");
        if(debug) System.out.println("When statement: If " + condition + " then " + action);
    }

    private static void processPrintStatement(String line) {
        String content = line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim().replace("\"", ""); // Extract the content

        // Replace variable references with their values
        for (Var variable : variables) {
            content = content.replace("${" + variable.getVariableName() + "}", variable.getVariableValue().toString());
        }

        System.out.println(content);
    }

    private static void processVarReassign(String line) {
        String[] split = line.split("=");
        String variableName = split[0].trim();
        Var var = getByName(variableName);

        if(var == null) {
            System.out.println("ERROR: No variable with name >>" + variableName + "<< !");
            return;
        }

        if(var instanceof ConstConst) {
            System.out.println("ERROR: Variable >>" + variableName + "<< is a const const and cannot be changed in any way!");
            return;
        }
        else if(var instanceof ConstVar) {
            System.out.println("ERROR: Variable >>" + variableName + "<< is a const var and cannot be reassigned!");
            return;
        }

        // String name = line.substring(line.indexOf("var const ") + 10, line.indexOf("=")).trim();

        String newVal = line.substring(line.indexOf("=") + 2, line.indexOf("!")).replace("\"", "");
        var.setVariableValue(newVal);
        if(debug) System.out.println("Variable >>" + variableName + "<< has now been assigned to " + newVal);
    }

    private static void processVarChange(String line) {
        String[] split = line.split("\\.");
        String variableName = split[0].trim();
        Var var = getByName(variableName);

        if(var == null) {
            System.out.println("ERROR: No variable with name >>" + variableName + "<< !");
            return;
        }

        if(var instanceof ConstConst) {
            System.out.println("ERROR: Variable >>" + variableName + "<< is a const const and cannot be changed in any way!");
            return;
        }
        else if(var instanceof VarConst) {
            System.out.println("ERROR: Variable >>" + variableName + "<< is a var const and cannot be edited!");
            return;
        }

        String functionName = line.substring(line.indexOf(".") + 1, line.indexOf("("));
        switch (functionName) {
            case "push" -> {
                String content = line.substring(line.indexOf("(") + 1, line.indexOf(")")).replace("\"", "");
                if(content.isEmpty()) break;
                var.setVariableValue(var.getVariableValue() + content);
                if(debug) System.out.println("For Variable >>" + variableName + "<< this has been pushed: " + content);
            }
            case "clear" -> {
                var.setVariableValue("");
                if(debug) System.out.println("Variable >> " + variableName + "<< has been cleared!");
            }
        }
    }

    private static void getFunctions(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("function ")) {
                    String functionName = line.substring("function ".length(), line.indexOf("()"));
                    StringBuilder functionCode = new StringBuilder();

                    try (BufferedReader reader2 = new BufferedReader(new FileReader(file))) {
                        String codeLine;
                        boolean insideFunction = false;
                        while ((codeLine = reader2.readLine()) != null) {
                            if (insideFunction) {
                                if (codeLine.trim().equals("}")) {
                                    break;
                                }
                                functionCode.append(codeLine).append("\n");
                            } else if (codeLine.trim().startsWith("function")) {
                                insideFunction = true;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String input = functionCode.toString();
                    List<String> code = new ArrayList<>();
                    for (String s : input.split("\n")) {
                        s = s.replaceAll("^\\s+", "");
                        code.add(s);
                    }

                    Function function = new Function(functionName, code.toArray(new String[0]));
                    functions.add(function);
                    if (debug) {
                        System.out.println("Function <<" + functionName + ">> has been added!");
                        System.out.println("Function <<" + functionName + ">> Code: " + input);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void processFunction(String line) {
        String functionName = line.substring(0, line.indexOf("()"));
        Function function = getFunctionByName(functionName);
        if(function == null) {
            System.out.println("ERROR: function with name <<" + functionName + ">> doesnt not exist!");
            return;
        }
        for (String s : function.getCode()) {
            executeStatement(s);
        }
    }

    private static void processDeletion(String line) {
        String value = line.replace("delete ", "").replace("!", "");
        deletionList.add(value);
    }

    private static Var getByName(String name) {
        for (Var variable : variables) {
            if(variable.getVariableName().equals(name)) return variable;
        }

        return null;
    }

    private static List<String> getVarNames() {
        List<String> names = new ArrayList<>();
        for (Var variable : variables) {
            names.add(variable.getVariableName());
        }

        return names;
    }

    private static Function getFunctionByName(String name) {
        for (Function function : functions) {
            if(function.getName().equals(name)) return function;
        }
        return null;
    }
}
