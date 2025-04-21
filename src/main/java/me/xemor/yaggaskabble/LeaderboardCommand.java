package me.xemor.yaggaskabble;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LeaderboardCommand extends ListenerAdapter {

    private final Yaggaskabble yaggaskabble;

    public LeaderboardCommand(Yaggaskabble yaggaskabble) {
        this.yaggaskabble = yaggaskabble;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("leaderboard")) return;

        String alignmentString = event.getOption("alignment").getAsString();
        String leaderboardString;
        String title;
        if (alignmentString.toUpperCase().equals("COMBINED")) {
            leaderboardString = generateCombinedLeaderboard();
            title = "Combined Leaderboard";
        }
        else {
            Alignment alignment;
            try {
                alignment = Alignment.valueOf(alignmentString.toUpperCase());
            } catch (IllegalArgumentException e) {
                event.reply("You have entered an invalid winning alignment!").queue();
                return;
            }
            leaderboardString = generateAlignmentLeaderboard(alignment);
            title = alignment.getEmojiString() + " Leaderboard";
        }

        // Build and send the embed
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title)
                .setColor(Color.MAGENTA)
                .addField(title, leaderboardString, true);

        event.replyEmbeds(embed.build()).queue();
    }

    public String generateCombinedLeaderboard() {
        List<Player> players = yaggaskabble.getPlayers();

        players.sort(Comparator.comparingDouble(player -> player.getSkillRatingForAlignment(Alignment.GOOD).conservativeRating() +
                                                          player.getSkillRatingForAlignment(Alignment.EVIL).conservativeRating()));
        Collections.reverse(players);
        return players.stream()
                .map((p) -> p.shorthandSkillForCombined(yaggaskabble.getBot()))
                .collect(Collectors.joining("\n"));
        

    }

    public String generateAlignmentLeaderboard(Alignment alignment) {
        List<Player> players = yaggaskabble.getPlayers();

        players.sort(Comparator.comparingDouble(player -> player.getSkillRatingForAlignment(alignment).conservativeRating()));
        Collections.reverse(players);
        return players.stream()
                .map((p) -> p.shorthandSkillForAlignment(yaggaskabble.getBot(), alignment))
                .collect(Collectors.joining("\n"));
    }
}
