package me.xemor.yaggaskabble;

public enum Alignment {

    GOOD,EVIL,BOTH;

    public String getEmojiString() {
        return this == Alignment.GOOD ? "😇 Good" : this == Alignment.EVIL ? "😈 Evil" : "Combined";
    }

}
