package main;

import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class Host implements Comparable<Host>, Runnable {

  private int loadScore;
  private String hostname;
  private int port;
  private Socket socket;
  private volatile LinkedList<Socket> incoming;

  public Host(String hostname, int port) {
    this.loadScore = 0;
    this.hostname = hostname;
    this.port = port;
    this.incoming = new LinkedList<Socket>();

    if (Configuration.DEBUG) {
      System.out.println("Constructing host for " +
          this.hostname + ":" + Integer.toString(this.port));
    }

    try {
      this.socket = new Socket(hostname, port);
    } catch (UnknownHostException ex) {
      if (Configuration.DEBUG) {
        System.out.println("Unknown host: " + this.hostname);
        ex.printStackTrace();
      }
    } catch (IOException ex) {
      if (Configuration.DEBUG) {
        System.out.println("IO error (constructor): " + ex);
        ex.printStackTrace();
      }
    }

    String read = this.write("HELO");
    if (read.equals("HELO")) {
      System.out.println("Discovered " + this.toString());
    } else {
      if (Configuration.DEBUG) {
        System.out.println("Received: " + read);
      }
    }

    /*synchronized(this.incoming) {
      try {
        this.incoming.wait();
      } catch (InterruptedException ex) {}
    }*/
  }

  public String write(String message) {
    try {
      PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
      BufferedReader in = new BufferedReader(
          new InputStreamReader(this.socket.getInputStream()));
      out.println(message);
      out.flush();
      String line = null;
      while ((line = in.readLine()) != null) {
        String[] parts = line.split(".");
        if (parts.length > 1) {
          this.loadScore = new Integer(parts[1]);
          if (Configuration.DEBUG) {
            System.out.println("Setting load score for " + this.toString() +
                ": " + Integer.toString(this.loadScore));
          }
        }
        return line;
      }
    } catch (IOException ex) {
      if (Configuration.DEBUG) {
        System.out.println("IO error: " + ex);
        ex.printStackTrace();
      }
    }
    return null;
  }

  public synchronized void addConnection(Socket sock) {
    this.incoming.add(sock);
    //this.incoming.notify();
  }

  public int getPort() {
    return this.port;
  }

  public int getLoadScore() {
    return this.loadScore;
  }

  public void run() {
    while (true) {
      if (this.incoming.size() > 0) {
        Socket sock = null;
        try {
          sock = this.incoming.poll();
          if (Configuration.DEBUG) {
            System.out.println("Polling incoming for socket...");
          }
          PrintStream out = new PrintStream(sock.getOutputStream());
          BufferedReader reader = new BufferedReader(
              new InputStreamReader(sock.getInputStream()));

          String line = reader.readLine();
          if (Configuration.DEBUG) {
            System.out.println("Read from socket: " + line);
          }
          out.println(this.write(line));
          out.flush();
          this.incoming.add(sock);
        } catch (IOException ex) {
          if (Configuration.DEBUG) {
            System.out.println("IO error: " + ex);
            ex.printStackTrace();
          }
        } finally {
          Gateway.addHost(this);
          if (sock != null) {
            this.incoming.add(sock);
          }
        }
      }
    }
  }

  public int compareTo(Host other) {
    if (this.loadScore > other.getLoadScore()) {
      return 1;
    } else if (this.loadScore < other.getLoadScore()) {
      return -1;
    } else {
      return 0;
    }
  }

  @Override
  public String toString() {
    return this.hostname + ":" + Integer.toString(this.port);
  }
}
