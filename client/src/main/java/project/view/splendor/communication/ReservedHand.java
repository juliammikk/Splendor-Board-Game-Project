package project.view.splendor.communication;

import java.util.ArrayList;
import java.util.List;

public class ReservedHand {
    private List<DevelopmentCard> developmentCards;
    private List<NobleCard> nobleCards;

    public ReservedHand() {
        this.developmentCards = new ArrayList<>();
        this.nobleCards = new ArrayList<>();
    }


    public void addDevelopmentCard(DevelopmentCard card){
            developmentCards.add(card);
    }

    public void addNobleCard(NobleCard card){
        nobleCards.add(card);
    }

    public void removeDevelopmentCard(DevelopmentCard card){
        developmentCards.remove(card);
    }

    public void removeNobleCard(NobleCard card){
        nobleCards.remove(card);
    }

    public boolean isFull(){
        return developmentCards.size() == 3;
    }

    public int getSize(){
        return developmentCards.size();
    }

    public List<DevelopmentCard> getDevelopmentCards() {
        return developmentCards;
    }

    public List<NobleCard> getNobleCards() {
        return nobleCards;
    }
}
