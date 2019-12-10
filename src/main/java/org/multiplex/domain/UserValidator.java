package org.multiplex.domain;

class UserValidator {

    public boolean isInvalid(String name, String surname) {
        return !isUserNameValid(name) || !isUserSurnameValid(surname);
    }

    private boolean isUserNameValid(String name) {
        return name.length() >= 3 && Character.isUpperCase(name.codePointAt(0));
    }

    private boolean isUserSurnameValid(String surname) {
        String[] surnameParts = surname.split("-");

        if (surnameParts.length > 2 || surnameParts.length == 0) return false;

        return surnameParts[0].length() >= 3
                && Character.isLowerCase(surnameParts[0].codePointAt(0))
                && (surnameParts.length < 2 || Character.isUpperCase(surnameParts[1].codePointAt(0)));
    }
}
