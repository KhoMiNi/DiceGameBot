package DiceGameBot;


import java.util.ArrayList;
import java.util.Map;

class DiceCombinations {
    String type;
    int points;
    ArrayList<Integer> indices;

    DiceCombinations() {

    }

    private DiceCombinations(String type, Integer amount, ArrayList<Integer> indices) {
        this.type = type;
        this.points = amount;
        this.indices = indices;
    }

    boolean singlesExists(Map<Integer, ArrayList<Integer>> rollMap) {
        for (int key : rollMap.keySet()) {
            if (key == 1 || key == 5) return true;
        }
        return false;
    }

    boolean straightExists(Map<Integer, ArrayList<Integer>> rollMap) {
        return rollMap.size() == 6;
    }

    boolean threePairsExists(Map<Integer, ArrayList<Integer>> rollMap) {

        int rollIndexListSizeOfTwo = 0;
        for (ArrayList<Integer> rollIndexList : rollMap.values()) {
            if (rollIndexList.size() == 2) rollIndexListSizeOfTwo++;
        }
        return rollIndexListSizeOfTwo == 3;
    }

    boolean threeExists(Map<Integer, ArrayList<Integer>> rollMap) {
        for (ArrayList<Integer> rollIndexList : rollMap.values()) {
            if (rollIndexList.size() >= 3) return true;
        }

        return false;
    }

    void calculateSingle(Map<Integer, ArrayList<Integer>> rollMap, ArrayList<DiceCombinations> diceRollMenu) {

        rollMap.forEach((rollNum, rollIndexList) -> {
            if (rollNum == 1 || rollNum == 5) {
                int rollIndexListSize = rollIndexList.size();
                if (rollIndexListSize < 3) {
                    String pointType = (rollNum == 1) ? "One" : "Five";
                    int pointAmount = (rollNum == 1) ? 100 : 50;
                    for (Integer aRollIndexList : rollIndexList) {
                        ArrayList<Integer> diceCombinationsRollIndex = new ArrayList<>();
                        diceCombinationsRollIndex.add(aRollIndexList);
                        DiceCombinations diceCombinations = new DiceCombinations(pointType, pointAmount, diceCombinationsRollIndex);
                        diceRollMenu.add(diceCombinations);
                    }
                }
            }
        });
    }

    void calculateThree(Map<Integer, ArrayList<Integer>> rollMap, ArrayList<DiceCombinations> diceRollMenu) {

        rollMap.forEach((rollNum, rollIndexList) -> {
            int rollIndexListSize = rollIndexList.size();
            String pointType = rollIndexListSize + " Of A Kind: " + rollNum;
            int pointsAmount = (rollNum == 1) ? 1000 : rollNum * 100;

            if (rollIndexListSize >= 3) {
                for (int i = 0; i < rollIndexListSize - 3; i++) {
                    pointsAmount = pointsAmount * 2;
                }

                DiceCombinations diceCombinations = new DiceCombinations(pointType, pointsAmount, rollIndexList);
                diceRollMenu.add(diceCombinations);
            }
        });
    }
}