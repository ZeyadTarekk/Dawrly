public class Pair3<type1, type2, type3, type4> {
    public type1 score;
    public type2 paragraph;
    public type3 title;
    public type4 word;

    public Pair3() {
    }

    @Override
    public String toString() {
        return "Pair{" +
                "Score =" + score +
                ", paragraph=" + paragraph +
                ", Title=" + title +
                ", Word=" + word +
                '}';
    }

    public Pair3(type1 score, type2 paragraph, type3 title, type4 word) {
        this.score = score;
        this.paragraph = paragraph;
        this.title = title;
        this.word = word;
    }

    public type1 getScore() {
        return this.score;
    }

    public type2 getParagraph() {
        return this.paragraph;
    }

    public type3 getTitle() {
        return this.title;
    }

    public type4 getWord() {
        return this.word;
    }

    public void setScore(type1 sc) {
        this.score = sc;
    }

    public void setParagraph(type2 par) {
        this.paragraph = par;
    }

    public void setTitle(type3 wor) {
        this.title = wor;
    }

    public void setWord(type4 wor) {
        this.word = wor;
    }
}
