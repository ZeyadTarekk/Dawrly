package SearchPackage;

public class Pair<type1, type2, type3> {
    public type1 TF;
    public type2 size;
    public type3 score;

    public Pair() {
    }

    @Override
    public String toString() {
        return "Pair{" +
                "TF=" + TF +
                ", size=" + size +
                ", score=" + score +
                '}';
    }

    public Pair(type1 TF, type2 size, type3 score) {
        this.TF = TF;
        this.size = size;
        this.score = score;
    }
}
