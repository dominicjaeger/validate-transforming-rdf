package com.validatingevolvingrdf;

import static java.lang.Character.isUpperCase;

public class Action {
    /**
     * Notation from the set-builder notation for the evaluation of the action, e.g.
     * A + phi_c is evaluated as { A(v) | v in [[phi_c]]^I}
     * + is the actionType
     * A is variableExpressionPart because A(v) is the variable expression
     * phi_c is predicatePart because v in [[phi_c]]^I is the predicate of the evaluation set
     */
    final public String variableExpressionPart;
    final public ActionType actionType;
    final public String predicatePart;
    public Action(String variableExpressionPart, ActionType actionType, String predicatePart) {
        this.variableExpressionPart = variableExpressionPart;
        this.actionType = actionType;
        this.predicatePart = predicatePart;
    }

    public boolean addsAClass() {
        int index = this.variableExpressionPart.indexOf("#");
        return isUpperCase(variableExpressionPart.charAt(index + 1));
    }

    public boolean addsAProperty() {
        return !this.addsAClass();
    }

    @Override
    public String toString() {
        return variableExpressionPart +
                (actionType.equals(ActionType.PLUS) ? " + " : " - ") +
                predicatePart;
    }

    enum ActionType {
        PLUS,
        MINUS
    }
}
