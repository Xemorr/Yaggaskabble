package me.xemor.yaggaskabble;

import me.xemor.yaggaskabble.commands.LeaderboardCommand;
import me.xemor.yaggaskabble.commands.RatingCommand;
import me.xemor.yaggaskabble.commands.RegisterGameCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.apache.commons.collections4.map.LRUMap;

import java.io.IOException;

public class Yaggaskabble {

    private final JDA bot;
    private LRUMap<Long, GuildPlayers> guildToGuildPlayers;

    public Yaggaskabble(String token) throws InterruptedException {
        this.guildToGuildPlayers = new LRUMap<>(100);
        bot = JDABuilder.create(token, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_EXPRESSIONS)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build()
                .awaitReady();
        registerCommands(bot);
        bot.addEventListener(new RegisterGameCommand(this));
        bot.addEventListener(new LeaderboardCommand(this));
        bot.addEventListener(new RatingCommand(this));
    }

    public GuildPlayers getGuildPlayers(long guildId) {
        return guildToGuildPlayers.computeIfAbsent(guildId, (innerGuildId) -> {
            try {
                return new GuildPlayers(bot, innerGuildId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void registerCommands(JDA bot) {
        bot.upsertCommand("registergame", "Say who won / lost") // global commands are slow
                .addOption(OptionType.STRING, "good", "Mention good users", true)
                .addOption(OptionType.STRING, "evil", "Mention evil users", true)
                .addOption(OptionType.STRING, "winner", "Who won", true)
                .queue();
        bot.upsertCommand("leaderboard", "The leaderboard") // global commands are slow
                .addOption(OptionType.STRING, "alignment", "The alignment you want to see the leaderboard for", false)
                .queue();
        bot.upsertCommand("rating", "Your rating") // global commands are slow
                .addOption(OptionType.STRING, "alignment", "The alignment you want to see the leaderboard for", false)
                .queue();
    }

}
