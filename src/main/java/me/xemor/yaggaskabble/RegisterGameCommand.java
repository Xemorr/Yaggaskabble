package me.xemor.yaggaskabble;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class RegisterGameCommand extends ListenerAdapter {

    private final Yaggaskabble yaggaskabble;

    public RegisterGameCommand(Yaggaskabble yaggaskabble) {
        this.yaggaskabble = yaggaskabble;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("registergame")) return;

        // Retrieve the user lists
        List<User> goodUsers = event.getOption("good").getMentions().getUsers();
        List<User> evilUsers = event.getOption("evil").getMentions().getUsers();

        if (goodUsers.size() < 2 || evilUsers.size() < 2) {
            event.reply("There must be at least two players a side!").queue();
            return;
        }

        Alignment winningAlignment;

        try {
            String winner = event.getOption("winner").getAsString();
            winningAlignment = Alignment.valueOf(winner.toUpperCase());
        } catch (IllegalArgumentException e) {
            event.reply("You have entered an invalid winning alignment!").queue();
            return;
        }

        // Format the user lists
        GameInfo gameInfo = yaggaskabble.registerGameAndRecalculateSkillRatings(goodUsers.stream().map(User::getIdLong).toList(), evilUsers.stream().map(User::getIdLong).toList(), winningAlignment);

        String goodString = goodUsers.stream()
                .map(User::getIdLong)
                .map(yaggaskabble::getPlayer)
                .map((p) -> p.shorthandSkillForAlignment(yaggaskabble.getBot(), Alignment.GOOD))
                .collect(Collectors.joining("\n"));

        String evilString = evilUsers.stream().map(User::getIdLong)
                .map(yaggaskabble::getPlayer)
                .map((p) -> p.shorthandSkillForAlignment(yaggaskabble.getBot(), Alignment.EVIL))
                .collect(Collectors.joining("\n"));

        // Build and send the embed
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🧭 Game Report")
                .setColor(Color.MAGENTA)
                .addField("😇 Good", goodString, true)
                .addField("😈 Evil", evilString, true)
                .setFooter(winningAlignment.name() + " has won! " + "Quality: %.2f, Good Win Chance %.2f".formatted( gameInfo.quality(), gameInfo.goodWinProbability()));

        event.replyEmbeds(embed.build()).queue();
    }
}
