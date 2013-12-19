package main.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.*;
import main.Configuration;

public class Server {

  private static ServerSocket sock;

  public static void main(String[] args) {
    Socket s = null;
    try {
      sock = new ServerSocket(1337);
      while (true) {
        s = sock.accept();

        PrintWriter out = new PrintWriter(s.getOutputStream());
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(s.getInputStream()));

        String line = "";
        while ((line = reader.readLine()) != null) {
          if (Configuration.DEBUG) {
            System.out.println("got: " + line + "(" + line.length() + ")");
          }
          if (line.equals("HELO")) {
            if (Configuration.DEBUG) {
              System.out.println("Got HELO");
            }
            out.println("HELO\r\n");
            out.flush();
          } else {
            try {
              long N = new Long(line);
              long startTime = System.nanoTime();
              BruteForceFactor factor = new BruteForceFactor(N);
              long endTime = System.nanoTime();
              out.println(factor.toString() + String.valueOf(endTime-startTime));
              out.flush();
            } catch (NumberFormatException ex) {
              if (Configuration.DEBUG) {
                System.out.println("Number format (" + line + "): " + ex);
              }
              continue;
            }
          }
        }
      }
    } catch (IOException ioe) {
      System.out.println("IOException on socket listen: " + ioe);
      ioe.printStackTrace();
    } finally {
      try {
        s.close();
        sock.close();
      } catch (IOException ex) {
        if (Configuration.DEBUG) {
          System.out.println("Socket close IO error: " + ex);
          ex.printStackTrace();
        }
      } catch (NullPointerException ex) {
        if (Configuration.DEBUG) {
          System.out.println("Server: no connection ever accepted: " + ex);
          ex.printStackTrace();
        }
      }


    }

  }

}
