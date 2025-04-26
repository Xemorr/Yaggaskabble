package me.xemor.yaggaskabble.commands;

import me.xemor.yaggaskabble.Alignment;
import me.xemor.yaggaskabble.GameInfo;
import me.xemor.yaggaskabble.GuildPlayers;
import me.xemor.yaggaskabble.Yaggaskabble;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

public class RegisterGameCommand extends ListenerAdapter {

    private final Yaggaskabble yaggaskabble;

    public RegisterGameCommand(Yaggaskabble yaggaskabble) {
        this.yaggaskabble = yaggaskabble;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("registergame")) return;
        if (event.getGuild() == null) return;

        GuildPlayers guildPlayers = yaggaskabble.getGuildPlayers(event.getGuild().getIdLong());

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
        List<Long> goodUserIds = goodUsers.stream().map(User::getIdLong).toList();
        List<Long> evilUserIds = evilUsers.stream().map(User::getIdLong).toList();
        GameInfo gameInfo = guildPlayers.registerGameAndRecalculateSkillRatings(goodUserIds, evilUserIds, winningAlignment);

        event.reply("Quality: %.2f, Good Win Chance %.2f".formatted(gameInfo.quality(), gameInfo.goodWinProbability())).queue();
        event.getChannel().sendMessage(winningAlignment.name() + " has won!").queue();
    }
}
