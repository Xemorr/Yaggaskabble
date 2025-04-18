package me.xemor;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Player {

    @JsonProperty
    private SkillRating goodSkill;
    @JsonProperty
    private SkillRating badSkill;

    public Player updateSkill(Alignment alignment, SkillRating newSkill) {
        if (alignment == Alignment.GOOD) {
            goodSkill = newSkill;
        } else {
            badSkill = newSkill;
        }
        return this;
    }

    public SkillRating getGoodSkill() {
        return goodSkill;
    }

    public SkillRating getBadSkill() {
        return badSkill;
    }
}
