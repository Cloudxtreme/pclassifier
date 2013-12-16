package main;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;

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

  public static void main(String args[]) {
    ddos = false;
    deriv = 0.0;
    deriv2 = 0.0;
    tempRequests = 0;
    hosts = new PriorityQueue<Host>(Configuration.HOSTS.length);

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
          Host next = hosts.peek();
          if (Configuration.DEBUG) {
            System.out.println("Queueing connection for " + next.toString());
          }
          next.addConnection(sock);
          Thread t = new Thread(next);
          t.start();
        }
      }
    } catch (IOException ex) {
      System.out.println("IOException on listen: " + ex);
      ex.printStackTrace();
    }
  }

}
