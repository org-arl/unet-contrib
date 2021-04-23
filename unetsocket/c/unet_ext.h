#ifndef _UNETEXT_H_
#define _UNETEXT_H_

#include "fjage.h"
#include "unet.h"

/// Get the range information
///
/// @param sock             Unet socket
/// @param to               Address of the node to which range is requested
/// @param range            Measured range
int unetsocket_ext_get_range(unetsocket_t sock, int to, float* range);

/// Set the transmission power level of frame
///
/// @param sock             Unet socket
/// @param index            Index of the modulation scheme (1 for CONTROL scheme and 2 for DATA scheme)
/// @param value            Transmission power level
int unetsocket_ext_set_powerlevel(unetsocket_t sock, int index, float value);

/// Transmit npulses
///
/// @param sock             Unet socket
/// @param signal           Signal to transmit
/// @param nsamples         Number of samples
/// @param npulses          Number of times the signal needs to be transmitted
/// @param pri              Pulse repetition interval (ms)

int unetsocket_ext_npulses(unetsocket_t sock, float *signal, int nsamples, int rate, int npulses, int pri);

/// Setter for integer parameters.
///
/// @param sock             Unet socket
/// @param index            Set for indexed parameters (e.g parameters
///                         for CONTROL and DATA frames), For general
///                         modem parameters index is set to 0
/// @param target_name      Fully qualified service class name/ agent name
/// @param param_name       Parameter name
/// @param value            Value to be set
/// @return                 0 on success, -1 otherwise

int unetsocket_ext_iset(unetsocket_t sock, int index, char *target_name, char *param_name, int value);

/// Setter for float parameters.
///
/// @param sock             Unet socket
/// @param index            Set for indexed parameters (e.g parameters
///                         for CONTROL and DATA frames), For general
///                         modem parameters index is set to 0
/// @param target_name      Fully qualified service class name/ agent name
/// @param param_name       Parameter name
/// @param value            Value to be set
/// @return                 0 on success, -1 otherwise

int unetsocket_ext_fset(unetsocket_t sock, int index, char *target_name, char *param_name, float value);

/// Setter for boolean parameters.
///
/// @param sock             Unet socket
/// @param index            Set for indexed parameters (e.g parameters
///                         for CONTROL and DATA frames), For general
///                         modem parameters index is set to 0
/// @param target_name      Fully qualified service class name/ agent name
/// @param param_name       Parameter name
/// @param value            Value to be set
/// @return                 0 on success, -1 otherwise

int unetsocket_ext_bset(unetsocket_t sock, int index, char *target_name, char *param_name, bool value);

/// Setter for String parameters.
///
/// @param sock             Unet socket
/// @param index            Set for indexed parameters (e.g parameters
///                         for CONTROL and DATA frames), For general
///                         modem parameters index is set to 0
/// @param target_name      Fully qualified service class name/ agent name
/// @param param_name       Parameter name
/// @param value            Value to be set
/// @return                 0 on success, -1 otherwise

int unetsocket_ext_sset(unetsocket_t sock, int index, char *target_name, char *param_name, char *value);

/// Getter for integer parameters.
///
/// @param sock             Unet socket
/// @param index            Set for indexed parameters (e.g parameters
///                         for CONTROL and DATA frames), For general
///                         modem parameters index is set to 0
/// @param target_name      Fully qualified service class name/ agent name
/// @param param_name       Parameter name
/// @param value            Value to get
/// @return                 0 on success, -1 otherwise

int unetsocket_ext_iget(unetsocket_t sock, int index, char *target_name, char *param_name, int *value);

/// Getter for float parameters.
///
/// @param sock             Unet socket
/// @param index            Set for indexed parameters (e.g parameters
///                         for CONTROL and DATA frames), For general
///                         modem parameters index is set to 0
/// @param target_name      Fully qualified service class name/ agent name
/// @param param_name       Parameter name
/// @param value            Values to get
/// @return                 0 on success, -1 otherwise

int unetsocket_ext_fget(unetsocket_t sock, int index, char *target_name, char *param_name, float *value);

/// Getter for boolean parameters.
///
/// @param sock             Unet socket
/// @param index            Set for indexed parameters (e.g parameters
///                         for CONTROL and DATA frames), For general
///                         modem parameters index is set to 0
/// @param target_name      Fully qualified service class name/ agent name
/// @param param_name       Parameter name
/// @param value            Value to get
/// @return                 0 on success, -1 otherwise

int unetsocket_ext_bget(unetsocket_t sock, int index, char *target_name, char *param_name, bool *value);

/// Getter for string parameters.
///
/// @param sock             Unet socket
/// @param index            Set for indexed parameters (e.g parameters
///                         for CONTROL and DATA frames), For general
///                         modem parameters index is set to 0
/// @param target_name      Fully qualified service class name/ agent name
/// @param param_name       Parameter name
/// @param buf              String to get
/// @param buflen           Length of the string
/// @return                 0 on success, -1 otherwise

int unetsocket_ext_sget(unetsocket_t sock, int index, char *target_name, char *param_name, char *buf, int buflen);

/// Record a passband signal.
///
/// @param sock             Unet socket
/// @param buf              Buffer to store the recorded signal
/// @param nsamples         Number of samples
/// @return                 0 on success, -1 otherwise
int unetsocket_ext_pbrecord(unetsocket_t sock, float *buf, int nsamples);

/// Transmit a signal.
///
/// For a 18-36 kHz modem, set fc to 24000 for baseband signal transmission
/// For a 4-6 kHz modem, set fc to 6000 for baseband signal transmission
/// FIXME: Add recommendations for other modems
///
/// @param sock             Unet socket
/// @param signal           Baseband signal or Passband signal
///                         Baseband signal must be an sequence of
///                         numbers with alternating real and imaginary values
///                         Passband signal is a real time series to transmit
/// @param nsamples         Number of samples
///                         For baseband signal, this is equal to number of
///                         baseband samples
/// @param fc               Signal carrier frequency in Hz
/// @return                 0 on success, -1 otherwise

int unetsocket_ext_tx_signal(unetsocket_t sock, float *signal, int nsamples, float fc, char *id);

/// Record a baseband signal.
///
/// @param sock             Unet socket
/// @param buf              Buffer to store the recorded signal
/// @param nsamples         Number of samples
/// @return                 0 on success, -1 otherwise

int unetsocket_ext_bbrecord(unetsocket_t sock, float *buf, int nsamples);

/// Ethernet wakeup
///
/// @param macaddr          6 bytes hex array mac address of the device to wake up
/// @return                 0 on success, -1 otherwise

int unetsocket_ext_ethernet_wakeup(unsigned char *macaddr);


/// RS232 wakeup
///
/// @param devname        Device name
/// @param baud           Baud rate
/// @param settings       RS232 settings (NULL or "N81")
/// @return               0 on success, -1 otherwise

int unetsocket_ext_rs232_wakeup(char *devname, int baud, const char *settings);


/// Put modem to sleep immediately.
///
/// @param sock             Unet socket
/// @return                 0 on success, -1 otherwise
int unetsocket_ext_sleep(unetsocket_t sock);

#endif
