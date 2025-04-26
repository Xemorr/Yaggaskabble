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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuildPlayers {

    private final JDA bot;
    private final Map<Long, GuildPlayer> players = new HashMap<>();
    private List<Game> games = new ArrayList<>();
    private final File playersFile;
    private final File gamesFile;

    public GuildPlayers(JDA bot, long guildId) throws IOException {
        this.bot = bot;
        playersFile = new File(guildId + "-players.json");
        gamesFile = new File(guildId + "-games.json");

        ObjectMapper objectMapper = new ObjectMapper();
        List<GuildPlayer> playersToLoad;
        if (playersFile.createNewFile()) { playersToLoad = new ArrayList<>(); objectMapper.writeValue(playersFile, players); }
        else playersToLoad = objectMapper.readValue(playersFile, new TypeReference<List<GuildPlayer>>() {});
        for (GuildPlayer guildPlayer : playersToLoad) {
            players.put(guildPlayer.getId(), guildPlayer);
        }
        if (gamesFile.createNewFile()) { games = new ArrayList<>(); objectMapper.writeValue(gamesFile, games); }
        else games = objectMapper.readValue(gamesFile, new TypeReference<List<Game>>() {});
    }

    public GuildPlayer getPlayer(long id) {
        return players.getOrDefault(id, GuildPlayer.createNewPlayer(id, averageMuRatingForAlignment(Alignment.GOOD), averageMuRatingForAlignment(Alignment.EVIL)));
    }

    public double averageMuRatingForAlignment(Alignment alignment) {
        return players.values().stream().mapToDouble(guildPlayer -> guildPlayer.getSkillRatingForAlignment(alignment).mu()).average().orElse(25);
    }

    public List<GuildPlayer> getPlayers() {
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

        List<SimplePlayerResult<Long>> goodPlayerSkills = goodIds.stream().map(this::getPlayer).map((guildPlayer) -> guildPlayer.getSimplePlayerResultForAlignment(Alignment.GOOD)).toList();
        List<SimplePlayerResult<Long>> evilPlayerSkills = evilIds.stream().map(this::getPlayer).map((guildPlayer) -> guildPlayer.getSimplePlayerResultForAlignment(Alignment.EVIL)).toList();

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
            GuildPlayer guildPlayer = getPlayer(adjustment.playerId());
            guildPlayer.updateSkill(goodIds.contains(adjustment.playerId()) ? Alignment.GOOD : Alignment.EVIL, new SkillRating(adjustment.mu(), adjustment.sigma()));
            players.put(guildPlayer.getId(), guildPlayer);
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
