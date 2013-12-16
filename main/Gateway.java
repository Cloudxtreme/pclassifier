package main;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;

import main.classifier.Clustering;
import main.service.Server;

public class Gateway {

  private static volatile PriorityQueue<Host> hosts;
  private static boolean ddos;
  public static int numRequests;
  private static volatile int tempRequests;
  public static double deriv, deriv2;

  public static synchronized void addHost(Host h) {
    hosts.add(h);
  }

  public static synchronized void flushTempRequests() {
    numRequests += tempRequests;
    tempRequests = 0;
  }

  public static synchronized void setDerivatives(double d, double d2) {
    deriv = d;
    deriv2 = d2;
    if (deriv > Configuration.DERIV &&
        deriv2 > Configuration.DERIV2) {
      ddos = true;
    } else if (deriv < -Configuration.DERIV &&
               deriv2 < -Configuration.DERIV2) {
      ddos = false;
    }
  }

  public static void main(String args[]) {
    ddos = false;
    deriv = 0.0;
    deriv2 = 0.0;
    tempRequests = 0;
    hosts = new PriorityQueue<Host>(Configuration.HOSTS.length);
    Clustering.initClustering();

    if (Configuration.DEBUG) {
      System.out.println("Forking updater thread...");
    }
    Thread updateThread = new Thread(new Updater());
    updateThread.start();

    for (String hostport : Configuration.HOSTS) {
      String[] parts = hostport.split(":");
      Host host = new Host(parts[0], new Integer(parts[1]));
      addHost(host);
      /*Thread t = new Thread(host);
      if (Configuration.DEBUG) {
        System.out.println("Forking thread: " + parts[0] + ":" + parts[1]);
      }
      t.start();*/
    }

    ServerSocket listener;
    try {
      listener = new ServerSocket(Configuration.GATEWAY_PORT);
      tempRequests++;
      Socket sock;

      while (true) {
        if (hosts.size() > 0) {
          sock = listener.accept();
          double init = ddos ? Configuration.BETA : Configuration.ALPHA;
          String ip = sock.getRemoteSocketAddress().toString();
          Clustering.addSample(ip, new Tuple<Double,Double>(init, init));
          if (Clustering.isGoodHost(ip)) {
            Host next = hosts.peek();
            if (Configuration.DEBUG) {
              System.out.println("Queueing connection for " + next.toString());
            }
            next.addConnection(sock);
            Thread t = new Thread(next);
            t.start();
          }
        }
      }
    } catch (IOException ex) {
      System.out.println("IOException on listen: " + ex);
      ex.printStackTrace();
    }
  }

}
