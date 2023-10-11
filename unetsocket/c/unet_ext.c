#define _DEFAULT_SOURCE
#include <stdlib.h>
#include <errno.h>
#include "pthreadwindows.h"
#include "fjage.h"
#include "unet.h"
#include "unet_ext.h"
#include <math.h>
#include <stdio.h>
#include <string.h>
#include <inttypes.h>

#ifndef _WIN32
#include <sys/time.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#endif

typedef struct {
  fjage_gw_t gw;
  pthread_mutex_t rxlock, txlock;
  int local_protocol;
  int remote_address;
  int remote_protocol;
  long timeout;
  fjage_aid_t provider;
  bool quit;
  fjage_msg_t ntf;
} _unetsocket_t;

static int error(const char *msg)
{
  fprintf(stderr, "\n*** ERROR: %s\n\n", msg);
  return -1;
}

static fjage_msg_t receive(_unetsocket_t *usock, const char *clazz, const char *id, long timeout) {
  pthread_mutex_lock(&usock->txlock);
  fjage_interrupt(usock->gw);
  int rv = pthread_mutex_trylock(&usock->rxlock);
  while (rv == EBUSY)
  {
    Sleep(100);
    fjage_interrupt(usock->gw);
    rv = pthread_mutex_trylock(&usock->rxlock);
  }
  fjage_msg_t msg = fjage_receive(usock->gw, clazz, id, timeout);
  pthread_mutex_unlock(&usock->rxlock);
  pthread_mutex_unlock(&usock->txlock);
  return msg;
}

static fjage_msg_t request(_unetsocket_t *usock, const fjage_msg_t request, long timeout) {
  pthread_mutex_lock(&usock->txlock);
  fjage_interrupt(usock->gw);
  int rv = pthread_mutex_trylock(&usock->rxlock);
  while (rv == EBUSY)
  {
    Sleep(100);
    fjage_interrupt(usock->gw);
    rv = pthread_mutex_trylock(&usock->rxlock);
  }
  fjage_msg_t msg = fjage_request(usock->gw, request, timeout);
  pthread_mutex_unlock(&usock->rxlock);
  pthread_mutex_unlock(&usock->txlock);
  return msg;
}

static fjage_aid_t agent_for_service(_unetsocket_t *usock, const char *service) {
  pthread_mutex_lock(&usock->txlock);
  fjage_interrupt(usock->gw);
  int rv = pthread_mutex_trylock(&usock->rxlock);
  while (rv == EBUSY)
  {
    Sleep(100);
    fjage_interrupt(usock->gw);
    rv = pthread_mutex_trylock(&usock->rxlock);
  }
  fjage_aid_t aid = fjage_agent_for_service(usock->gw, service);
  pthread_mutex_unlock(&usock->rxlock);
  pthread_mutex_unlock(&usock->txlock);
  return aid;
}

static int agents_for_service(_unetsocket_t *usock, const char *service, fjage_aid_t* agents, int max) {
  pthread_mutex_lock(&usock->txlock);
  fjage_interrupt(usock->gw);
  int rv = pthread_mutex_trylock(&usock->rxlock);
  while (rv == EBUSY)
  {
    Sleep(100);
    fjage_interrupt(usock->gw);
    rv = pthread_mutex_trylock(&usock->rxlock);
  }
  int as = fjage_agents_for_service(usock->gw, service, agents, max);
  pthread_mutex_unlock(&usock->rxlock);
  pthread_mutex_unlock(&usock->txlock);
  return as;
}

int unetsocket_ext_get_range(unetsocket_t sock, int to, float* range) {
  if (sock == NULL) return -1;
  _unetsocket_t *usock = sock;
  fjage_aid_t ranging;
  fjage_msg_t msg;
  ranging = fjage_agent_for_service(usock->gw, "org.arl.unet.Services.RANGING");
  fjage_subscribe_agent(usock->gw, ranging);
  msg = fjage_msg_create(rangereq, FJAGE_REQUEST);
  fjage_msg_set_recipient(msg, ranging);
  fjage_msg_add_int(msg, "to", to);
  msg = request(usock, msg, TIMEOUT);
  if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_AGREE) {
    fjage_msg_destroy(msg);
    msg = receive(usock, rangentf, NULL, 30000);
    if (msg != NULL) {
      *range = fjage_msg_get_float(msg, "range", 0);
      fjage_msg_destroy(msg);
      return 0;
    }
  }
  fjage_msg_destroy(msg);
  return -1;
}

