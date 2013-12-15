package main.service;

import main.Tuple;

public class BruteForceFactor extends FactorService {

  private long N;
  private long p, q;

  private Tuple<Long,Long> computeFactors() {
    for (int i = 2; i <= this.N; i++) {
      if (this.N % i == 0) {
        this.p = i;
        this.q = this.N / i;
        return new Tuple<Long,Long>(this.p, this.q);
      }
    }
    return new Tuple<Long,Long>(null, null);
  }

  public BruteForceFactor(long N) {
    this.N = N;
    this.computeFactors();
  }

  public Tuple<Long,Long> getFactors() {
    return new Tuple<Long,Long>(this.p, this.q);
  }

  @Override
  public String toString() {
    Tuple<Long,Long> tup = new Tuple<Long,Long>(this.p, this.q);
    return tup.toString();
  }

}
