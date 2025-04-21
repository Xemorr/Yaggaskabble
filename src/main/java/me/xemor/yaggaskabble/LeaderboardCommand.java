package me.xemor.yaggaskabble;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
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

        Alignment alignment;
        try {
            String alignmentString = event.getOption("alignment").getAsString();
            if (alignmentString.toUpperCase().equals("COMBINED")) {
                this.generateBoth(event);
                return;
            }
            alignment = Alignment.valueOf(alignmentString.toUpperCase());
        } catch (IllegalArgumentException e) {
            event.reply("You have entered an invalid winning alignment!").queue();
            return;
        }
        String leaderboardString;
        List<Player> players = yaggaskabble.getPlayers();

        players.sort(Comparator.comparingDouble(player -> player.getSkillRatingForAlignment(alignment).conservativeRating()));
        Collections.reverse(players);
        leaderboardString = players.stream()
                .map((p) -> p.shorthandSkillForAlignment(yaggaskabble.getBot(), alignment))
                .collect(Collectors.joining("\n"));
        
        // Build and send the embed
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(alignment.getEmojiString() + " Leaderboard")
                .setColor(Color.MAGENTA)
                .addField("", leaderboardString, true);

        event.replyEmbeds(embed.build()).queue();
    }

    public void generateBoth(SlashCommandInteractionEvent event) {
        String leaderboardString;
        List<Player> players = yaggaskabble.getPlayers();

        players.sort(Comparator.comparingDouble(player -> player.getSkillRatingForAlignment(Alignment.GOOD).conservativeRating() +
                                                          player.getSkillRatingForAlignment(Alignment.EVIL).conservativeRating()));
        Collections.reverse(players);
        leaderboardString = players.stream()
                .map((p) -> p.shorthandSkillForCombined(yaggaskabble.getBot()))
                .collect(Collectors.joining("\n"));
        
        // Build and send the embed
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Combined Leaderboard")
                .setColor(Color.MAGENTA)
                .addField("", leaderboardString, true);

        event.replyEmbeds(embed.build()).queue();
    }
}
