package me.xemor.yaggaskabble;

import java.util.List;

public record Game(List<Long> goodPlayersIds, List<Long> evilPlayersIds, Alignment winningAlignment) {}
