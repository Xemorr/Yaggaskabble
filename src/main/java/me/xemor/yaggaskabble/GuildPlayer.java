package me.xemor.yaggaskabble;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pocketcombats.openskill.data.SimplePlayerResult;

public class GuildPlayer {

    @JsonProperty
    private long id;
    @JsonProperty
    private SkillRating goodSkill;
    @JsonProperty
    private SkillRating evilSkill;

    private GuildPlayer() {}

    public static GuildPlayer createNewPlayer(long id, double averageGoodRating, double averageEvilRating) {
        GuildPlayer guildPlayer = new GuildPlayer();
        guildPlayer.id = id;
        guildPlayer.goodSkill = new SkillRating(25, 8.33333);
        guildPlayer.evilSkill = new SkillRating(25, 8.33333);
        return guildPlayer;
    }

    public GuildPlayer updateSkill(Alignment alignment, SkillRating newSkill) {
        if (alignment == Alignment.GOOD) {
            goodSkill = newSkill;
        } else {
            evilSkill = newSkill;
        }
        return this;
    }

    public String shorthandSkillForAlignment(String name, Alignment alignment) {
        SkillRating skill = alignment == Alignment.GOOD ? goodSkill : evilSkill;
        String apostropheOrApostropheS = name.charAt(name.length() - 1) == 's' ? "\'" : "'s";
        return "%s%s Points: %.0f".formatted(name, apostropheOrApostropheS, skill.conservativeRating());
    }

    public String shorthandFullSkillForAlignment(String name, Alignment alignment) {
        SkillRating skill = alignment == Alignment.GOOD ? goodSkill : evilSkill;
        String apostropheOrApostropheS = name.charAt(name.length() - 1) == 's' ? "\'" : "'s";
        return "%s%s Points: %.0f, Rating: %.0f±%.0f".formatted(name, apostropheOrApostropheS, skill.conservativeRating(), skill.mu() * 60, skill.std() * 60);
    }

    public String shorthandSkillForCombined(String name) {
        String apostropheOrApostropheS = name.charAt(name.length() - 1) == 's' ? "\'" : "'s";
        return "%s%s Points: %.0f".formatted(name, apostropheOrApostropheS, goodSkill.conservativeRating() + evilSkill.conservativeRating());
    }

    public SimplePlayerResult<Long> getSimplePlayerResultForAlignment(Alignment alignment) {
        return alignment == Alignment.GOOD ? new SimplePlayerResult<>(id, goodSkill.mu(), goodSkill.std()) :
                new SimplePlayerResult<>(id, evilSkill.mu(), evilSkill.std());
    }

    public SkillRating getSkillRatingForAlignment(Alignment alignment) {
        return alignment == Alignment.GOOD ? goodSkill : evilSkill;
    }

    public long getId() {
        return id;
    }

    public SkillRating getGoodSkill() {
        return goodSkill;
    }

    public SkillRating getEvilSkill() {
        return evilSkill;
    }
}
