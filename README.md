# Yaggaskabble
An OpenSkill based discord bot for rating Blood on the Clocktower games.

It uses two completely independent ratings for a BotC guildPlayer, depending on whether they finished the game on the good or evil team.
- A guildPlayer can be thought of as a tuple that looks like (discordId, (μ, σ), (μ, σ))
- Players are initially initialized as (discordId, (25, 8.333), (25, 8.333)) but all ratings are multiplied by 60 before being shown in discord, so the starting rating appears as 1500+-500.
- Subsequent guildPlayers are initialised as (discordId, (mean good μ, 8.333), (mean evil μ, 8.333))

There are two commands:
- /registergame <good> <evil> <alignment> for writing down which alignment won and which guildPlayers were on each side (by mentioning their discord account).
- /leaderboard <alignment> for seeing how skilled guildPlayers are at playing as each alignment

