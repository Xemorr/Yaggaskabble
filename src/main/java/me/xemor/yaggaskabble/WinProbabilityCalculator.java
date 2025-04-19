package me.xemor.yaggaskabble;

import com.pocketcombats.openskill.RatingModelConfig;
import com.pocketcombats.openskill.data.SimpleTeamResult;

public class WinProbabilityCalculator {

    public static double calculateGoodWinProbability(SimpleTeamResult<Long> goodTeam, SimpleTeamResult<Long> evilTeam, RatingModelConfig config) {
        double goodTeamSigmaSquared = goodTeam.sigma() * goodTeam.sigma();
        double evilTeamSigmaSquared = evilTeam.sigma() * evilTeam.sigma();
        // This variable represents a combined standard deviation (σ) measure, incorporating the variances of
        // two competing teams, crucial for determining the likelihood of one team winning over another.
        double cIq = Math.sqrt(goodTeamSigmaSquared + evilTeamSigmaSquared + 2 * config.beta() * config.beta());
        // The probability that team beats opponent, derived from the logistic function applied to the
        // difference in team skills
        return 1 / (1 + Math.exp((evilTeam.mu() - goodTeam.mu()) / cIq));
    }

}
