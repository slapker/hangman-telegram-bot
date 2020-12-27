package fr.slapker.hangmanbot.game;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayResult {
    private String textToSend;
    private boolean chainNextStep;
    private int delayBeforeNextStep;
}
