package com.subnero.unet.nodeinfo;

import java.io.*;
import java.net.*;
import java.util.*;
import org.arl.fjage.*;
import org.arl.unet.*;
import org.arl.unet.nodeinfo.NodeInfoParam;
import org.arl.unet.utils.GpsLocalFrame;
import org.arl.fjage.param.Parameter;

/**
 * Location agent to get GPS data from a GPS server and update node agent's location parameters.
 *
 * @author Manu Ignatius
 */

public class Location extends UnetAgent {

  public final static String title = "Location update manager";
  public final static String description = "Updates node location based on GPS data from a TCP/IP GPS server.";

  private static final int MAX_EMPTY_COUNT = 5;

  private Socket clientSocket;
  private String ip = null;
  private int port = 0;
  private double latD = 0.0;
  private double latM = 0.0;
  private double longD = 0.0;
  private double longM = 0.0;
  private double[] location = new double[2];
  private double depth = 0.0;
  private String gpsCoordinates = null;
  private String lastFixTime = null;
  private boolean enable = true;
  private boolean updateNode = false;
  private boolean connected = false;
  private int emptyCount = 0;
  private AgentID notify = null;
  private TickerBehavior locationUpdate = null;
  private TickerBehavior serverConnect = null;
  private BufferedReader inFromServer = null;

  public Location() {
    // do nothing
  }

  public Location(String ip) {
    if (ip != null && !ip.isEmpty() && validateIPAddress(ip)) {
      this.ip = ip;
    }
  }

  public Location(int port) {
    if (port > 0 && port < 65536) {
      this.port = port;
    }
  }

  public Location(String ip, int port) {
    if (ip != null && !ip.isEmpty() && validateIPAddress(ip) && port > 0 && port < 65536) {
        this.ip = ip;
        this.port = port;
    }
  }

  @Override
  protected void setup() {
    notify = topic();
  }

  @Override
  protected void startup() {
    if (!connected) {
      connectToServer();
    }
  }

  @Override
  protected void shutdown() {
    if (serverConnect != null) serverConnect.stop();
    closeConnection();
    super.shutdown();
  }

  @Override
  protected List<Parameter> getParameterList() {
    return allOf(LocationParam.class);
  }

  public String getIp() {
    return this.ip;
  }

  public void setIp(String value) {
    // error check is done inside connectToServer()
    this.ip = value;

    if (connected) {
      closeConnection();
    }
    if (serverConnect != null) serverConnect.stop();
    connectToServer();
  }

  public int getPort() {
    return this.port;
  }

  public void setPort (int value) {
    // error check is done inside connectToServer()
    this.port = value;

    if (connected) {
      closeConnection();
    }
    if (serverConnect != null) serverConnect.stop();
    connectToServer();
  }

  public String getGpsCoordinates() {
    return this.gpsCoordinates;
  }

  public double[] getLocation() {
    return this.location;
  }

  public String getLastFixTime() {
    return this.lastFixTime;
  }

  public boolean getEnable() {
    return this.enable;
  }

  public double getDepth() {
    return this.depth;
  }

  public void setDepth(double value) {
    this.depth = value;
  }

  public void setEnable(boolean value) {
    this.enable = value;
  }

  public boolean getUpdateNode() {
    return this.updateNode;
  }

  public void setUpdateNode(boolean value) {
    this.updateNode = value;
  }

  private void reconnectToServer(){
    closeConnection();
    if (serverConnect != null) serverConnect.stop();
    connectToServer();
  }

