package ca.group8.gameservice.splendorgame.model.splendormodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class stores the cards/decks with level 1->3 on base board and the nobles
 */
public class BaseBoard extends Board {

  private final Map<Integer, List<DevelopmentCard>> decks = new HashMap<>();
  private final Map<Integer, DevelopmentCard[]> cardsOnBoard = new HashMap<>();
  private List<NobleCard> nobles;


  public BaseBoard(List<String> playerNames) {
    // set up decks and cards on board
    setup(playerNames);
  }

  /**
   * Used for reserve from the Deck
   * @param cardLevel level of the card
   * @return a card taken from cardLevel deck
   */
  public DevelopmentCard popLevelCardFromDeck(int cardLevel) {
    return decks.get(cardLevel).remove(0);
  }

  /**
   * Used to get the DevCard[] for a certain level
   * @param cardLevel level of the card
   * @return an array of dev cards on board
   */
  public DevelopmentCard[] getLevelCardsOnBoard(int cardLevel) {
    return cardsOnBoard.get(cardLevel);
  }

  /**
   * getter for nobles field
   * @return the list of noble cards
   */
  public List<NobleCard> getNobles() {
    return nobles;
  }

  /**
   * Find the noble in the list and remove it
   * @param noble the noble that we want to remove (has been unlocked or reserved)
   */
  public void removeNoble(NobleCard noble){
    nobles.remove(noble);
  }

  /**
   * Return the noble when player does not meet the requirement to unlock one
   * @param noble the noble to be added back
   */
  public void addNoble(NobleCard noble) {
    nobles.add(noble);
  }

  /**
   * Quickly set the card at that position to null, as a representation of removing it
   * @param cardPosition x and y: x stands for the level and y stands for the index
   */
  public DevelopmentCard removeCard(Position cardPosition) {
    int level = cardPosition.getX();
    int cardIndex = cardPosition.getY();
    DevelopmentCard resultCard = cardsOnBoard.get(level)[cardIndex];
    cardsOnBoard.get(level)[cardIndex] = null;
    return resultCard;
  }

  public Map<Integer, List<DevelopmentCard>> getBaseDecks() {
    return decks;
  }

  public Map<Integer, DevelopmentCard[]> getBaseCardsOnBoard() {
    return cardsOnBoard;
  }


  /**
   * Generate decks for each level of base dev card
   * @param allCards all dev cards (with no CardEffect) parsed from json
   */
  private void generateDeckPerLevel(List<DevelopmentCard> allCards) {
    List<DevelopmentCard> levelOneDeck = new ArrayList<>();
    List<DevelopmentCard> levelTwoDeck = new ArrayList<>();
    List<DevelopmentCard> levelThreeDeck = new ArrayList<>();
    for (DevelopmentCard card : allCards) {
      if (card.getLevel() == 1) {
        levelOneDeck.add(card);
      } else if (card.getLevel() == 2) {
        levelTwoDeck.add(card);
      } else {
        levelThreeDeck.add(card);
      }
    }
    Collections.shuffle(levelOneDeck);
    Collections.shuffle(levelTwoDeck);
    Collections.shuffle(levelThreeDeck);
    decks.put(1, levelOneDeck);
    decks.put(2, levelTwoDeck);
    decks.put(3, levelThreeDeck);
  }

  /**
   * Refill any null position on the board (3 of len = 4 DevelopmentCard array)
   * must make sure once the card is purchased, make the corresponding slot (index) -> null
   */
  private void refillCardBoard() {
    for (int i = 1; i <= 3; i++) {
      DevelopmentCard[] curLevelCardsOnBoard = getLevelCardsOnBoard(i);
      for (int j = 0; j < 4; j++) {
        if (curLevelCardsOnBoard[j] == null) {
          curLevelCardsOnBoard[j] = popLevelCardFromDeck(i);
        }
      }
    }
  }

  @Override
  public void update(Card aCard, int index) {
    refillCardBoard();
  }

  @Override
  public void setup(List<String> playerNames) {
    int playerCount = playerNames.size();

    // set up nobles on board
    List<NobleCard> allNobles = super.generateNobleCards();
    Collections.shuffle(allNobles);
    nobles = allNobles.subList(0,playerCount+1); // noble count = player count + 1

    // get all cards info from json file
    List<DevelopmentCard> baseDevCards
        = super.generateDevelopmentCards("cardinfo_basecard");

    // initialize decks and slots to put card on board
    generateDeckPerLevel(baseDevCards);
    for (int i = 1; i <= 3; i++) {
      cardsOnBoard.put(i, new DevelopmentCard[4]);
    }

    // fill the board
    refillCardBoard();
  }

}
