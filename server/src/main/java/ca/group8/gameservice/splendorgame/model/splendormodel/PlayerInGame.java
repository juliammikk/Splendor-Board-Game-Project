package ca.group8.gameservice.splendorgame.model.splendormodel;

import ca.group8.gameservice.splendorgame.controller.SplendorDevHelper;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Players.
 */
public class PlayerInGame {


  private final EnumMap<Colour, Integer> wealth;
  private final TokenHand tokenHand;
  private final ReservedHand reservedHand;
  private final PurchasedHand purchasedHand;
  private String name;
  private int prestigePoints;


  /**
   * Constructor.
   *
   * @param name player's name
   */
  public PlayerInGame(String name) {
    this.name = name;
    this.tokenHand = new TokenHand(0); // Initialize to 0 (empty)
    this.purchasedHand = new PurchasedHand();
    this.reservedHand = new ReservedHand();
    this.wealth = SplendorDevHelper.getInstance().getRawTokenColoursMap(); // wealth including gold
    this.prestigePoints = 0;
  }

  /**
   * This method calculates what tokens the player must use to buy the card. It then removes the
   * required tokens from the player's tokenHand.
   *
   * @param goldCardsRequired The number of gold tokens required to complete this purchase
   * @param paidTokens        tokens paid to buy the card
   */
  public void payTokensToBuy(int goldCardsRequired, EnumMap<Colour, Integer> paidTokens) {

    // remove tokens (after discount_ for this payment from playerHand
    tokenHand.removeToken(paidTokens);

    List<DevelopmentCard> allDevCards = new ArrayList<>(purchasedHand.getDevelopmentCards());
    while (goldCardsRequired > 0) {
      for (DevelopmentCard card : allDevCards) {
        if (card.getGemColour().equals(Colour.GOLD)) {
          goldCardsRequired -= 1;
          // remove the gold card that's used to pay
          purchasedHand.removeDevelopmentCard(card);
        }
      }
    }
  }

  /**
   * Gets player's token hand.
   *
   * @return player's TokenHand
   */
  public TokenHand getTokenHand() {
    return tokenHand;
  }

  /**
   * Gets PurchasedHand.
   *
   * @return player's PurchasedHand
   */
  public PurchasedHand getPurchasedHand() {
    return purchasedHand;
  }

  /**
   * Gets player's reserved hand.
   *
   * @return player's ReservedHand
   */
  public ReservedHand getReservedHand() {
    return reservedHand;
  }

  /**
   * Gets player's name.
   *
   * @return String name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets player's name.
   *
   * @param name of player
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets player's prestige points.
   *
   * @return int of prestige points.
   */
  public int getPrestigePoints() {
    return prestigePoints;
  }

  /**
   * Amount can be negative, as to minus.
   *
   * @param amount amountNotSetter
   */
  public void changePrestigePoints(int amount) {
    prestigePoints += amount;
  }

  /**
   * Removed given number from prestige points.
   *
   * @param amount to take away
   */
  public void removePrestigePoints(int amount) {
    prestigePoints -= amount;
  }

  /**
   * Returns map of total gems (out of all dev cards) a player has.
   * Guarantee to return a map with only RED, BLUE, WHITE, BLACK AND GREEN and gold
   * for the purpose of double_gold card in orient
   *
   * @return Map of total gems
   */
  public EnumMap<Colour, Integer> getTotalGems() {
    EnumMap<Colour, Integer> totalGems = SplendorDevHelper.getInstance().getRawTokenColoursMap();
    if (purchasedHand.getDevelopmentCards().size() > 0) {
      for (DevelopmentCard card : purchasedHand.getDevelopmentCards()) {
        // Only count the card with regular gem colours
        //if (card.hasRegularGemColour()) {
        //}
        Colour colour = card.getGemColour();
        if (!colour.equals(Colour.ORIENT)) {
          Logger logger  = LoggerFactory.getLogger(PlayerInGame.class);
          int oldValue = totalGems.get(colour);
          int count;
          if (colour == Colour.GOLD) {
            count = card.getGemNumber() / 2;
          } else {
            count = card.getGemNumber();
          }

          totalGems.put(colour, oldValue + count);
          //logger.warn("Player name:" + name);
          //logger.warn("Colour " + colour);
          //logger.warn("TotalGems: " + totalGems);
        }
      }
    }
    return totalGems;
  }

  /**
   * Gets player's tokens plus gems.
   *
   * @return Map of wealth
   */
  public EnumMap<Colour, Integer> getWealth() {
    Logger logger = LoggerFactory.getLogger(PlayerInGame.class);
    EnumMap<Colour, Integer> gems = getTotalGems();
    //logger.info("All tokens in token hand: " + tokenHand.getAllTokens());
    //logger.info("All gems as a enum map: " + gems);

    for (Colour colour : SplendorDevHelper.getInstance().getRawTokenColoursMap().keySet()) {
      wealth.put(colour, tokenHand.getAllTokens().get(colour) + gems.get(colour));
    }
    return wealth;
  }


}
