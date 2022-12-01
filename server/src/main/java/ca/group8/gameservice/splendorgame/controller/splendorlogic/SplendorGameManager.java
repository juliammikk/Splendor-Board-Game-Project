package ca.group8.gameservice.splendorgame.controller.splendorlogic;

import ca.group8.gameservice.splendorgame.model.ModelAccessException;
import ca.group8.gameservice.splendorgame.model.splendormodel.GameInfo;
import ca.group8.gameservice.splendorgame.model.splendormodel.PlayerInGame;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Manages instances of Splendor games.
 */
@Component
public class SplendorGameManager {
  private final Map<Long, GameInfo> activeGames;

  public SplendorGameManager() {
    this.activeGames = new HashMap<>();
  }

  /**
   * Constructor.
   */
  public GameInfo getGameById(long gameId) throws ModelAccessException {
    if (!isExistentGameId(gameId)) {
      throw new ModelAccessException("No registered game with that ID");
    }
    return activeGames.get(gameId);
  }


  public boolean isExistentGameId(long gameId) {
    return activeGames.containsKey(gameId);
  }

  public void addGame(long gameId, GameInfo newGameInfo) throws ModelAccessException {
    activeGames.put(gameId, newGameInfo);
  }

  public void removeGame(long gameId) {
    activeGames.remove(gameId);
  }


  public Map<Long, GameInfo> getActiveGames() {
    return activeGames;
  }

  /**
   * Check whether current player is in game or not.
   */
  public boolean isPlayerInGame(long gameId, String playerName) {
    GameInfo curGameState = activeGames.get(gameId);
    return curGameState.getPlayerNames().contains(playerName);
  }

  /**
   * If the current player is not in the game, the returned value will be null.
   */
  public PlayerInGame getPlayerInGame(long gameId, String playerName) {
    GameInfo curGameState = activeGames.get(gameId);
    PlayerInGame resultPlayer = null;
    for (PlayerInGame player : curGameState.getPlayersInGame()) {
      if (player.getName().equals(playerName)) {
        resultPlayer = player;
      }
    }

    return resultPlayer;
  }

}
