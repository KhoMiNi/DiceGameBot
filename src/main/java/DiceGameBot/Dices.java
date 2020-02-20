package DiceGameBot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

class Dices {

    private DiceCombinations diceCombinations;
    String rollText, rollResult;
    ArrayList<DiceCombinations> diceRollCounts;
    boolean zonk;

    Dices() {
        this.diceCombinations = new DiceCombinations();
        rollText = "";
        rollResult = "";
        diceRollCounts = new ArrayList<>();

    }

    private void analyzeDiceRoll(Player player, ArrayList<Integer> diceRoll) {


        HashMap<Integer, ArrayList<Integer>> rollMap = buildRollHashMap(diceRoll);
        ArrayList<DiceCombinations> diceRollMenu = new ArrayList<>();

        // Analyze roll for straight and 3 pairs of 2
        if (this.diceCombinations.straightExists(rollMap)) {
            this.rollResult = player.getName() + "'s roll: " + Arrays.toString(diceRoll.toArray()) + "\n";
            this.rollText = "\n" + player.getName() + " rolled a straight. This is worth 2,000 points.";
            player.setTurnScore(2000);
        } else if (this.diceCombinations.threePairsExists(rollMap)) {
            this.rollResult = player.getName() + "'s roll: " + Arrays.toString(diceRoll.toArray()) + "\n";
            this.rollText = "\n" + player.getName() + " rolled three pair. This is worth 1,500 points.";
            player.setTurnScore(1500);
        } else { // Check for >= 3 of a Kind, Ones, and Fives
            if (this.diceCombinations.threeExists(rollMap)) {
                this.diceCombinations.calculateThree(rollMap, diceRollMenu);
            }
            if (this.diceCombinations.singlesExists(rollMap)) {
                this.diceCombinations.calculateSingle(rollMap, diceRollMenu);
            }
            if (player.getNumDiceInUse() == 6) {
                System.out.println(player.getName() + " has all 6 dice in use and must reroll.");
            }
            // If players roll contains any scoring combinations, continue turn.
            if (diceRollMenu.size() > 0) {
                this.rollResult = player.getName() + "'s roll: " + Arrays.toString(diceRoll.toArray()) + "\n";
                this.diceRollCounts.addAll(diceRollMenu);
            } else { // Else if player roll zonk, reset all turn variables end turn.
                this.zonk = true;
                this.rollResult = player.getName() + "'s roll: " + Arrays.toString(diceRoll.toArray()) + "\n";
                player.resetTurnScore();
                this.rollText = "\n" + player.getName() + " rolled Zonk. 0 points for this turn.";

            }
        }
    }

    private HashMap<Integer, ArrayList<Integer>> buildRollHashMap(ArrayList<Integer> roll) {

        HashMap<Integer, ArrayList<Integer>> rollHashMap = new HashMap<>();

        Integer rollIndex = 0;
        for (Integer rollNum : roll) {
            if (rollHashMap.containsKey(rollNum)) {
                rollHashMap.get(rollNum).add(rollIndex);
            } else {
                ArrayList<Integer> rollIndexList = new ArrayList<>();
                rollIndexList.add(rollIndex);
                rollHashMap.put(rollNum, rollIndexList);
            }
            rollIndex++;
        }

        return rollHashMap;
    }

    void rollAndAnalyzeDice(Player player, int numDice) {
        ArrayList<Integer> diceRoll = rollDice(numDice);
        this.diceRollCounts.clear();
        this.rollResult = "";
        this.rollText = "";
        this.zonk = false;
        analyzeDiceRoll(player, diceRoll);
    }

    private ArrayList<Integer> rollDice(int numDice) {
        ArrayList<Integer> diceRoll = new ArrayList<>();
        for (int i = 0; i < numDice; i++) {
            diceRoll.add((int) (Math.random() * 6 + 1));
        }

        return diceRoll;
    }
}