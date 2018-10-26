/******************************************************************************
Copyright (c) 2018, Prasad Anjangi, Mandar Chitre
This file is released under Simplified BSD License.
Go to http://www.opensource.org/licenses/BSD-3-Clause for full license details.
******************************************************************************/

#ifndef _UNET_H_
#define _UNET_H_

#include <stdbool.h>
#include <stdint.h>

typedef void* modem_t;        ///< modem connection

/// Maximum length of a frame ID string.

#define FRAME_ID_LEN        64

/// Types of frames.

typedef enum {
  CONTROL_FRAME = 1,
  DATA_FRAME = 2,
  DATAGRAM = 3
} modem_packet_t;

/// Tx callback.
///
/// @param id               Frame ID of the transmitted frame
/// @param type             Type of the frame
/// @param time             Transmit timestamp in microseconds
/// @return                 void

typedef void (*modem_txcb_t)(const char* id, modem_packet_t type, long time);

/// Rx callback.
///
/// @param from             Address of the node from which the
///                         message is received
/// @param to               Address of the node to which the
///                         message is intended
/// @param type             Type of the frame
/// @param data             Received data
/// @param nbytes           Number of bytes
/// @param time             Receive timestamp in microseconds
/// @return                 void

typedef void (*modem_rxcb_t)(int from, int to, modem_packet_t type, void* data, int nbytes, long time);

/// Open a connection to the Subnero modem.
///
/// @param ip_address       Host name or IP address
/// @param port             Port number
/// @return                 Gateway

modem_t modem_open_eth(char* ip_address, int port);

/// Open a connection to the Subnero modem.
///
/// @param devname        Device name
/// @param baud           Baud rate
/// @param settings       RS232 settings (NULL or "N81")
/// @return               Gateway

modem_t modem_open_rs232(char* devname, int baud, const char* settings);

/// Close connection to the Subnero modem.
///
/// @param modem            Gateway
/// @return                 0 on success, error code otherwise

int modem_close(modem_t modem);

/// Transmit a frame
///
/// @param modem            Gateway
/// @param to               The to/destination node address
/// @param data             Data to send across
/// @param nbytes           Number of bytes in the data
/// @param type             Type of frame
/// @param id               Buffer to return frame ID, or NULL
/// @return                 0 on success, error code otherwise

int modem_tx_data(modem_t modem, int to, void* data, int nbytes, modem_packet_t type, char* id);

/// Register a callback to receive data.
///
/// @param modem            Gateway
/// @param callback         Pointer to function handling received data
/// @return                 0 on success, error code otherwise

int modem_set_rx_callback(modem_t modem, modem_rxcb_t callback);

/// Register a callback to receive transmission notifications.
///
/// @param modem            Gateway
/// @param callback         Pointer to function handling transmit notifications
/// @return                 0 on success, error code otherwise

int modem_set_tx_callback(modem_t modem, modem_txcb_t callback);

/// Get the range information
///
/// @param modem            Gateway
/// @param to               Address of the node to which range is requested
/// @param range            Measured range

int modem_get_range(modem_t modem, int to, float* range);

/// Get the range and bearing information
///
/// @param modem            Gateway
/// @param to               Address of the node to which range
///                         and bearing is requested
/// @param range            Measured range

int modem_get_range_and_bearing(modem_t modem, int to, float* range, float* bearing);

/// Transmit a signal.
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
///                         For baseband signal transmission set fc = 24000.
///                         For passband signal transmission set fc = 0.
/// @return                 0 on success, error code otherwise

int modem_tx_signal(modem_t modem, float* signal, int nsamples, float fc, char* id);

/// Record a passband signal.
///
/// @param modem            Gateway
/// @param buf              Buffer to store the recorded signal
/// @param nsamples         Number of samples
/// @return                 0 on success, error code otherwise

int modem_record(modem_t modem, float* buf, int nsamples);

/// Transmit and immediately record
///
/// @param modem            Gateway
/// @param buf              Buffer to store the signal to be transmitted
/// @param bufsize          Size of the buffer containing signal
/// @param npulses          Number of times signal is transmitted
/// @param pri              Pulse repetition interval in ms
/// @param recbuf           Buffer to store the recorded signal
/// @param recbufsize       Size of the buffer containing recorded signal
/// @param txtimes          Buffer to store the start transmission times of pulses
/// @return                 0 on success, -1 otherwise

