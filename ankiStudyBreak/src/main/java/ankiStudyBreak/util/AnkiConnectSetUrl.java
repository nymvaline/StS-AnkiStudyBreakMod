package ankiStudyBreak.util;

import basemod.interfaces.TextReceiver;
import ankiStudyBreak.AnkiStudyBreak;

import java.util.Arrays;

public class AnkiConnectSetUrl implements TextReceiver {

    @Override
    public String getCurrentText() {
        return AnkiStudyBreak.ankiConnectUrlString;
    }

    @Override
    public void setText(String s) {
        AnkiStudyBreak.ankiConnectUrlString = s;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean acceptCharacter(char c) {
        if (Arrays.asList('$', '-', '_', '.', '+', '!', '*', '(', ')', ':', '/').contains(c)) {
            return true;
        } else if (Character.isDigit(c) || Character.isAlphabetic(c)) {
            return true;
        }
        return false;
    }
}
