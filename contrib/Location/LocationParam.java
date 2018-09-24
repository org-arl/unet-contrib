package org.subnero.unet.nodeinfo;

import org.arl.unet.Parameter;

/**
 * Node information available as parameters.
 */
public enum LocationParam implements Parameter {

  /**
   * GPS server address
   */
  ip,

  /**
   * GPS server TCP port
   */
  port,

  /**
   * Retry limit to connect to server
   */
  maxRetryLimit,

  /**
   * Timeout for data read from server
   */
  timeout,

  /**
   * Maximun number of timeouts till the client retains conneciton to server.
   * Once this count is reached, the connection is terminated. if count <= 0, no termination.
   */
  maxTimeoutCnt,

  /**
   * Latitude in degree
   */
  latD,

  /**
   * Latitude in minutes
   */
  latM,

  /**
   * Longitude in degree
   */
  longD,

  /**
   * Longitude in minutes
   */
  longM,

  /**
   * Time of last fix in HHMMSS.SS UTC
   */
  lastFixTime,

  /**
   * GPS location update period in seconds
   */
  locationUpdatePeriod
  
}
