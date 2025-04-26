package me.xemor.yaggaskabble;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        String token = args[0];
        Yaggaskabble yaggaskabble = new Yaggaskabble(token);
    }


}