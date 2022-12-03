package project.view.splendor.communication;

import java.util.EnumMap;
import project.view.splendor.Colour;


public class TakeTokenAction implements Action {


  private EnumMap<Colour, Integer> tokens;

  public TakeTokenAction(EnumMap<Colour, Integer> tokens) {
    this.tokens = tokens;
  }

  public EnumMap<Colour, Integer> getTokens() {
    return tokens;
  }

  public void setTokens(
      EnumMap<Colour, Integer> tokens) {
    this.tokens = tokens;
  }

  @Override
  public void execute(GameInfo currentGameState, PlayerInGame playerState) {
  }

  @Override
  public boolean checkIsCardAction() {
    return false;
  }

  @Override
  public Card getCard() {
    return null;
  }

  @Override
  public Position getPosition() {
    return null;
  }
}

