package me.xemor.yaggaskabble;

public record SkillRating(double mu, double std) {

    public double conservativeRating() {
        return Math.max(60 * (mu - 3 * std), 0); // multiply by 60, so it is of the same scale as chess elo
    }

}