int unetsocket_ext_set_powerlevel(unetsocket_t sock, int index, float value) {
  if (sock == NULL) return -1;
  _unetsocket_t *usock = sock;
  fjage_msg_t msg;
  fjage_aid_t phy;
  phy = fjage_agent_for_service(usock->gw, "org.arl.unet.Services.PHYSICAL");
  msg = fjage_msg_create(parameterreq, FJAGE_REQUEST);
  fjage_msg_set_recipient(msg, phy);
  if (index == 0) index = -1;
  fjage_msg_add_int(msg, "index", index);
  fjage_msg_add_string(msg, "param", "powerLevel");
  fjage_msg_add_float(msg, "value", value);
  msg = request(usock, msg, TIMEOUT);
  if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_INFORM) {
    fjage_msg_destroy(msg);
    fjage_aid_destroy(phy);
    return 0;
  }
  fjage_msg_destroy(msg);
  fjage_aid_destroy(phy);
  return -1;
}

int unetsocket_ext_npulses(unetsocket_t sock, float *signal, int nsamples, int rate, int npulses, int pri) {
  if (sock == NULL) return -1;
  if (nsamples < 0) return -1;
  if (nsamples > 0 && signal == NULL) return -1;
  _unetsocket_t *usock = sock;
  fjage_msg_t msg;
  fjage_aid_t bb;
  int pulsedelay_cache = 0;
  int npulses_cache = 0;
  float signalduration = (float)((1000.0 / rate) * nsamples);
  int pulsedelay = (int)round(((float)pri - signalduration));
  if (pri < signalduration + 5)
  {
    error("Pulse delay is less than 5 ms...");
    return -1;
  }
  if ((unetsocket_ext_iget(usock, 0, "org.arl.unet.Services.BASEBAND", "npulses", &npulses_cache) < 0) || (unetsocket_ext_iset(usock, 0, "org.arl.unet.Services.BASEBAND", "npulses", npulses) < 0))
  {
    error("Unable to get/set npulses...");
    return -1;
  }
  if ((unetsocket_ext_iget(usock, 0, "org.arl.unet.Services.BASEBAND", "pulsedelay", &pulsedelay_cache) < 0) || (unetsocket_ext_iset(usock, 0, "org.arl.unet.Services.BASEBAND", "pulsedelay", pulsedelay) < 0))
  {
    unetsocket_ext_iset(usock, 0, "org.arl.unet.Services.BASEBAND", "npulses", npulses_cache);
    error("Unable to get/set pulse delay...");
    return -1;
  }
  bb = fjage_agent_for_service(usock->gw, "org.arl.unet.Services.BASEBAND");
  msg = fjage_msg_create("org.arl.unet.bb.TxBasebandSignalReq", FJAGE_REQUEST);
  fjage_msg_set_recipient(msg, bb);
  fjage_msg_add_float(msg, "fc", 0);
  if (signal != NULL) fjage_msg_add_float_array(msg, "signal", signal, nsamples);
  msg = request(usock, msg, 5 * TIMEOUT);
  if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_AGREE)
  {
    fjage_msg_destroy(msg);
    msg = receive(usock, "org.arl.unet.phy.TxFrameNtf", NULL, (pri*npulses)+(2*TIMEOUT));
    unetsocket_ext_iset(usock, 0, "org.arl.unet.Services.BASEBAND", "npulses", npulses_cache);
    unetsocket_ext_iset(usock, 0, "org.arl.unet.Services.BASEBAND", "pulsedelay", pulsedelay_cache);
    fjage_msg_destroy(msg);
    return 0;
  }
  fjage_msg_destroy(msg);
  unetsocket_ext_iset(usock, 0, "org.arl.unet.Services.BASEBAND", "npulses", npulses_cache);
  unetsocket_ext_iset(usock, 0, "org.arl.unet.Services.BASEBAND", "pulsedelay", pulsedelay_cache);
  return -1;
}

int unetsocket_ext_iset(unetsocket_t sock, int index, char *target_name, char *param_name, int value)
{
    if (sock == NULL) return -1;
    _unetsocket_t *usock = sock;
    fjage_msg_t msg;
    fjage_aid_t aid;
    aid = agent_for_service(usock, target_name);
    if (aid == NULL) aid = fjage_aid_create(target_name);
    msg = fjage_msg_create(parameterreq, FJAGE_REQUEST);
    fjage_msg_set_recipient(msg, aid);
    if (index == 0) index = -1;
    fjage_msg_add_int(msg, "index", index);
    fjage_msg_add_string(msg, "param", param_name);
    fjage_msg_add_int(msg, "value", value);
    msg = request(usock, msg, 5 * TIMEOUT);
    if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_INFORM)
    {
        fjage_msg_destroy(msg);
        fjage_aid_destroy(aid);
        return 0;
    }
    fjage_msg_destroy(msg);
    fjage_aid_destroy(aid);
    return -1;
}

