package SearchPackage;

public class Pair<type1,type2> {
    public type1 TF;
    public type2 size;
    public Pair() {
    }

    @Override
    public String toString() {
        return "Pair{" +
                "TF=" + TF +
                ", size=" + size +
                '}';
    }

    public Pair(type1 first, type2 second) {
        this.TF = first;
        this.size = second;
    }
}
