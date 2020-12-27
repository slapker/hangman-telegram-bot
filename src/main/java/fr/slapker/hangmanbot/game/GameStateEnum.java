package fr.slapker.hangmanbot.game;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum GameStateEnum {
    CHOOSE_ROUND(0,"Choose round number"),
    START(1, "Start game"),
    IN_GAME(2, "In game"),
    FINISHED(3, "End of game"),
    GET_ROUND(4,"Get round number"),
    FINSHED_ROUND(5,"Round finished"),
    BE_READY_NEXT_ROUND(6,"Be ready for next round"),
    SHOW_SCORE(7,"Show score"),
    WAITING_MODE(8,"waiting mode");

    private int code;
    private String libelle;

    GameStateEnum(int pCode, String pLibelle) {
        this.code = pCode;
        this.libelle = pLibelle;
    }

}