int unetsocket_ext_fset(unetsocket_t sock, int index, char *target_name, char *param_name, float value)
{
    if (sock == NULL) return -1;
    _unetsocket_t *usock = sock;
    fjage_msg_t msg;
    fjage_aid_t aid;
    aid = agent_for_service(usock, target_name);
    if (aid == NULL) aid = fjage_aid_create(target_name);
    msg = fjage_msg_create(parameterreq, FJAGE_REQUEST);
    fjage_msg_set_recipient(msg, aid);
    if (index == 0) index = -1;
    fjage_msg_add_int(msg, "index", index);
    fjage_msg_add_string(msg, "param", param_name);
    fjage_msg_add_float(msg, "value", value);
    msg = request(usock, msg, 5 * TIMEOUT);
    if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_INFORM)
    {
        fjage_msg_destroy(msg);
        fjage_aid_destroy(aid);
        return 0;
    }
    fjage_msg_destroy(msg);
    fjage_aid_destroy(aid);
    return -1;
}

int unetsocket_ext_bset(unetsocket_t sock, int index, char *target_name, char *param_name, bool value)
{
    if (sock == NULL) return -1;
    _unetsocket_t *usock = sock;
    fjage_msg_t msg;
    fjage_aid_t aid;
    aid = agent_for_service(usock, target_name);
    if (aid == NULL) aid = fjage_aid_create(target_name);
    msg = fjage_msg_create(parameterreq, FJAGE_REQUEST);
    fjage_msg_set_recipient(msg, aid);
    if (index == 0) index = -1;
    fjage_msg_add_int(msg, "index", index);
    fjage_msg_add_string(msg, "param", param_name);
    fjage_msg_add_bool(msg, "value", value);
    msg = request(usock, msg, 5 * TIMEOUT);
    if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_INFORM)
    {
        fjage_msg_destroy(msg);
        fjage_aid_destroy(aid);
        return 0;
    }
    fjage_msg_destroy(msg);
    fjage_aid_destroy(aid);
    return -1;
}

int unetsocket_ext_sset(unetsocket_t sock, int index, char *target_name, char *param_name, char *value)
{
    if (sock == NULL) return -1;
    _unetsocket_t *usock = sock;
    fjage_msg_t msg;
    fjage_aid_t aid;
    aid = agent_for_service(usock, target_name);
    if (aid == NULL) aid = fjage_aid_create(target_name);
    msg = fjage_msg_create(parameterreq, FJAGE_REQUEST);
    fjage_msg_set_recipient(msg, aid);
    if (index == 0) index = -1;
    fjage_msg_add_int(msg, "index", index);
    fjage_msg_add_string(msg, "param", param_name);
    fjage_msg_add_string(msg, "value", value);
    msg = request(usock, msg, 5 * TIMEOUT);
    if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_INFORM)
    {
        fjage_msg_destroy(msg);
        fjage_aid_destroy(aid);
        return 0;
    }
    fjage_msg_destroy(msg);
    fjage_aid_destroy(aid);
    return -1;
}

int unetsocket_ext_iget(unetsocket_t sock, int index, char *target_name, char *param_name, int *value)
{
    if (sock == NULL) return -1;
    _unetsocket_t *usock = sock;
    fjage_msg_t msg;
    fjage_aid_t aid;
    aid = agent_for_service(usock, target_name);
    if (aid == NULL) aid = fjage_aid_create(target_name);
    msg = fjage_msg_create(parameterreq, FJAGE_REQUEST);
    fjage_msg_set_recipient(msg, aid);
    if (index == 0) index = -1;
    fjage_msg_add_int(msg, "index", index);
    fjage_msg_add_string(msg, "param", param_name);
    msg = request(usock, msg, 5 * TIMEOUT);
    if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_INFORM)
    {
        *value = fjage_msg_get_int(msg, "value", 0);
        fjage_msg_destroy(msg);
        fjage_aid_destroy(aid);
        return 0;
    }
    fjage_msg_destroy(msg);
    fjage_aid_destroy(aid);
    return -1;
}

