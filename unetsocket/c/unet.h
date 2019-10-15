#ifndef _UNET_H_
#define _UNET_H_

#include <stdbool.h>
#include <stdint.h>

typedef void *modem_t;        ///< modem connection

// Services
#define PHYSICAL                 "org.arl.unet.Services.PHYSICAL"
#define BASEBAND                 "org.arl.unet.Services.BASEBAND"
#define RANGING                  "org.arl.unet.Services.RANGING"
#define SCHEDULER                "org.arl.unet.Services.SCHEDULER"
#define SHELL                    "org.arl.fjage.shell.Services.SHELL"

// Messages
#define TXFRAMEREQ               "org.arl.unet.phy.TxFrameReq"
#define TXFRAMENTF               "org.arl.unet.phy.TxFrameNtf"
#define RXFRAMENTF               "org.arl.unet.phy.RxFrameNtf"
#define TXFRAMESTARTNTF          "org.arl.unet.phy.TxFrameStartNtf"
#define DATAGRAMREQ              "org.arl.unet.DatagramReq"
#define RANGEREQ                 "org.arl.unet.phy.RangeReq"
#define RANGENTF                 "org.arl.unet.phy.RangeNtf"
#define TXBASEBANDSIGNALREQ      "org.arl.unet.bb.TxBasebandSignalReq"
#define RECORDBASEBANDSIGNALREQ  "org.arl.unet.bb.RecordBasebandSignalReq"
#define RXBASEBANDSIGNALNTF      "org.arl.unet.bb.RxBasebandSignalNtf"
#define ADDSCHEDULEDSLEEPREQ     "org.arl.unet.scheduler.AddScheduledSleepReq"
#define PARAMETERREQ             "org.arl.unet.ParameterReq"

/// Port number

#define PORT                1100

// Tx offset

#define TX_OFFSET           200

/// Pre-amplifier gain in dB

#define TRIS_PGA_GAIN_0       0
#define TRIS_PGA_GAIN_6       6
#define TRIS_PGA_GAIN_12      12
#define TRIS_PGA_GAIN_18      18
#define TRIS_PGA_GAIN_24      24
#define TRIS_PGA_GAIN_30      30
#define TRIS_PGA_GAIN_36      36

/// Maximum length of a frame ID string

#define FRAME_ID_LEN        64

/// Transmit sampling rate

#define TXSAMPLINGFREQ           192000 //Hz

/// Timeout

#define TIMEOUT                  1000   //ms

/// Types of frames.

typedef enum
{
    CONTROL_FRAME = 1,
    DATA_FRAME = 2,
    DATAGRAM = 3
} modem_packet_t;

/// Tx callback.
///
/// @param id               Frame ID of the transmitted frame
/// @param type             Type of the frame
/// @param txtime           Transmit start timestamp in microseconds
/// @return                 void

typedef void (*modem_txcb_t)(const char *id, modem_packet_t type, long txtime);

/// Rx callback.
///
/// @param from             Address of the node from which the
///                         message is received
/// @param to               Address of the node to which the
///                         message is intended
/// @param type             Type of the frame
/// @param data             Received data
/// @param nbytes           Number of bytes
/// @param rxtime           Receive start timestamp in microseconds
/// @return                 void
///
/// [NOTE: The received data through this callback is freed after this callback returns. Therefore, the user must copy the data buffer if needed.]

typedef void (*modem_rxcb_t)(int from, int to, modem_packet_t type, void *data, int nbytes, long rxtime);

/// Open a connection to the Subnero modem.
///
/// @param ip_address       Host name or IP address
/// @param port             Port number
/// @return                 Gateway

modem_t modem_open_eth(char *ip_address, int port);

/// Open a connection to the Subnero modem.
///
/// @param devname        Device name
/// @param baud           Baud rate
/// @param settings       RS232 settings (NULL or "N81")
/// @return               Gateway

modem_t modem_open_rs232(char *devname, int baud, const char *settings);

/// Close connection to the Subnero modem.
///
/// @param modem            Gateway
/// @return                 0 on success, -1 otherwise

int modem_close(modem_t modem);

/// Transmit a frame
///
/// @param modem            Gateway
/// @param to               The to/destination node address
/// @param data             Data to send across
/// @param nbytes           Number of bytes in the data
/// @param type             Type of frame
/// @param id               Buffer to return frame ID, or NULL
/// @return                 0 on success, -1 otherwise

int modem_tx_data(modem_t modem, int to, void *data, int nbytes, modem_packet_t type, char *id);

/// Register a callback to receive data.
///
/// @param modem            Gateway
/// @param callback         Pointer to function handling received data
/// @return                 0 on success, -1 otherwise

int modem_set_rx_callback(modem_t modem, modem_rxcb_t callback);

/// Register a callback to receive transmission notifications.
///
/// @param modem            Gateway
/// @param callback         Pointer to function handling transmit notifications
/// @return                 0 on success, -1 otherwise

int modem_set_tx_callback(modem_t modem, modem_txcb_t callback);

/// Get the range information
///
/// @param modem            Gateway
/// @param to               Address of the node to which range is requested
/// @param range            Measured range
/// @return                 0 on success, -1 otherwise

int modem_get_range(modem_t modem, int to, float *range);

/// Transmit a signal.
///
/// For a 18-36 KHz modem, set fc to 24000 for baseband signal transmission
///
/// @param modem            Gateway
/// @param signal           Baseband signal or Passband signal.
///                         Baseband signal must be an sequence of
///                         numbers with alternating real and imaginary values.
///                         Passband signal is a real time series to transmit.
/// @param nsamples         Number of samples.
///                         For baseband signal, this is equal to number of
///                         baseband samples.
/// @param rate             Baseband/ Passband sampling rate of signal in Hz
/// @param fc               Signal carrier frequency in Hz for passband transmission.
/// @return                 0 on success, -1 otherwise

