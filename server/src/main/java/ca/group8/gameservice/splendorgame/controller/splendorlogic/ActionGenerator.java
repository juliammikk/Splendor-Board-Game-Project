package ca.group8.gameservice.splendorgame.controller.splendorlogic;

import ca.group8.gameservice.splendorgame.controller.SplendorJsonHelper;
import ca.group8.gameservice.splendorgame.model.splendormodel.BaseBoard;
import ca.group8.gameservice.splendorgame.model.splendormodel.CardEffect;
import ca.group8.gameservice.splendorgame.model.splendormodel.Colour;
import ca.group8.gameservice.splendorgame.model.splendormodel.DevelopmentCard;
import ca.group8.gameservice.splendorgame.model.splendormodel.Extension;
import ca.group8.gameservice.splendorgame.model.splendormodel.OrientBoard;
import ca.group8.gameservice.splendorgame.model.splendormodel.PlayerInGame;
import ca.group8.gameservice.splendorgame.model.splendormodel.Position;
import ca.group8.gameservice.splendorgame.model.splendormodel.Power;
import ca.group8.gameservice.splendorgame.model.splendormodel.PowerEffect;
import ca.group8.gameservice.splendorgame.model.splendormodel.TableTop;
import ca.group8.gameservice.splendorgame.model.splendormodel.TraderBoard;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Makes list of all possible actions for current player.
 */
public class ActionGenerator {

  // This is a Singleton Component Class that's used to control all action map for all players
  // for all games. Once provided a gameId and a playerName, we can identify a unique
  // Map<String (MD5 hashed Action objs), Action> to such player in that specific gam


  // 1. Long: gameId
  // 2. String: playerName
  // 3. String: MD5 hashed Action object

  // In GET request, 1 and 2 will be provided from the request path variable
  // but 3 will be generated by interpreting the access_token sent from client
  // so that we know what's the KEY to put in the Map<playerName, Map<String,Action>>


  // In any POST request, 1,2 and 3 will be provided, we can just use this
  // nested 3 level map to find the corresponding ONE specific Action and call execute() on it.

  //previous name of this field/the field this is replacing: actionLookUpMap
  private final Map<String, Map<String, Action>> playerActionMaps;
  private final TableTop tableTop;

  public ActionGenerator(Map<String, Map<String, Action>> playerActionMaps, TableTop tableTop) {
    this.playerActionMaps = playerActionMaps;
    this.tableTop = tableTop;
  }


  // Translate all dev cards on base/orient board to reserve actions
  private List<Action> cardsToReserveAction(BaseBoard baseBoard,
                                            OrientBoard orientBoard,
                                            PlayerInGame curPlayerInfo) {
    List<Action> result = new ArrayList<>();
    boolean canReserve = !curPlayerInfo.getReservedHand().isFull();
    if (canReserve) {
      for (int level = 1; level <= 3; level++) {
        DevelopmentCard[] baseLevelCards = baseBoard.getLevelCardsOnBoard(level);
        DevelopmentCard[] orientLevelCards = orientBoard.getLevelCardsOnBoard(level);
        for (int cardIndex = 0; cardIndex < baseLevelCards.length; cardIndex++) {
          Position cardPosition = new Position(level, cardIndex);
          DevelopmentCard card = baseLevelCards[cardIndex];
          // always generate reserve actions for base cards for index 0,1,2,3
          result.add(new ReserveAction(cardPosition, card));
          if (cardIndex < 2) {
            // if index = 0 or 1, generate reserve action for orient cards
            DevelopmentCard orientCard = orientLevelCards[cardIndex];
            result.add(new ReserveAction(cardPosition, orientCard));
          }
        }
      }
    }
    return result;
  }


  private boolean hasDoubleGoldPowerUnlocked(PlayerInGame curPlayerInfo) {
    String playerName = curPlayerInfo.getName();
    boolean hasTraderExtension = tableTop.getGameBoards().containsKey(Extension.TRADING_POST);
    boolean hasDoubleGoldPower = false;
    if (hasTraderExtension) {
      TraderBoard traderBoard = (TraderBoard) tableTop.getBoard(Extension.TRADING_POST);
      Map<PowerEffect,Power> powerMap = traderBoard.getAllPlayerPowers().get(playerName);
      // this boolean becomes true if the player does have the DOUBLE_GOLD power on
      hasDoubleGoldPower = powerMap.entrySet().stream().anyMatch(entry ->
              entry.getKey().equals(PowerEffect.DOUBLE_GOLD) && entry.getValue().isUnlocked());
    }
    return hasDoubleGoldPower;
  }

