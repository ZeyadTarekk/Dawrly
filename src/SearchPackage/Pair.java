package SearchPackage;

import java.util.List;

public class Pair<type1, type2, type3, type4,type5> {
    public type1 TF;
    public type2 size;
    public type3 score;

    public List<type4> index;

    public  type5 actualIndex;
    public Pair() {
    }

    @Override
    public String toString() {
        return "Pair{" +
                "TF=" + TF +
                ", size=" + size +
                ", score=" + score +
                ", index=" + index +
                ", actualIndex=" + actualIndex +
                '}';
    }

    public Pair(type1 TF, type2 size, type3 score) {
        this.TF = TF;
        this.size = size;
        this.score = score;
    }
}