int modem_tx_signal(modem_t modem, float *signal, int nsamples, int rate, float fc, char *id);

/// Record a baseband signal.
///
/// @param modem            Gateway
/// @param buf              Buffer to store the recorded signal
/// @param nsamples         Number of samples
/// @return                 0 on success, -1 otherwise

int modem_record(modem_t modem, float *buf, int nsamples);

/// Set ADC sampling rate.
///
/// @param modem            Gateway
/// @param recordrate       Sampling rate for recording
/// @return                 0 on success, -1 otherwise

int modem_setrecordingrate(modem_t modem, int recordrate);

/// Put modem to sleep immediately.
///
/// @param modem            Gateway
/// @return                 0 on success, -1 otherwise

int modem_sleep(modem_t modem);

/// Ethernet wakeup
///
/// @param macaddr          6 bytes hex array mac address of the device to wake up
/// @return                 0 on success, -1 otherwise

int modem_ethernet_wakeup(unsigned char *macaddr);


/// RS232 wakeup
///
/// @param devname        Device name
/// @param baud           Baud rate
/// @param settings       RS232 settings (NULL or "N81")
/// @return               0 on success, -1 otherwise

int modem_rs232_wakeup(char *devname, int baud, const char *settings);

/// Acoustic wakeup
///
/// @param modem            Gateway
/// @param id               Buffer to return frame ID, or NULL
/// @return                 0 on success, -1 otherwise

int modem_tx_wakeup(modem_t modem, char *id);

/// Setter for integer parameters.
///
/// @param modem            Gateway
/// @param index            Set for indexed parameters (e.g paramaters
///                         for CONTROL and DATA frames), For general
///                         modem parameters index is set to 0
/// @param target_name      Fully qulaified service class name/ agent name
/// @param param_name       Parameter name
/// @param value            Value to be set
/// @return                 0 on success, -1 otherwise

int modem_iset(modem_t modem, int index, char *target_name, char *param_name, int value);

/// Setter for float parameters.
///
/// @param modem            Gateway
/// @param index            Set for indexed parameters (e.g paramaters
///                         for CONTROL and DATA frames), For general
///                         modem parameters index is set to 0
/// @param target_name      Fully qulaified service class name/ agent name
/// @param param_name       Parameter name
/// @param value            Value to be set
/// @return                 0 on success, -1 otherwise

int modem_fset(modem_t modem, int index, char *target_name, char *param_name, float value);

/// Setter for boolean parameters.
///
/// @param modem            Gateway
/// @param index            Set for indexed parameters (e.g paramaters
///                         for CONTROL and DATA frames), For general
///                         modem parameters index is set to 0
/// @param target_name      Fully qulaified service class name/ agent name
/// @param param_name       Parameter name
/// @param value            Value to be set
/// @return                 0 on success, -1 otherwise

int modem_bset(modem_t modem, int index, char *target_name, char *param_name, bool value);

/// Setter for String parameters.
///
/// @param modem            Gateway
/// @param index            Set for indexed parameters (e.g paramaters
///                         for CONTROL and DATA frames), For general
///                         modem parameters index is set to 0
/// @param target_name      Fully qulaified service class name/ agent name
/// @param param_name       Parameter name
/// @param value            Value to be set
/// @return                 0 on success, -1 otherwise

int modem_sset(modem_t modem, int index, char *target_name, char *param_name, char *value);

/// Getter for integer parameters.
///
/// @param modem            Gateway
/// @param index            Set for indexed parameters (e.g paramaters
///                         for CONTROL and DATA frames), For general
///                         modem parameters index is set to 0
/// @param target_name      Fully qulaified service class name/ agent name
/// @param param_name       Parameter name
/// @param value            Value to get
/// @return                 0 on success, -1 otherwise

int modem_iget(modem_t modem, int index, char *target_name, char *param_name, int *value);

/// Getter for float parameters.
///
/// @param modem            Gateway
/// @param index            Set for indexed parameters (e.g paramaters
///                         for CONTROL and DATA frames), For general
///                         modem parameters index is set to 0
/// @param target_name      Fully qulaified service class name/ agent name
/// @param param_name       Parameter name
/// @param value            Values to get
/// @return                 0 on success, -1 otherwise

int modem_fget(modem_t modem, int index, char *target_name, char *param_name, float *value);

/// Getter for boolean parameters.
///
/// @param modem            Gateway
/// @param index            Set for indexed parameters (e.g paramaters
///                         for CONTROL and DATA frames), For general
///                         modem parameters index is set to 0
/// @param target_name      Fully qulaified service class name/ agent name
/// @param param_name       Parameter name
/// @param value            Value to get
/// @return                 0 on success, -1 otherwise

int modem_bget(modem_t modem, int index, char *target_name, char *param_name, bool *value);

/// Getter for string parameters.
///
/// @param modem            Gateway
/// @param index            Set for indexed parameters (e.g paramaters
///                         for CONTROL and DATA frames), For general
///                         modem parameters index is set to 0
/// @param target_name      Fully qulaified service class name/ agent name
/// @param param_name       Parameter name
/// @param buf              String to get
/// @param buflen           Length of the string
/// @return                 0 on success, -1 otherwise

int modem_sget(modem_t modem, int index, char *target_name, char *param_name, char *buf, int buflen);


#endif