  // Translate all dev cards on base/orient board to purchase actions
  private List<Action> cardsToPurchaseAction(BaseBoard baseBoard,
                                             OrientBoard orientBoard,
                                             PlayerInGame curPlayerInfo) {

    EnumMap<Colour, Integer> wealth = curPlayerInfo.getWealth();
    boolean hasDoubleGoldPower = hasDoubleGoldPowerUnlocked(curPlayerInfo);
    // now when we decide whether a card is affordable or not, we need to consider the effect of
    // the double gold power is on
    List<Action> result = new ArrayList<>();
    int goldTokenCount;
    for (int level = 1; level <= 3; level++) {
      DevelopmentCard[] baseLevelCards = baseBoard.getLevelCardsOnBoard(level);
      DevelopmentCard[] orientLevelCards = orientBoard.getLevelCardsOnBoard(level);
      for (int cardIndex = 0; cardIndex < baseLevelCards.length; cardIndex++) {
        Position cardPosition = new Position(level, cardIndex);
        DevelopmentCard card = baseLevelCards[cardIndex];
        // always generate reserve actions for base cards for index 0,1,2,3
        goldTokenCount = card.canBeBought(hasDoubleGoldPower, wealth);
        if (goldTokenCount > 0) {
          result.add(new PurchaseAction(cardPosition,card,goldTokenCount));
        }
        if (cardIndex < 2) {
          // if index = 0 or 1, generate reserve action for orient cards
          DevelopmentCard orientCard = orientLevelCards[cardIndex];
          goldTokenCount = orientCard.canBeBought(hasDoubleGoldPower, wealth);
          if(goldTokenCount > 0) {
            result.add(new PurchaseAction(cardPosition, orientCard, goldTokenCount));
          }
        }
      }
    }

    return result;
  }

  /**
   * Set up the initial actions for curPlayerInfo.
   * PurchaseActions, ReserveAction and TakeTokenAction
   *
   * @param curPlayerInfo current player's associated player info
   */
  public void setInitialActions(PlayerInGame curPlayerInfo) {

    // we know by default, orient and base are always on the table
    BaseBoard baseBoard = (BaseBoard) tableTop.getBoard(Extension.BASE);
    OrientBoard orientBoard = (OrientBoard) tableTop.getBoard(Extension.ORIENT);
    List<Action> allActions =
        new ArrayList<>(cardsToReserveAction(baseBoard, orientBoard, curPlayerInfo));
    allActions.addAll(cardsToPurchaseAction(baseBoard, orientBoard, curPlayerInfo));

    Gson gsonParser = SplendorJsonHelper.getInstance().getGson();
    Map<String, Action> curActionMap = new HashMap<>();
    for (Action action : allActions) {
      String actionJson = gsonParser.toJson(action).toUpperCase();
      String actionId = DigestUtils.md5Hex(actionJson);
      curActionMap.put(actionId, action);
    }
    String playerName = curPlayerInfo.getName();
    playerActionMaps.put(playerName, curActionMap);

  }

  //TODO
  public void updateCascadeActions(String playerName,
                                   DevelopmentCard playerCard,
                                   CardEffect cardEffect) {

  }

  //TODO
  public void updatePowerActions(String playerName, PowerEffect powerEffect) {

  }

  //TODO
  public void updateReturnTokenActions(int extraTokenCount,
                                       EnumMap<Colour, Integer> curPlayerTokens) {

  }

  public Map<String, Map<String, Action>> getPlayerActionMaps() {
    return playerActionMaps;
  }

  public TableTop getTableTop() {
    return tableTop;
  }