int modem_tx_and_record(modem_t modem, float* buf, int bufsize, int npulses, int pri, float* recbuf, int recbufsize, int* txtimes);

/// Put modem to sleep immediately.
///
/// @param modem            Gateway
/// @return                 0 on success, -1 otherwise

int modem_sleep(modem_t modem);

/// Acoustic wakeup
///
/// @param modem            Gateway
/// @param id               Buffer to return frame ID, or NULL
/// @return                 0 on success, -1 otherwise

int modem_tx_wakeup(modem_t modem, char* id);  // remote acoustic wakeup

/// Setter for integer parameters.
///
/// @param modem            Gateway
/// @param index            Set for indexed parameters (e.g paramaters
///                         for CONTROL and DATA frames), For general
///                         modem parameters index is set to 0
/// @param target_name      Fully qulaified service class name/ agent name
/// @param param_name       Parameter name
/// @param value            Value to be set
/// @return                 0 on success, error code otherwise

int modem_iset(modem_t modem, int index, char* target_name, char* param_name, int value);

/// Setter for float parameters.
///
/// @param modem            Gateway
/// @param index            Set for indexed parameters (e.g paramaters
///                         for CONTROL and DATA frames), For general
///                         modem parameters index is set to 0
/// @param target_name      Fully qulaified service class name/ agent name
/// @param param_name       Parameter name
/// @param value            Value to be set
/// @return                 0 on success, error code otherwise

int modem_fset(modem_t modem, int index, char* target_name, char* param_name, float value);

/// Setter for boolean parameters.
///
/// @param modem            Gateway
/// @param index            Set for indexed parameters (e.g paramaters
///                         for CONTROL and DATA frames), For general
///                         modem parameters index is set to 0
/// @param target_name      Fully qulaified service class name/ agent name
/// @param param_name       Parameter name
/// @param value            Value to be set
/// @return                 0 on success, error code otherwise

int modem_bset(modem_t modem, int index, char* target_name, char* param_name, bool value);

/// Setter for String parameters.
///
/// @param modem            Gateway
/// @param index            Set for indexed parameters (e.g paramaters
///                         for CONTROL and DATA frames), For general
///                         modem parameters index is set to 0
/// @param target_name      Fully qulaified service class name/ agent name
/// @param param_name       Parameter name
/// @param value            Value to be set
/// @return                 0 on success, error code otherwise

int modem_sset(modem_t modem, int index, char* target_name, char* param_name, char* value);

/// Getter for integer parameters.
///
/// @param modem            Gateway
/// @param index            Set for indexed parameters (e.g paramaters
///                         for CONTROL and DATA frames), For general
///                         modem parameters index is set to 0
/// @param target_name      Fully qulaified service class name/ agent name
/// @param param_name       Parameter name
/// @param value            Value to get
/// @return                 0 on success, error code otherwise

int modem_iget(modem_t modem, int index, char* target_name, char* param_name, int* value);

/// Getter for float parameters.
///
/// @param modem            Gateway
/// @param index            Set for indexed parameters (e.g paramaters
///                         for CONTROL and DATA frames), For general
///                         modem parameters index is set to 0
/// @param target_name      Fully qulaified service class name/ agent name
/// @param param_name       Parameter name
/// @param value            Values to get
/// @return                 0 on success, error code otherwise

int modem_fget(modem_t modem, int index, char* target_name, char* param_name, float* value);

/// Getter for boolean parameters.
///
/// @param modem            Gateway
/// @param index            Set for indexed parameters (e.g paramaters
///                         for CONTROL and DATA frames), For general
///                         modem parameters index is set to 0
/// @param target_name      Fully qulaified service class name/ agent name
/// @param param_name       Parameter name
/// @param value            Value to get
/// @return                 0 on success, error code otherwise

int modem_bget(modem_t modem, int index, char* target_name, char* param_name, bool* value);

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
/// @return                 0 on success, error code otherwise

int modem_sget(modem_t modem, int index, char* target_name, char* param_name, char* buf, int buflen);

/// Self test for Subnero modem
///
/// @param modem            Gateway
/// @param out              File to store the test results

int modem_selftest(modem_t modem, FILE* out);


#endif
