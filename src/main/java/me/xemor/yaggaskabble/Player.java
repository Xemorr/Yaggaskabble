package me.xemor.yaggaskabble;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pocketcombats.openskill.data.SimplePlayerResult;
import net.dv8tion.jda.api.JDA;

public class Player {

    @JsonProperty
    private long id;
    @JsonProperty
    private SkillRating goodSkill;
    @JsonProperty
    private SkillRating evilSkill;

    private Player() {}

    public static Player createNewPlayer(long id, double averageGoodRating, double averageEvilRating) {
        Player player = new Player();
        player.id = id;
        player.goodSkill = new SkillRating(25, 8.33333);
        player.evilSkill = new SkillRating(25, 8.33333);
        return player;
    }

    public Player updateSkill(Alignment alignment, SkillRating newSkill) {
        if (alignment == Alignment.GOOD) {
            goodSkill = newSkill;
        } else {
            evilSkill = newSkill;
        }
        return this;
    }
    
    public String shorthandSkillForAlignment(JDA bot, Alignment alignment) {
        SkillRating skill = alignment == Alignment.GOOD ? goodSkill : evilSkill;
        return "%s: %.0f - %.0f±%.0f".formatted(bot.getUserById(id).getAsMention(), skill.conservativeRating(), skill.mu() * 60, skill.std() * 60);
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
