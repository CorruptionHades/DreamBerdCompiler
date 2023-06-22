package me.corruptionhades.dreambeard;

import me.corruptionhades.dreambeard.structure.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    private static File file;
    private static final String INDENTATION = "   ";
    private static final boolean debug = false;
    private static final List<Var> variables = new ArrayList<>();
    private static final List<Function> functions = new ArrayList<>();
    private static final List<String> deletionList = new ArrayList<>();
    private static final List<Statement> lines = new ArrayList<>();

    private static int index = 1;

    public static void parse(File inputFile) {
        file = inputFile;

        setupLines(inputFile);
        getFunctions(inputFile);

        System.out.println(">>>> Start of parsing file <<<<");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            index = 1;
            while ((line = reader.readLine()) != null) {

                if(index > lines.size()) {
                    System.out.println(">>>> Reached end of file <<<<");
                    break;
                }

                if(line.endsWith("!") || line.endsWith("?") || line.endsWith("{") || line.endsWith("}") || line.isEmpty()) {
                    Statement statement = getStatement(index);
                    if(statement == null) {
                        System.out.println("ERROR: Invalid statement: " + index);
                        continue;
                    }
                    executeStatement(statement);
                }
                else System.out.println("ERROR: \"!\" or \"?\" expected at line: " + index + "[" + line + "]");

                index++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setupLines(File inputFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            int index = 1;
            while ((line = reader.readLine()) != null) {
                lines.add(new Statement(line.replaceAll("^\\s+", ""), index));
                index++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void executeStatement(Statement statement) {

        String line = statement.getCode();
        if(line.isEmpty()) return;

        for (String s : deletionList) {
            if(line.contains(s)) {
                System.out.println("ERROR: ->" + s + "<- is invalid due to deletion!");
                return;
            }
        }

        if (line.endsWith("?")) {
            debugStatement(statement);
        }
        else if (line.startsWith("var var ")) {
            processVarVar(statement);
        }
        else if (line.startsWith("const const ")) {
            processConstConst(statement);
        }
        else if (line.startsWith("var const ")) {
            processVarConst(statement);
        }
        else if (line.startsWith("const var ")) {
            processConstVar(statement);
        }
        else if (line.startsWith("if")) {
            processIfStatement(statement);
        }
        else if (line.startsWith("print(")) {
            processPrintStatement(statement);
        }
        else if(line.startsWith("delete")) {
            processDeletion(statement);
        }

        for (String varName : getVarNames()) {
            if(line.startsWith(varName) && !line.startsWith(varName + ".")) {
                processVarReassign(statement);
            }
            else if(line.startsWith(varName + ".")) {
                processVarChange(statement);
            }
        }

        for (Function function : functions) {
            if(line.startsWith(function.getName() + "(")) {
                processFunction(statement);
            }
        }
    }

    private static void debugStatement(Statement lin) {
        String line = lin.getCode();
        String debugMessage = line.substring(1).trim();
        System.out.println("Debug: " + debugMessage);
    }

    private static void processConstConst(Statement lin) {
        String line = lin.getCode();
        String name = line.substring(line.indexOf("const const ") + 12, line.indexOf("=")).trim();
        String value = line.substring(line.indexOf("=") + 1, line.lastIndexOf("!")).trim().replace("\"", "");
        variables.add(new Var(name, value, Var.Type.ConstConst));
        if(debug) System.out.println("Created constant variable: " + name + " = " + value);
    }

    private static void processVarConst(Statement lin) {
        String line = lin.getCode();
        String name = line.substring(line.indexOf("var const ") + 10, line.indexOf("=")).trim();
        String value = line.substring(line.indexOf("=") + 1, line.lastIndexOf("!")).trim().replace("\"", "");
        variables.add(new Var(name, value, Var.Type.VarConst));
        if(debug) System.out.println("Created variable: " + name + " = " + value);
    }

    private static void processVarVar(Statement lin) {
        String line = lin.getCode();
        String name = line.substring(line.indexOf("var var ") + 8, line.indexOf("=")).trim();
        String value = line.substring(line.indexOf("=") + 1, line.lastIndexOf("!")).trim().replace("\"", "");
        variables.add(new Var(name, value, Var.Type.VarVar));
        if(debug) System.out.println("Created variable: " + name + " = " + value);
    }

    private static void processConstVar(Statement lin) {
        String line = lin.getCode();
        String name = line.substring(line.indexOf("const var ") + 10, line.indexOf("=")).trim();
        String value = line.substring(line.indexOf("=") + 1, line.lastIndexOf("!")).trim().replace("\"", "");
        variables.add(new Var(name, value, Var.Type.ConstVar));
        if(debug) System.out.println("Created constant: " + name + " = " + value);
    }

    private static void processPrintStatement(Statement lin) {
        String line = lin.getCode();
        String content = line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim().replace("\"", ""); // Extract the content

        // Replace variable references with their values
        for (Var variable : variables) {
            content = content.replace("${" + variable.getVariableName() + "}", variable.getVariableValue().toString());
        }

        System.out.println(content);
    }

    private static void processVarReassign(Statement lin) {
        String line = lin.getCode();
        String[] split = line.split("=");
        String variableName = split[0].trim();
        Var var = getByName(variableName);

        if(var == null) {
            System.out.println("ERROR: No variable with name >>" + variableName + "<< !");
            return;
        }

        if(var.getType() == Var.Type.ConstConst) {
            System.out.println("ERROR: Variable >>" + variableName + "<< is a const const and cannot be changed in any way!");
            return;
        }
        else if(var.getType() == Var.Type.ConstVar) {
            System.out.println("ERROR: Variable >>" + variableName + "<< is a const var and cannot be reassigned!");
            return;
        }

        // String name = line.substring(line.indexOf("var const ") + 10, line.indexOf("=")).trim();

        String newVal = line.substring(line.indexOf("=") + 2, line.indexOf("!")).replace("\"", "");
        var.setVariableValue(newVal);
        if(debug) System.out.println("Variable >>" + variableName + "<< has now been assigned to " + newVal);
    }

    private static void processVarChange(Statement lin) {
        String line = lin.getCode();
        String[] split = line.split("\\.");
        String variableName = split[0].trim();
        Var var = getByName(variableName);

        if(var == null) {
            System.out.println("ERROR: No variable with name >>" + variableName + "<< !");
            return;
        }

        if(var.getType() == Var.Type.ConstConst) {
            System.out.println("ERROR: Variable >>" + variableName + "<< is a const const and cannot be changed in any way!");
            return;
        }
        if(var.getType() == Var.Type.VarConst) {
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
            int index = 0;
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
                    List<Statement> code = new ArrayList<>();
                    code.add(getStatement(index));

                    Function function = new Function(functionName, code.toArray(new Statement[0]));
                    functions.add(function);
                    if (debug) {
                        System.out.println("Function <<" + functionName + ">> has been added!");
                        System.out.println("Function <<" + functionName + ">> Code: " + input);
                    }
                }
                index++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void processFunction(Statement lin) {
        String line = lin.getCode();
        String functionName = line.substring(0, line.indexOf("()"));
        Function function = getFunctionByName(functionName);
        if(function == null) {
            System.out.println("ERROR: function with name <<" + functionName + ">> doesnt not exist!");
            return;
        }
        for (Statement s : function.getCode()) {
            executeStatement(s);
        }
    }

    private static void processDeletion(Statement lin) {
        String line = lin.getCode();
        String value = line.replace("delete ", "").replace("!", "");
        deletionList.add(value);
    }

    private static void processIfStatement(Statement statement) {
        String line = statement.getCode();

        // Check condition
        String conditionText = line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim();
        boolean condition = false;
        conditionText = applyVariables(conditionText);

        if(conditionText.contains("==")) {
            String var1 = conditionText.substring(0, conditionText.indexOf("==")).trim();
            String var2 = conditionText.substring(conditionText.indexOf("==") + 2).trim();
            condition = var1.equals(var2);
            if(debug) {
                System.out.println("Condition of if statement: " + condition);
                System.out.println(var1 + (condition ? " is " : " isn't ") + var2);
            }
        }

        int skip = processCodeBlock(statement, /*evaluateCondition(condition)*/ condition);
        skipTo(skip);
    }

    private static int processCodeBlock(Statement statement, boolean execute) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean insideCodeBlock = true;

            int currentLine = 1;
            while (currentLine < statement.getLine() + 1) {
                reader.readLine();
                currentLine++;
            }

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Check for the end of the code block
                if (line.equals("}")) {
                    insideCodeBlock = false;
                    break;
                }

                // Process each line within the code block
                Statement lineStatement = getStatement(currentLine);
                if(lineStatement == null) {
                    System.out.println("ERROR: Invalid statement! " + currentLine);
                    continue;
                }

                if(execute) executeStatement(lineStatement);
                currentLine++;
            }

            if (insideCodeBlock) {
                System.out.println("ERROR: Missing closing brace '}' for code block!");
            }

            return currentLine;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return statement.getLine();
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

    private static Statement getStatement(int index) {
        for (Statement statement : lines) {
            if(statement.getLine() == index)
                return statement;
        }

        return null;
    }

    private static String applyVariables(String text) {
        for (Var variable : variables) {
            String variableName = variable.getVariableName();
            String variableValue = variable.getVariableValue().toString();

            // Create a regular expression pattern with delimiters
            String pattern = "\\b" + variableName + "\\b";

            // Replace exact variable matches using the pattern
            text = text.replaceAll(pattern, variableValue);
        }
        return text;
    }


    private static void skipTo(int line) {
        index = line;
    }
}
