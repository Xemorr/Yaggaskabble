package me.xemor.yaggaskabble.commands;

import me.xemor.yaggaskabble.Alignment;
import me.xemor.yaggaskabble.GuildPlayer;
import me.xemor.yaggaskabble.GuildPlayers;
import me.xemor.yaggaskabble.Yaggaskabble;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
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
        if (event.getGuild() == null) return;

        GuildPlayers guildPlayers = yaggaskabble.getGuildPlayers(event.getGuild().getIdLong());

        String alignmentString = event.getOption("alignment") == null ? "combined" : event.getOption("alignment").getAsString();
        String leaderboardString;
        String title;
        if (alignmentString.toUpperCase().equals("COMBINED")) {
            leaderboardString = generateCombinedLeaderboard(guildPlayers, event.getGuild());
            title = "Top 10 Points Leaderboard";
        }
        else {
            Alignment alignment;
            try {
                alignment = Alignment.valueOf(alignmentString.toUpperCase());
            } catch (IllegalArgumentException e) {
                event.reply("You have entered an invalid winning alignment!").queue();
                return;
            }
            leaderboardString = generateAlignmentLeaderboard(guildPlayers, event.getGuild(), alignment);
            title = alignment.getEmojiString() + " Top 10 Points Leaderboard";
        }

        // Build and send the embed
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title)
                .setColor(Color.MAGENTA)
                .setFooter("Gain points by playing more often, or winning games.")
                .addField(title, leaderboardString, true);

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    public String generateCombinedLeaderboard(GuildPlayers guildPlayers, Guild guild) {
        List<GuildPlayer> players = guildPlayers.getPlayers();

        players.sort(Comparator.comparingDouble(guildPlayer -> guildPlayer.getSkillRatingForAlignment(Alignment.GOOD).conservativeRating() +
                                                          guildPlayer.getSkillRatingForAlignment(Alignment.EVIL).conservativeRating()));
        Collections.reverse(players);

        return players.stream()
                .map((p) -> p.shorthandSkillForCombined(guild.retrieveMemberById(p.getId()).complete().getEffectiveName()))
                .limit(10)
                .collect(Collectors.joining("\n"));
        

    }

    public String generateAlignmentLeaderboard(GuildPlayers guildPlayers, Guild guild, Alignment alignment) {
        List<GuildPlayer> players = guildPlayers.getPlayers();

        players.sort(Comparator.comparingDouble(guildPlayer -> guildPlayer.getSkillRatingForAlignment(alignment).conservativeRating()));
        Collections.reverse(players);

        return players.stream()
                .map((p) -> p.shorthandSkillForAlignment(guild.retrieveMemberById(p.getId()).complete().getEffectiveName(), alignment))
                .limit(10)
                .collect(Collectors.joining("\n"));
    }
}