int unetsocket_ext_fget(unetsocket_t sock, int index, char *target_name, char *param_name, float *value)
{
    if (sock == NULL) return -1;
    _unetsocket_t *usock = sock;
    fjage_msg_t msg;
    fjage_aid_t aid;
    aid = agent_for_service(usock, target_name);
    if (aid == NULL) aid = fjage_aid_create(target_name);
    msg = fjage_msg_create(parameterreq, FJAGE_REQUEST);
    fjage_msg_set_recipient(msg, aid);
    if (index == 0) index = -1;
    fjage_msg_add_int(msg, "index", index);
    fjage_msg_add_string(msg, "param", param_name);
    msg = request(usock, msg, 5 * TIMEOUT);
    if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_INFORM)
    {
        *value = fjage_msg_get_float(msg, "value", 0);
        fjage_msg_destroy(msg);
        fjage_aid_destroy(aid);
        return 0;
    }
    fjage_msg_destroy(msg);
    fjage_aid_destroy(aid);
    return -1;
}

int unetsocket_ext_bget(unetsocket_t sock, int index, char *target_name, char *param_name, bool *value)
{
    if (sock == NULL) return -1;
    _unetsocket_t *usock = sock;
    fjage_msg_t msg;
    fjage_aid_t aid;
    aid = agent_for_service(usock, target_name);
    if (aid == NULL) aid = fjage_aid_create(target_name);
    msg = fjage_msg_create(parameterreq, FJAGE_REQUEST);
    fjage_msg_set_recipient(msg, aid);
    if (index == 0) index = -1;
    fjage_msg_add_int(msg, "index", index);
    fjage_msg_add_string(msg, "param", param_name);
    msg = request(usock, msg, 5 * TIMEOUT);
    if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_INFORM)
    {
        *value = fjage_msg_get_bool(msg, "value", 0);
        fjage_msg_destroy(msg);
        fjage_aid_destroy(aid);
        return 0;
    }
    fjage_msg_destroy(msg);
    fjage_aid_destroy(aid);
    return -1;
}

int unetsocket_ext_sget(unetsocket_t sock, int index, char *target_name, char *param_name, char *buf, int buflen)
{
    if (sock == NULL) return -1;
    _unetsocket_t *usock = sock;
    fjage_msg_t msg;
    fjage_aid_t aid;
    aid = agent_for_service(usock, target_name);
    if (aid == NULL) aid = fjage_aid_create(target_name);
    msg = fjage_msg_create(parameterreq, FJAGE_REQUEST);
    fjage_msg_set_recipient(msg, aid);
    if (index == 0) index = -1;
    fjage_msg_add_int(msg, "index", index);
    fjage_msg_add_string(msg, "param", param_name);
    msg = request(usock, msg, 5 * TIMEOUT);
    if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_INFORM)
    {
        if (buf != NULL) strncpy(buf, fjage_msg_get_string(msg, "value"), (unsigned int)buflen);
        fjage_msg_destroy(msg);
        fjage_aid_destroy(aid);
        return 0;
    }
    fjage_msg_destroy(msg);
    fjage_aid_destroy(aid);
    return -1;
}

int unetsocket_ext_pbrecord(unetsocket_t sock, float *buf, int nsamples) {
  if (sock == NULL) return -1;
  if (nsamples <= 0 || buf == NULL) return -1;
  _unetsocket_t *usock = sock;
  int pbscnt = 0;
  float tempbuf[PBSBLK];
  pbscnt = (int)ceil((float)nsamples / PBSBLK);
  fjage_subscribe_agent(usock->gw, fjage_agent_for_service(usock->gw, "org.arl.unet.Services.BASEBAND"));
  printf("Requesting %d blocks of %d samples each...\n", pbscnt, PBSBLK);
  if (unetsocket_ext_iset(usock, 0, "org.arl.unet.Services.BASEBAND", "pbsblk", PBSBLK) < 0) return -1;
  if (unetsocket_ext_iset(usock, 0, "org.arl.unet.Services.BASEBAND", "pbscnt", pbscnt) < 0) return -1;
  for (int i = 0; i < pbscnt; i++)
  {
    fjage_msg_t rxsigntf = receive(usock, "org.arl.unet.bb.RxBasebandSignalNtf", NULL, 5 * TIMEOUT);
    if (rxsigntf == NULL) return -1;
    fjage_msg_get_float_array(rxsigntf, "signal", tempbuf, PBSBLK);
    fjage_msg_destroy(rxsigntf);
    unsigned long remaining = (unsigned long) (nsamples - (i * PBSBLK));
    memcpy(buf + (i * PBSBLK), tempbuf, remaining > PBSBLK ? (sizeof(float)*PBSBLK) : (sizeof(float)*remaining));
  }
  return 0;
}

