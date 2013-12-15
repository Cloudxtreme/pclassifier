package main;

public class Configuration {

  // Controls whether to print debug information.
  public static final boolean DEBUG = true;

  // The port the gateway which dispatches to the hosts listens on.
  public static final int GATEWAY_PORT = 1338;

  // The interval for updating the incoming request derivatives.
  public static final double INTERVAL = 0.5;

  // The list of hosts, specified in hostname:port format.
  public static String[] HOSTS = new String[]
  {
    "localhost:1337"
  };

}
