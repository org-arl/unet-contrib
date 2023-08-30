package com.subnero.unet.nodeinfo;

import org.arl.fjage.param.Parameter;

/**
 * Node information available as parameters.
 */
public enum LocationParam implements Parameter {

  /**
   * GPS server address
   */
  ip,

  /**
   * GPS server TCP port no
   */
  port,

  /**
   * GPS coordinates in human readable format
   */
  gpsCoordinates,

  /**
   * location (latitude, longitude)
   */
  location,

  /**
   * depth (in m)
   */
  depth,

  /**
   * Time of last fix in HHMMSS.SS UTC
   */
  lastFixTime,

  /**
   * Location agent enable/disable
   */
  enable,

  /**
   * Whether to update the location parameter of node agent
   */
  updateNode
}
