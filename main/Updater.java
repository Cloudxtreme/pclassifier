package main;

import main.classifier.Clustering;

public class Updater implements Runnable {

  public void run() {

    while (true) {
      int old = Gateway.numRequests;
      Gateway.flushTempRequests();

      Gateway.setDerivatives(
          (Gateway.numRequests - old) / Configuration.INTERVAL,
          (Gateway.numRequests - old - Gateway.deriv * Configuration.INTERVAL)
            / (Configuration.INTERVAL * Configuration.INTERVAL));

      Clustering.deduct(Configuration.UNTRAIN);

      try {
        Thread.sleep((long)(Configuration.INTERVAL * 1000));
      } catch (InterruptedException ex) {
        if (Configuration.DEBUG) {
          System.out.println("Updater interrupted: " + ex);
          ex.printStackTrace();
        }
      }
    }
  }

}
