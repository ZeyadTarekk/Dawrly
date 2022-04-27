package SearchPackage;

public class Pair2<type1, type2> {
    public type1 TF;
    public type2 score;

    public Pair2() {
    }

    @Override
    public String toString() {
        return "Pair{" +
                "TF=" + TF +
                ", score=" + score +
                '}';
    }

    public Pair2(type1 TF, type2 score) {
        this.TF = TF;
        this.score = score;
    }

    public type1 first() {
        return this.TF;
    }

    public type2 second() {
        return this.score;
    }
}