  private void connectToServer() {
    log.info("Connecting to GPS server at "+ip+":"+port);

    if (ip != null && !ip.isEmpty() && validateIPAddress(ip) && port > 0 && port < 65536) {

      serverConnect = new TickerBehavior(1000) {
        @Override
        public void onTick() {
          try {
            clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(ip, port), 100);
            log.info("Connection to GPS server established");
            clientSocket.setSoTimeout(100);
            connected = true;
            serverConnect.stop();
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // once connected to server, start location update
            locationUpdate = new TickerBehavior(1000) {
              @Override
              public void onTick() {
                readNmeaData();
              }
            };
            add(locationUpdate);
          }

          catch (IOException e) {
            log.fine("IOException: Unable to connect to server, trying again...");
          }
        }
      };
      add(serverConnect);
    }
    else {
      log.warning("Invalid IP address or port number out of range, setting to default values");
    }
  }

  private void readNmeaData() {
    List<String> localBuffer = new ArrayList<String>();
    boolean connectionNeedsReset = false;
    try {
      // Empty the Buffered Reader
      String nmeaLine = null;
      while ((nmeaLine = inFromServer.readLine()) != null) localBuffer.add(nmeaLine);
      connectionNeedsReset = true;
    }catch (SocketTimeoutException e){
      // Done reading everything the socket has for now.
      log.finest("Read " + localBuffer.size() + " lines of from GPS Server.");
    }catch (Exception e){
      log.fine("Exception in reading data from GPS server : " + e.toString());
      connectionNeedsReset = true;
    }

    if (localBuffer.size() == 0 && ++emptyCount > MAX_EMPTY_COUNT) connectionNeedsReset = true;
    if (localBuffer.size() > 0 ) emptyCount = 0;

    Collections.reverse(localBuffer);

    // Filter the last GPRMC/GPGGA/GNGGA string
    String latestNmeaString = localBuffer.stream()
            .filter(l -> l.startsWith("$GPRMC") || l.startsWith("$GPGGA") || l.startsWith("$GNGGA"))
            .findFirst()
            .orElse(null);

    if (latestNmeaString != null){
      if (latestNmeaString.startsWith("$GPRMC")) {
        if (parseGPRMC(latestNmeaString)) {
          log.fine("$GPRMC: time: " + lastFixTime + ", Lat: " + latD + " deg " + latM + " min N" + ", Lon: " + longD + " deg " + longM + " min E");
          if (enable) {
            updateGpsCoordinates();
            updateLocation();
          }
        }
      } else if (latestNmeaString.startsWith("$GPGGA") || latestNmeaString.startsWith("$GNGGA")) {
        if (parseGPGGA(latestNmeaString)) {
          log.fine("$GPGGA: time: " + lastFixTime + ", Lat: " + latD + " deg " + latM + " min N" + ", Lon: " + longD + " deg " + longM + " min E");
          if (enable) {
            updateGpsCoordinates();
            updateLocation();
          }
        }
      }
    }

    if (connectionNeedsReset) {
      log.fine("GPS server connection error. Reconnecting.");
      reconnectToServer();
    }
  }

  private void closeConnection()
  {
    if (clientSocket != null) {
      try {
        log.info("Closing connection to GPS server");
        clientSocket.close();
        connected = false;
        if (locationUpdate != null) locationUpdate.stop();
      }
      catch (IOException e) {
        log.warning("IOException: Unable to close clientSocket");
      }
    }
  }

  private void updateGpsCoordinates() {
    gpsCoordinates = ""+latD + "deg " + latM + "'N" + ", " + longD + "deg " + longM + "'E";
    ParamChangeNtf ntf = new ParamChangeNtf(notify);
    ntf.set(LocationParam.gpsCoordinates, gpsCoordinates);
    send(ntf);
  }

  private void updateLocation() {

    double latDec = latD + latM/60;
    double longDec = longD + longM/60;
    location[0] = latDec;
    location[1] = longDec;
    log.fine("Location [latDeg, longDeg]: [" + latDec + ", " + longDec + "]");
    ParamChangeNtf ntf = new ParamChangeNtf(notify);
    ntf.set(LocationParam.location, location);
    send(ntf);

    // update node Agent's location
    AgentID node = agentForService(Services.NODE_INFO);
    double[] origin = (double[])get(node, NodeInfoParam.origin);

    if (origin == null || origin.length < 2) {
      double[] curLocation3 = new double[3];
      curLocation3[0] = latDec;
      curLocation3[1] = longDec;
      curLocation3[2] = depth;
      if (updateNode) set(node, NodeInfoParam.location, curLocation3);
    } else {
      // Convert to local coordinates and update node agent's location
      double[] xyCoordinates;
      GpsLocalFrame gps = new GpsLocalFrame(origin[0], origin[1]);
      xyCoordinates = gps.toLocal(latD, latM, longD, longM);
      if (xyCoordinates.length == 2) {
        double[] curLocation3 = new double[3];
        curLocation3[0] = xyCoordinates[0];
        curLocation3[1] = xyCoordinates[1];
        curLocation3[2] = depth;
        if (updateNode) set(node, NodeInfoParam.location, curLocation3);
      }
      log.fine("Local coordinates: x = " + xyCoordinates[0] + ", y = " + xyCoordinates[1] + ", depth = " + depth);
    }
  }

  private boolean parseGPRMC(String nl) {
    //log.info(nl);
    String[] gprmc = nl.split(",");
    if (gprmc[2].equals("A")) {

      lastFixTime = gprmc[1].substring(0,2)+":"+gprmc[1].substring(2,4)+":"+gprmc[1].substring(4,6)+gprmc[1].substring(6)+" UTC";

      String latd = gprmc[3].split("\\.")[0];
      String latm1 = gprmc[3].split("\\.")[1];
      String lond = gprmc[5].split("\\.")[0];
      String lonm1 = gprmc[5].split("\\.")[1];

      String latm = latd.substring(latd.length()-2);
      latm=latm.concat(".");
      latm=latm.concat(latm1);
      latd = latd.substring(0, latd.length()-2);

      String lonm = lond.substring(lond.length()-2);
      lonm=lonm.concat(".");
      lonm=lonm.concat(lonm1);
      lond = lond.substring(0, lond.length()-2);

      latD = Double.parseDouble(latd);
      latM = Double.parseDouble(latm);
      longD = Double.parseDouble(lond);
      longM = Double.parseDouble(lonm);

      return true;
    }
    return false;
  }

  private boolean parseGPGGA(String nl) {
    //log.info(nl);
    String[] gpgga = nl.split(",");
    if (gpgga[2] != null && !gpgga[2].isEmpty()) {

      lastFixTime = gpgga[1].substring(0,2)+":"+gpgga[1].substring(2,4)+":"+gpgga[1].substring(4,6)+gpgga[1].substring(6)+" UTC";

      String latd = gpgga[2].split("\\.")[0];
      String latm1 = gpgga[2].split("\\.")[1];
      String lond = gpgga[4].split("\\.")[0];
      String lonm1 = gpgga[4].split("\\.")[1];

      String latm = latd.substring(latd.length()-2);
      latm=latm.concat(".");
      latm=latm.concat(latm1);
      latd = latd.substring(0, latd.length()-2);

      String lonm = lond.substring(lond.length()-2);
      lonm=lonm.concat(".");
      lonm=lonm.concat(lonm1);
      lond = lond.substring(0, lond.length()-2);

      latD = Double.parseDouble(latd);
      latM = Double.parseDouble(latm);
      longD = Double.parseDouble(lond);
      longM = Double.parseDouble(lonm);

      return true;
    }
    return false;
  }

  private boolean validateIPAddress(String ipAddress) {
    if (ipAddress.equals("localhost")) return true;
    String[] tokens = ipAddress.split("\\.");
    if (tokens.length != 4) {
      log.warning("Invalid IP address");
      return false;
    }
    for (String str : tokens) {
      int i = Integer.parseInt(str);
      if ((i < 0) || (i > 255)) {
        log.warning("Invalid IP address");
        return false;
      }
    }
    return true;
  }

}
