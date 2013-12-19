package main.classifier;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.HashMap;
import java.util.Map.Entry;

import main.Configuration;
import main.Tuple;

public class Clustering implements Runnable {

  private static Tuple<Double,Double> goodCluster;
  private static Tuple<Double,Double> badCluster;
  private static volatile BlockingQueue<Tuple<Double,Double>> queue;
  private static volatile HashMap<String,Tuple<Double,Double>> table;

  private static final double CONST = 0.2;

  public static void initClustering() {
    goodCluster = new Tuple<Double,Double>(0.8, 0.8);
    badCluster = new Tuple<Double,Double>(-0.8, -0.8);
    queue = new ArrayBlockingQueue<Tuple<Double,Double>>(1024);
    table = new HashMap<String,Tuple<Double,Double>>();
  }

  public static synchronized void addSample(
      String ip, Tuple<Double,Double> sample) {
    try {
      queue.put(sample);
      if (table.containsKey(ip)) {
        Tuple<Double,Double> existing = table.get(ip);
        table.put(ip, new Tuple<Double,Double>(
              existing.fst() + sample.fst(), existing.snd() + sample.snd()));
      } else {
        table.put(ip, sample);
      }
    } catch (InterruptedException ex) {
      if (Configuration.DEBUG) {
        System.out.println("Cluster queueing interruption: " + ex);
      }
    }
  }

  public static synchronized void deduct(double delta) {
    for (Entry<String,Tuple<Double,Double>> e : table.entrySet()) {
      Tuple<Double,Double> t = new Tuple<Double,Double>(
          e.getValue().fst() - delta, e.getValue().snd() - delta);
      table.put(e.getKey(), t);
    }
  }

  private static double distanceSq(
      Tuple<Double,Double> a,
      Tuple<Double,Double> b) {
    return Math.pow(a.fst() - b.fst(), 2) + Math.pow(a.snd() - b.snd(), 2);
  }

  public static boolean isGoodHost(String ip) {
    Tuple<Double,Double> t = table.get(ip);
    return distanceSq(t, goodCluster) < distanceSq(t, badCluster);
  }

  public void run() {
    Tuple<Double,Double> sample = new Tuple<Double,Double>(0.0, 0.0);
    try {
      for (; ; sample = queue.take()) {
        double goodDist = distanceSq(sample, goodCluster);
        double badDist = distanceSq(sample, badCluster);
        if (goodDist < badDist) {
          goodCluster = new Tuple<Double,Double>(
              goodCluster.fst() + CONST * (sample.fst() - goodCluster.fst()),
              goodCluster.snd() + CONST * (sample.snd() - goodCluster.snd()));
        } else {
          badCluster = new Tuple<Double,Double>(
              badCluster.fst() + CONST * (sample.fst() - badCluster.fst()),
              badCluster.snd() + CONST * (sample.snd() - badCluster.snd()));
        }
      }
    } catch (InterruptedException ex) {
      if (Configuration.DEBUG) {
        System.out.println("Clustering interruption: " + ex);
      }
    }
  }

}
