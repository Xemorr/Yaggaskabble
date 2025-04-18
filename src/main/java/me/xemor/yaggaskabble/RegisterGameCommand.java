package me.xemor;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class RegisterGameCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("align")) return;

        // Retrieve the user lists
        List<User> goodUsers = event.getOption("good").getMentions().getUsers();
        List<User> evilUsers = event.getOption("evil").getMentions().getUsers();

        // Format the user lists
        String goodList = goodUsers.isEmpty() ?
                "*None*" :
                goodUsers.stream().map(User::getAsMention).collect(Collectors.joining("\n"));

        String evilList = evilUsers.isEmpty() ?
                "*None*" :
                evilUsers.stream().map(User::getAsMention).collect(Collectors.joining("\n"));

        // Build and send the embed
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🧭 Alignment Report")
                .setColor(Color.MAGENTA)
                .addField("😇 Good", goodList, true)
                .addField("😈 Evil", evilList, true)
                .setFooter("May the best side win!");

        event.replyEmbeds(embed.build()).queue();
    }
}
