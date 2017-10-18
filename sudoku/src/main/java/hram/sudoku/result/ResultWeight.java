package hram.sudoku.result;

public class ResultWeight extends ResultValue {

    private int weight;

    public ResultWeight(int value) {
        this.weight = value;
    }

    public int GetWeight() {
        return weight;
    }

    @Override
    public ResulType GetType() {
        return ResulType.WEIGHT;
    }

}
