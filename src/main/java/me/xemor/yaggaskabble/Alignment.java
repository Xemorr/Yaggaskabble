package me.xemor.yaggaskabble;

public enum Alignment {

    GOOD,EVIL;

    public String getEmojiString() {
        return this == Alignment.GOOD ? "😇 Good" : "😈 Evil";
    }

}