int unetsocket_ext_tx_signal(unetsocket_t sock, float *signal, int nsamples, float fc, char *id) {
  if (sock == NULL) return -1;
  if (nsamples < 0) return -1;
  if (nsamples > 0 && signal == NULL) return -1;
  _unetsocket_t *usock = sock;
  fjage_msg_t msg;
  fjage_aid_t bb;
  bb = fjage_agent_for_service(usock->gw, "org.arl.unet.Services.BASEBAND");
  msg = fjage_msg_create("org.arl.unet.bb.TxBasebandSignalReq", FJAGE_REQUEST);
  fjage_msg_set_recipient(msg, bb);
  fjage_msg_add_float(msg, "fc", fc);
  fjage_msg_add_bool(msg, "signal__isComplex", fc != 0.0);
  if (signal != NULL) fjage_msg_add_float_array(msg, "signal", signal, ((int)fc ? 2 : 1)*nsamples);
  if (id != NULL) strcpy(id, fjage_msg_get_id(msg));
  msg = request(usock, msg, 5 * TIMEOUT);
  if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_AGREE)
  {
    fjage_msg_destroy(msg);
    return 0;
  }
  fjage_msg_destroy(msg);
  return -1;
}

int unetsocket_ext_bbrecord(unetsocket_t sock, float *buf, int nsamples) {
  if (sock == NULL) return -1;
  if (nsamples <= 0 || buf == NULL) return -1;
  _unetsocket_t *usock = sock;
  fjage_msg_t msg;
  fjage_aid_t bb;
  msg = fjage_msg_create("org.arl.unet.bb.RecordBasebandSignalReq", FJAGE_REQUEST);
  bb = fjage_agent_for_service(usock->gw, "org.arl.unet.Services.BASEBAND");
  fjage_msg_set_recipient(msg, bb);
  fjage_msg_add_int(msg, "recLength", nsamples);
  msg = request(usock, msg, 5 * TIMEOUT);
  if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_AGREE) {
    fjage_msg_destroy(msg);
    msg = receive(usock, "org.arl.unet.bb.RxBasebandSignalNtf", NULL, 20 * TIMEOUT);
    if (msg != NULL) {
      fjage_msg_get_float_array(msg, "signal", buf, 2 * nsamples);
      fjage_msg_destroy(msg);
      return 0;
    }
  }
  fjage_msg_destroy(msg);
  return -1;
}

#ifndef _WIN32

int unetsocket_ext_rs232_wakeup(char *devname, int baud, const char *settings)
{
  if (fjage_rs232_wakeup(devname, baud, settings) == -1)
  {
    return -1;
  }
  return 0;
}

int unetsocket_ext_ethernet_wakeup(unsigned char *macaddr)
{
  int i;
  unsigned char toSend[102];
  struct sockaddr_in udpClient, udpServer;
  int broadcast = 1;
  int udpSocket = socket(AF_INET, SOCK_DGRAM, 0);
  if (setsockopt(udpSocket, SOL_SOCKET, SO_BROADCAST, &broadcast, sizeof(broadcast)) == -1)
  {
    return -1;
  }
  udpClient.sin_family = AF_INET;
  udpClient.sin_addr.s_addr = INADDR_ANY;
  udpClient.sin_port = 0;
  bind(udpSocket, (struct sockaddr *)&udpClient, sizeof(udpClient));
  for (i = 0; i < 6; i++)
  {
    toSend[i] = 0xFF;
  }
  for (i = 1; i <= 16; i++)
  {
    memcpy(&toSend[i * 6], macaddr, 6 * sizeof(unsigned char));
  }
  udpServer.sin_family = AF_INET;
  udpServer.sin_addr.s_addr = inet_addr("255.255.255.255");
  udpServer.sin_port = htons(9);
  sendto(udpSocket, &toSend, sizeof(unsigned char) * 102, 0, (struct sockaddr *)&udpServer, sizeof(udpServer));
  return 0;
}

#endif

int unetsocket_ext_sleep(unetsocket_t sock)
{
    if (sock == NULL) return -1;
    _unetsocket_t *usock = sock;
    fjage_msg_t msg;
    fjage_aid_t scheduler;
    msg = fjage_msg_create("org.arl.unet.scheduler.AddScheduledSleepReq", FJAGE_REQUEST);
    scheduler = fjage_agent_for_service(usock->gw, "org.arl.unet.Services.SCHEDULER");
    fjage_msg_set_recipient(msg, scheduler);
    msg = request(usock, msg, 5 * TIMEOUT);
    if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_AGREE)
    {
        fjage_msg_destroy(msg);
        return 0;
    }
    fjage_msg_destroy(msg);
    return -1;
}
