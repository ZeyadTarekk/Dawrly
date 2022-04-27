package SearchPackage;

public class Pair3<type1, type2, type3> {
    public type1 score;
    public type2 paragraph;
    public type3 word;

    public Pair3() {
    }

    @Override
    public String toString() {
        return "Pair{" +
                "Score =" + score +
                ", paragraph=" + paragraph +
                ", word=" + word +
                '}';
    }

    public Pair3(type1 score, type2 paragraph, type3 word) {
        this.score = score;
        this.paragraph = paragraph;
        this.word = word;
    }

    public type1 first() {
        return this.score;
    }

    public type2 second() {
        return this.paragraph;
    }

    public type3 third() {
        return this.word;
    }

    public void setScore(type1 sc) {
        this.score = sc;
    }

    public void setParagraph(type2 par) {
        this.paragraph = par;
    }

    public void setWord(type3 wor) {
        this.word = wor;
    }
}
