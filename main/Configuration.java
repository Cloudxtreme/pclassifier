package main;

public class Configuration {

  // Controls whether to print debug information.
  public static final boolean DEBUG = true;

  // The port the gateway which dispatches to the hosts listens on.
  public static final int GATEWAY_PORT = 1338;

  // The interval for updating the incoming request derivatives.
  public static final double INTERVAL = 5;

  // The offset by which to 'untrain' each sample.
  public static final double UNTRAIN = 0.05;

  // The offset for training under a DDoS attack.
  public static final double BETA = 0.3;

  // The offset for training under normal operation.
  public static final double ALPHA = 0.5;

  // The derivative thresholds for entering DDoS protection mode.
  public static final double DERIV = 10;
  public static final double DERIV2 = 20;

  // The list of hosts, specified in hostname:port format.
  public static String[] HOSTS = new String[]
  {
    "localhost:1337"
  };

}
