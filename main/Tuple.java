package main;

public class Tuple<A,B> {

  private A fst;
  private B snd;

  public Tuple(A fst, B snd) {
    this.fst = fst;
    this.snd = snd;
  }

  public A fst() {
    return this.fst;
  }

  public B snd() {
    return this.snd;
  }

  @Override
  public String toString() {
    return "(" + this.fst.toString() + ", " + this.snd.toString() + ")";
  }
}
