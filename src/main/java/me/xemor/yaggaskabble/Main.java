package me.xemor.yaggaskabble;

import net.dv8tion.jda.api.JDABuilder;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello, World!");
        Yaggaskabble yaggaskabble = new Yaggaskabble(args[0]);
    }
}