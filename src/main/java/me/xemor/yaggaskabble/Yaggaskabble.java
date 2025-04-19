package me.xemor.yaggaskabble;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocketcombats.openskill.Adjudicator;
import com.pocketcombats.openskill.QualityEvaluator;
import com.pocketcombats.openskill.RatingModelConfig;
import com.pocketcombats.openskill.data.RatingAdjustment;
import com.pocketcombats.openskill.data.SimplePlayerResult;
import com.pocketcombats.openskill.data.SimpleTeamResult;
import com.pocketcombats.openskill.model.PlackettLuce;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Yaggaskabble {

    private final JDA bot;
    private final Map<Long, Player> players = new HashMap<>();
    private List<Game> games = new ArrayList<>();
    private final File playersFile = new File("players.json");
    private final File gamesFile = new File("games.json");

    public Yaggaskabble(String token) throws IOException, InterruptedException {
        bot = JDABuilder.create(token, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_EXPRESSIONS)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build()
                .awaitReady();
        registerCommands();
        bot.addEventListener(new RegisterGameCommand(this));
        bot.addEventListener(new LeaderboardCommand(this));

        ObjectMapper objectMapper = new ObjectMapper();
        List<Player> playersToLoad;
        if (playersFile.createNewFile()) { playersToLoad = new ArrayList<>(); objectMapper.writeValue(playersFile, players); }
        else playersToLoad = objectMapper.readValue(playersFile, new TypeReference<List<Player>>() {});
        for (Player player : playersToLoad) {
            players.put(player.getId(), player);
        }
        if (gamesFile.createNewFile()) { games = new ArrayList<>(); objectMapper.writeValue(gamesFile, games); }
        else games = objectMapper.readValue(gamesFile, new TypeReference<List<Game>>() {});
    }

    public void registerCommands() {
        bot.upsertCommand("registergame", "Say who won / lost") // global commands are slow
                .addOption(OptionType.STRING, "good", "Mention good users", true)
                .addOption(OptionType.STRING, "evil", "Mention evil users", true)
                .addOption(OptionType.STRING, "winner", "Who won", true)
                .queue();
        bot.upsertCommand("leaderboard", "The leaderboard for the given alignment") // global commands are slow
                .addOption(OptionType.STRING, "alignment", "The alignment you want to see the leaderboard for", true)
                .queue();
    }

    public Player getPlayer(long id) {
        return players.getOrDefault(id, Player.createNewPlayer(id, averageMuRatingForAlignment(Alignment.GOOD), averageMuRatingForAlignment(Alignment.EVIL)));
    }

    public double averageMuRatingForAlignment(Alignment alignment) {
        return players.values().stream().mapToDouble(player -> player.getSkillRatingForAlignment(alignment).mu()).average().orElse(25);
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players.values());
    }

    public void addAndSaveGame(Game game) {
        games.add(game);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(gamesFile, games);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void savePlayers() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(playersFile, players.values());
        } catch (IOException e) {throw new RuntimeException(e);}
    }

    public GameInfo registerGameAndRecalculateSkillRatings(List<Long> goodIds, List<Long> evilIds, Alignment winningAlignment) {
        if (goodIds.stream().anyMatch(evilIds::contains) || evilIds.stream().anyMatch(goodIds::contains)) {
            throw new IllegalArgumentException("Good and evil users cannot be the same");
        }
        addAndSaveGame(new Game(goodIds, evilIds, winningAlignment));

        RatingModelConfig config = RatingModelConfig.builder()
                // Core Parameters
                .build();

        List<SimplePlayerResult<Long>> goodPlayerSkills = goodIds.stream().map(this::getPlayer).map((player) -> player.getSimplePlayerResultForAlignment(Alignment.GOOD)).toList();
        List<SimplePlayerResult<Long>> evilPlayerSkills = evilIds.stream().map(this::getPlayer).map((player) -> player.getSimplePlayerResultForAlignment(Alignment.EVIL)).toList();

        // Form teams
        SimpleTeamResult<Long> goodTeam = makeTeam(Alignment.GOOD == winningAlignment, goodPlayerSkills);
        SimpleTeamResult<Long> evilTeam = makeTeam(Alignment.EVIL == winningAlignment, evilPlayerSkills);

        // Rate using Bradley-Terry model
        Adjudicator<Long> adjudicator = new Adjudicator<>(
                config,
                new PlackettLuce(config)
        );

        QualityEvaluator evaluator = new QualityEvaluator(config);
        double matchQuality = evaluator.evaluateQuality(goodTeam, evilTeam);
        double goodWinProbability = WinProbabilityCalculator.calculateGoodWinProbability(goodTeam, evilTeam, config);

        List<RatingAdjustment<Long>> adjustments = adjudicator.rate(List.of(goodTeam, evilTeam));
        adjustments.forEach((adjustment) -> {
            Player player = getPlayer(adjustment.playerId());
            player.updateSkill(goodIds.contains(adjustment.playerId()) ? Alignment.GOOD : Alignment.EVIL, new SkillRating(adjustment.mu(), adjustment.sigma()));
            players.put(player.getId(), player);
        });

        savePlayers();
        return new GameInfo(matchQuality, goodWinProbability);
    }

    public static <T> SimpleTeamResult<T> makeTeam(boolean winner, List<SimplePlayerResult<T>> players) {
        double mu = players.stream().mapToDouble(SimplePlayerResult::mu).average().orElse(0.0);
        double sigma = Math.sqrt(players.stream().mapToDouble(p -> Math.pow(p.sigma(), 2)).sum()); // square root of the sum of variances
        return new SimpleTeamResult<>(mu, sigma, winner ? 1 : 2, players);
    }

    public JDA getBot() {
        return bot;
    }
}
