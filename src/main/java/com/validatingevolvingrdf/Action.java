package com.validatingevolvingrdf;

import static java.lang.Character.isUpperCase;

public class Action {
    enum ActionType {
        PLUS,
        MINUS
    }
    final public String newResource;
    final public ActionType actionType;
    final public String concept;

    public Action(String newResource, ActionType actionType, String concept) {
        this.newResource = newResource;
        this.actionType = actionType;
        this.concept = concept;
    }

    public boolean addsAClass() {
        int index = this.newResource.indexOf("#");
        return isUpperCase(newResource.charAt(index+1));
    }

    public boolean addsAProperty() {
        return !this.addsAClass();
    }

    @Override
    public String toString() {
        return newResource +
                (actionType.equals(ActionType.PLUS) ? " + " : " - ") +
                concept;
    }
}
