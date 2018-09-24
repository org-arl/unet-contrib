package org.subnero.unet.nodeinfo;

import java.io.*;
import java.net.*;
import java.util.List;
import org.arl.fjage.*;
import org.arl.unet.*;
import org.arl.unet.nodeinfo.NodeInfoParam;
import org.arl.unet.utils.GpsLocalFrame;
import org.arl.unet.ParameterReq;

/**
 * Location agent to get GPS data from a GPS server and update node agent's location parameters.
 *
 * @author Manu Ignatius
 */

public class Location extends UnetAgent {

  private Socket clientSocket;
  private String ip = null;
  private int port = 0;
  private int timeout = 100;
  private int timeoutCnt = 0;
  private int maxTimeoutCnt = 100;
  private int maxRetryLimit = 5;
  private int locationUpdatePeriod = 10;
  private double latD = 0.0;
  private double latM = 0.0;
  private double longD = 0.0;
  private double longM = 0.0;
  private String lastFixTime = null;
  private String nmeaLine = null;
  private boolean connected = false;
  private TickerBehavior locationUpdate = null;

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
    // do nothing
  }

  @Override
  protected void startup() {
    if (!connected) {
      connectToServer();
    }
  }

  @Override
  protected void shutdown() {
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
    if (value != null && !value.isEmpty() && validateIPAddress(value)) {
      this.ip = value;

      if (connected) {
        closeConnection();
      }
      connectToServer();
    }
  }

  public int getPort() {
    return this.port;
  }

  public void setPort (int value) {
    if (value > 0 && value < 65536) {
      this.port = value;

      if (connected) {
        closeConnection();
      }
      connectToServer();
    }
  }

  public double getLatD() {
    return this.latD;
  }

  public double getLatM() {
    return this.latM;
  }

  public double getLongD() {
    return this.longD;
  }

  public double getLongM() {
    return this.longM;
  }

  public String getLastFixTime() {
    return this.lastFixTime;
  }

  public int getMaxRetryLimit() {
    return this.maxRetryLimit;
  }

  public void setMaxRetryLimit(int value) {
    if (value > 0) {
      this.maxRetryLimit = value;
    }
  }

  public int getTimeout() {
    return this.timeout;
  }

  public void setTimeout(int value) {
    if (value > 0) {
      this.timeout = value;
    }
  }

  public int getMaxTimeoutCnt() {
    return this.maxTimeoutCnt;
  }

  public void setMaxTimeoutCnt(int value) {
      this.maxTimeoutCnt = value;
      timeoutCnt = 0;
  }

  public int getLocationUpdatePeriod() {
    return this.locationUpdatePeriod;
  }

  public void setLocationUpdatePeriod(int value) {
    if (value >= 0) {
      this.locationUpdatePeriod = value;
      if (locationUpdate != null) locationUpdate.stop();
      if (value == 0) locationUpdate = null;
      else {
        locationUpdate = new TickerBehavior(value*1000) {
          @Override
          public void onTick() {
            readNmeaData();
          }
        };
        add(locationUpdate);
      }
    }
  }

  private void connectToServer() {
    log.info("Conneting to new GPS server");

    int rl = maxRetryLimit;
    if (ip != null && !ip.isEmpty() && validateIPAddress(ip) && port > 0 && port < 65536) {
      while (rl > 0) {
        try {
          clientSocket = new Socket();
          clientSocket.connect(new InetSocketAddress(ip, port), timeout);
          log.info("Location Agent: Connection to GPS server established");
          clientSocket.setSoTimeout(timeout);
          connected = true;
          break;
        }
        catch (IOException e) {
          log.warning("IOException: Unable to connect to server");
          rl--;
        }
      }
      if (rl <= 0) {
        log.info("Maximum retry limit reached. Exiting now");
      }
      else if (locationUpdatePeriod > 0) {
        locationUpdate = new TickerBehavior(locationUpdatePeriod*1000) {
          @Override
          public void onTick() {
            readNmeaData();
          }
        };
        add(locationUpdate);
      }
      else {
        // Connected to server, but location update period is 0. So, do nothing.
      }
    }
    else {
      log.warning("Invalid IP address or port number out of range");
    }
  }

  private void readNmeaData() {
    try {
      BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      if ((nmeaLine = inFromServer.readLine()) != null) {
        timeoutCnt = 0;

        if (nmeaLine.startsWith("$GPRMC")) {
          if (parseGPRMC(nmeaLine))
          {
            log.info("$GPRMC: time: " + lastFixTime + ", Latd: " + latD + " deg " + latM + " min N" + ", Lon: " + longD + " deg " + longM + " min E");
            updateLocation();
          }
        }
        else if (nmeaLine.startsWith("$GPGGA") || nmeaLine.startsWith("$GNGGA")) {
          if (parseGPGGA(nmeaLine)) {
            log.info("$GPGGA: time: " + lastFixTime + ", Latd: " + latD + " deg " + latM + " min N" + ", Lon: " + longD + " deg " + longM + " min E");
            updateLocation();
          }
        }
      }
    }
    catch (IOException e) {
      log.warning("IOException: Attempt to read data from GPS server timed out ("+timeoutCnt+")");
      if (maxTimeoutCnt > 0) {
        timeoutCnt++;
        if (timeoutCnt >= maxTimeoutCnt) {
          log.warning("Max timeout count reached. Closing connection to server");
          if (locationUpdate != null) locationUpdate.stop();
          timeoutCnt = 0;
          closeConnection();
        }
      }
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
        timeoutCnt = 0;
      }
      catch (IOException e) {
        log.warning("IOException: Unable to close clientSocket");
      }
    }
  }

  private void updateLocation () {
    ParameterReq req = new ParameterReq(agentForService(Services.NODE_INFO));
    req.get(NodeInfoParam.origin);
    ParameterRsp rsp = (ParameterRsp) request(req, 1000);
    double[] origin = (double[])rsp.get(NodeInfoParam.origin);
    if (origin == null) {
      log.warning("No response from agent");
    }
    if (origin.length < 2) {
      log.warning("Origin not set, setting current location as origin");

      // Convert lat long to decimal format
      double latDec = latD + latM/60;
      double longDec = longD + longM/60;
      double newOrigin[] = new double[2];
      newOrigin[0] = latDec;
      newOrigin[1] = longDec;

      // If origin is not set, set the current location as origin.
      req = new ParameterReq(agentForService(Services.NODE_INFO));
      req.set(NodeInfoParam.origin, newOrigin);
      rsp = (ParameterRsp)request(req, 1000);
    }
    else {
      // Convert to local coordinates and update current location
      GpsLocalFrame gps = new GpsLocalFrame(origin[0], origin[1]);
      double[] xy = new double[2];
      xy = gps.toLocal(latD, latM, longD, longM);
      if (xy.length == 2) {
        req = new ParameterReq(agentForService(Services.NODE_INFO));
        req.set(NodeInfoParam.location, xy);
        rsp = (ParameterRsp)request(req, 1000);
      }
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
