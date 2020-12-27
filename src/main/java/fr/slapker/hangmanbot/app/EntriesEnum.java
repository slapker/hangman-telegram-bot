package fr.slapker.hangmanbot.app;

import lombok.Getter;

import java.util.jar.JarFile;

@Getter
public enum EntriesEnum {
    QUOTE("/quote", "Start hangman game with random famous quote !", false),
    STOP("/stop", "Stop hangman game", false),
    SCORE("/score","Get your score",false),
    RESET("/reset","Reset score",false),
    START("/start","Get info about this hangman game Bot",false),
    HELP("/help","Get info about this hangman game Bot",false),
    ADMIN("/admin","Admin infos",false);

    private String entry;
    private String desc;
    private boolean isForAdmin;

    EntriesEnum(String entry, String rule, boolean isForAdmin) {
        this.entry = entry;
        this.desc = rule;
        this.isForAdmin = isForAdmin;
    }

}
