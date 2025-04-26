package me.xemor.yaggaskabble.commands;

import me.xemor.yaggaskabble.Alignment;
import me.xemor.yaggaskabble.GuildPlayer;
import me.xemor.yaggaskabble.GuildPlayers;
import me.xemor.yaggaskabble.Yaggaskabble;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RatingCommand extends ListenerAdapter {

    private final Yaggaskabble yaggaskabble;

    public RatingCommand(Yaggaskabble yaggaskabble) {
        this.yaggaskabble = yaggaskabble;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("rating")) return;
        if (event.getGuild() == null) return;

        GuildPlayers guildPlayers = yaggaskabble.getGuildPlayers(event.getGuild().getIdLong());
        GuildPlayer guildPlayer = guildPlayers.getPlayer(event.getInteraction().getUser().getIdLong());

        String alignmentString = event.getOption("alignment") == null ? "combined" : event.getOption("alignment").getAsString();
        if (alignmentString.toUpperCase().equals("COMBINED")) {
            event.reply(guildPlayer.shorthandSkillForCombined(event.getInteraction().getUser().getEffectiveName())).setEphemeral(true).queue();
        }
        else {
            Alignment alignment;
            try {
                alignment = Alignment.valueOf(alignmentString.toUpperCase());
            } catch (IllegalArgumentException e) {
                event.reply("You have entered an invalid winning alignment!").queue();
                return;
            }
            event.reply(guildPlayer.shorthandFullSkillForAlignment(event.getInteraction().getUser().getEffectiveName(), alignment)).setEphemeral(true).queue();
        }
    }

}