  //EVERYTHING AFTER THIS IS OLD CODE (not up to date based on M6 model)
  /*
   * 1. create a new map -- DONE
   * - get player wealth
   * - get flag
   * 2. Get list of cards from Base Board (baseBoardCards)
   * 3. Iterate through baseBoardCards -->
   * 3a. call getPosition() on base board object, and pass in the card
   * 3b. verify if you can purchase card --> If yes, create purchase card action [PARAM: WEALTH]
   * 3c. verify if you can reserve card
   * 4. return the map
   */
  /*
  //TODO
  private static List<Action> cardsToActions(GameInfo gameInfo, PlayerInGame player) {
    List<Action> actionOptions = new ArrayList<>();
    EnumMap<Colour, Integer> wealth = player.getWealth();
    boolean canReserve = !player.getReservedHand().isFull();
   /*
    Map<Integer, List<BaseCard>> baseBoardCards =
        gameInfo.getTableTop().getBaseBoard().getBaseCardsOnBoard();

    for (int level : baseBoardCards.keySet()) {
      List<BaseCard> cardsPerLevel = baseBoardCards.get(level);
      for (int cardIndex = 0; cardIndex < cardsPerLevel.size(); cardIndex++) {
        Position cardPosition = new Position(level, cardIndex);
        BaseCard curBaseCard = cardsPerLevel.get(cardIndex);
        //start of purchase card verification
        //this creates a goldCounter, to see if gold tokens are needed
        int goldCounter = 0;
        EnumMap<Colour, Integer> cardPrice = curBaseCard.getPrice();
        for (Colour col : Colour.values()) {
          if (col.equals(Colour.GOLD)) {
            continue;
          }
          if (cardPrice.get(col) != 0) {
            if (cardPrice.get(col) > wealth.get(col)) {
              goldCounter += cardPrice.get(col) - wealth.get(col);
            }
          }
        }

        //checks if you can purchase (with or without gold tokens)
        if (goldCounter <= player.getTokenHand().getGoldTokenNumber()) {
          //create new purchase action option & add it to the actionList.
          actionOptions.add(new PurchaseAction(cardPosition, curBaseCard, goldCounter));

        }
        //verify if player can reserve card
        if (canReserve) {
          actionOptions.add(new ReserveAction(cardPosition, curBaseCard));
        }
      }
    }



    return actionOptions;
  }
   */

  /*
   * Generate the hash -> Actions map provided: gameId, playerName (implicitly in PlayerInGame).
   * will be called everytime GET games/{gameId}/players/{playerName}/actions
   * will replace the previous Action Map every time
   */
  /*
  public void generateActions(long gameId, GameInfo gameInfo, PlayerInGame player) {

    // TODO: Player Identity will be verified before calling generateActions with access_token
    //  no need to check it here (we can safely assume player is valid before calling this)
    String curPlayerName = gameInfo.getCurrentPlayer();
    String askedActionsPlayerName = player.getName();
    Map<String, Action> hashActionMap = new HashMap<>();
    if ((!gameInfo.isFinished()) && (curPlayerName.equals(askedActionsPlayerName))) {
      // only generate the actions if the game is NOT finished and
      // the player asked for actions IS the current turn player

      // adding cardActions
      for (Action action : cardsToActions(gameInfo, player)) {
        String actionMd5 = DigestUtils.md5Hex(new Gson().toJson(action)).toUpperCase();
        hashActionMap.put(actionMd5, action);
      }
      EnumMap<Colour, Integer> playerTokens = player.getTokenHand().getAllTokens();
      TakeTokenAction takeTokenAction = new TakeTokenAction(playerTokens);
      String takeTokenActionMd5 =
          DigestUtils.md5Hex(new Gson().toJson(takeTokenAction)).toUpperCase();

      // add the take token actions (card unrelated actions)
      hashActionMap.put(takeTokenActionMd5, takeTokenAction);
    }

    /*
    // once the hash -> Action map is ready, we add it for this specific player
    if (!actionLookUpMap.containsKey(gameId)) {
      // if the gameId is not recorded, means we have no players' actions, thus we are adding
      // the first player
      Map<String, Map<String, Action>> playerSpecificActionsMap = new HashMap<>();
      playerSpecificActionsMap.put(askedActionsPlayerName, hashActionMap);
      actionLookUpMap.put(gameId, playerSpecificActionsMap);
    } else {
      // otherwise, we must have at least one player name stored in the map, therefore we
      // can for sure get the Map<String, Map<String, Action>>
      // then either overwrites or adding new action map
      Map<String, Map<String, Action>> playerSpecificActionsMap = actionLookUpMap.get(gameId);
      playerSpecificActionsMap.put(askedActionsPlayerName, hashActionMap);
    }

  }

 */


  /*
   * Find the (potentially, might be empty map) previously generated hash -> Action map
   * when receive POST request on games/{gameId}/players/{playerName}/actions/{actionId}
   * first call this method to find the map, then with {actionId} provided, we can find the
   * right Action to execute.
   */
  /*
  public Map<String, Action> lookUpActions(long gameId, String playerName) {
    // whether player is in the game or not will be checked in RestController class
    // if this is an empty map, then there is no need to look up actionMd5, just reply
    // with a bad_request
    return this.actionLookUpMap.get(gameId).get(playerName);
  }

   */


}