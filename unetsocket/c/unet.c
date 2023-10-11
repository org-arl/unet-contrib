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

char* parameterreq = OLDPARAMETERREQ;
char* parameterrsp = OLDPARAMETERRSP;
char* rangereq = OLDRANGEREQ;
char* rangentf = OLDRANGENTF;

static int error(const char *msg)
{
  fprintf(stderr, "\n*** ERROR: %s\n\n", msg);
  return -1;
}

static long long _time_in_ms(void) {
  struct timeval tv;
  gettimeofday(&tv, NULL);
  return (((long long)tv.tv_sec)*1000)+(tv.tv_usec/1000);
}

static fjage_msg_t receive(_unetsocket_t *usock, const char *clazz, const char *id, long timeout) {
  fjage_interrupt(usock->gw);
  return fjage_receive(usock->gw, clazz, id, timeout);
}

static fjage_msg_t request(_unetsocket_t *usock, const fjage_msg_t request, long timeout) {
  fjage_interrupt(usock->gw);
  return fjage_request(usock->gw, request, timeout);
}

static fjage_aid_t agent_for_service(_unetsocket_t *usock, const char *service) {
  fjage_interrupt(usock->gw);
  return fjage_agent_for_service(usock->gw, service);
}

static int agents_for_service(_unetsocket_t *usock, const char *service, fjage_aid_t* agents, int max) {
  fjage_interrupt(usock->gw);
  return fjage_agents_for_service(usock->gw, service, agents, max);
}

unetsocket_t unetsocket_setup(_unetsocket_t *usock){
  pthread_mutex_init(&usock->rxlock, NULL);
  pthread_mutex_init(&usock->txlock, NULL);
  usock->local_protocol = -1;
  usock->remote_address = -1;
  usock->remote_protocol = 0;
  usock->timeout = -1;
  usock->provider = NULL;
  usock->quit = true;
  int nagents = agents_for_service(usock, "org.arl.unet.Services.DATAGRAM", NULL, 0);
  fjage_aid_t* agents = malloc((unsigned long)nagents*sizeof(fjage_aid_t));
  for (int i=0; i< nagents; i++) agents[i] = NULL;
  if (agents_for_service(usock, "org.arl.unet.Services.DATAGRAM", agents, nagents) < 0) {
    free(usock);
    free(agents);
    return NULL;
  }
  for(int i = 0; i < nagents; i++) fjage_subscribe_agent(usock->gw, agents[i]);
  free(agents);
  // check the parameter request class name
  fjage_msg_t msg;
  fjage_aid_t aid;
  aid = agent_for_service(usock, "org.arl.unet.Services.NODE_INFO");
  msg = fjage_msg_create(NEWPARAMETERREQ, FJAGE_REQUEST);
  fjage_msg_set_recipient(msg, aid);
  fjage_msg_add_int(msg, "index", -1);
  fjage_msg_add_string(msg, "param", "version");
  msg = request(usock, msg, 5 * TIMEOUT);
  if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_INFORM) {
    parameterreq = NEWPARAMETERREQ;
    parameterrsp = NEWPARAMETERRSP;
    rangereq = NEWRANGEREQ;
    rangentf = NEWRANGENTF;
  }
  fjage_msg_destroy(msg);
  fjage_aid_destroy(aid);
  return usock;
}


unetsocket_t unetsocket_open(const char* hostname, int port) {
  _unetsocket_t *usock = malloc(sizeof(_unetsocket_t));
  if (usock == NULL) return NULL;
  usock->gw = fjage_tcp_open(hostname, port);
  if (usock->gw == NULL) {
    free(usock);
    return NULL;
  }
  return unetsocket_setup(usock);
}

#ifndef _WIN32
unetsocket_t unetsocket_rs232_open(const char* devname, int baud, const char* settings) {
  _unetsocket_t *usock = malloc(sizeof(_unetsocket_t));
  if (usock == NULL) return NULL;
  usock->gw = fjage_rs232_open(devname, baud, settings);
  if (usock->gw == NULL) {
    free(usock);
    return NULL;
  }
  return unetsocket_setup(usock);
}
#endif

int unetsocket_close(unetsocket_t sock) {
  if (sock == NULL) return -1;
  _unetsocket_t *usock = sock;
  usock->quit = true;
  fjage_interrupt(usock->gw);
  fjage_close(usock->gw);
  free(usock);
  return 0;
}

int unetsocket_is_closed(unetsocket_t sock) {
  if (sock == NULL) return -1;
  return 0;
}

int unetsocket_bind(unetsocket_t sock, int protocol) {
  if (sock == NULL) return -1;
  _unetsocket_t *usock = sock;
  if (protocol == DATA || (protocol >= USER && protocol <= MAX)) {
    usock->local_protocol = protocol;
    return 0;
  }
  return -1;
}

void unetsocket_unbind(unetsocket_t sock) {
  _unetsocket_t *usock = sock;
  usock->local_protocol = -1;
}

int unetsocket_is_bound(unetsocket_t sock) {
  if (sock == NULL) return -1;
  _unetsocket_t *usock = sock;
  if (usock->local_protocol >= 0) return 0;
  return -1;
}

int unetsocket_connect(unetsocket_t sock, int to, int protocol) {
  if (sock == NULL) return -1;
  _unetsocket_t *usock = sock;
  if ((to >= 0) && (protocol == DATA || (protocol >= USER && protocol <= MAX))) {
    usock->remote_address = to;
  	usock->remote_protocol = protocol;
  	return 0;
  }
  return -1;
}

void unetsocket_disconnect(unetsocket_t sock) {
  _unetsocket_t *usock = sock;
  usock->remote_address = -1;
  usock->remote_protocol = 0;
}

int unetsocket_is_connected(unetsocket_t sock) {
  if (sock == NULL) return -1;
  _unetsocket_t *usock = sock;
  if (usock->remote_address >= 0) return 0;
  return -1;
}

int unetsocket_get_local_address(unetsocket_t sock) {
  if (sock == NULL) return -1;
  _unetsocket_t *usock = sock;
  fjage_msg_t msg;
  fjage_aid_t node;
  int rv;
  node = agent_for_service(usock, "org.arl.unet.Services.NODE_INFO");
  if (node == NULL) return -1;
  msg = fjage_msg_create(parameterreq, FJAGE_REQUEST);
  fjage_msg_set_recipient(msg, node);
  fjage_msg_add_int(msg, "index", -1);
  fjage_msg_add_string(msg, "param", "address");
  msg = request(usock, msg, 5*TIMEOUT);
  if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_INFORM) {
  	rv = fjage_msg_get_int(msg, "value", 0);
  	fjage_msg_destroy(msg);
  	free(node);
  	return rv;
  }
  fjage_msg_destroy(msg);
  fjage_aid_destroy(node);
  return -1;
}

int unetsocket_get_local_protocol(unetsocket_t sock) {
  if (sock == NULL) return -1;
  _unetsocket_t *usock = sock;
  return usock->local_protocol;
}

int unetsocket_get_remote_address(unetsocket_t sock) {
  if (sock == NULL) return -1;
  _unetsocket_t *usock = sock;
  return usock->remote_address;
}

int unetsocket_get_remote_protocol(unetsocket_t sock) {
  if (sock == NULL) return -1;
  _unetsocket_t *usock = sock;
  return usock->remote_protocol;
}

void unetsocket_set_timeout(unetsocket_t sock, long ms) {
  _unetsocket_t *usock = sock;
  if (ms < 0) {
  	ms = -1;
  }
  usock->timeout = ms;
}

long unetsocket_get_timeout(unetsocket_t sock) {
  if (sock == NULL) return -1;
  _unetsocket_t *usock = sock;
  return usock->timeout;
}

// NOTE: changed const uint8_t* to uint8_t*
int unetsocket_send(unetsocket_t sock, uint8_t* data, int len, int to, int protocol) {
  if (sock == NULL) return -1;
  _unetsocket_t *usock = sock;
  fjage_msg_t msg;
  int rv;
  msg = fjage_msg_create("org.arl.unet.DatagramReq", FJAGE_REQUEST);
  if (data != NULL) fjage_msg_add_byte_array(msg, "data", data, len);
  fjage_msg_add_int(msg, "to", to);
  fjage_msg_add_int(msg, "protocol", protocol);
  rv = unetsocket_send_request(usock, msg);
  return rv;
}

int unetsocket_send_reliable(unetsocket_t sock, uint8_t* data, int len, int to, int protocol) {
  if (sock == NULL) return -1;
  if (to == 0) return -1; // broadcast with reliability is not allowed
  _unetsocket_t *usock = sock;
  fjage_msg_t msg;
  int rv;
  msg = fjage_msg_create("org.arl.unet.DatagramReq", FJAGE_REQUEST);
  if (data != NULL) fjage_msg_add_byte_array(msg, "data", data, len);
  fjage_msg_add_int(msg, "to", to);
  fjage_msg_add_int(msg, "protocol", protocol);
  fjage_msg_add_bool(msg, "reliability", true);
  rv = unetsocket_send_request(usock, msg);
  return rv;
}

int unetsocket_send_request(unetsocket_t sock, fjage_msg_t req) {
  if (sock == NULL) return -1;
  _unetsocket_t *usock = sock;
  int protocol;
  const char* recipient;
  protocol = fjage_msg_get_int(req, "protocol", 0);
  if (protocol != DATA && (protocol < USER || protocol > MAX)) return -1;
  recipient = fjage_msg_get_string(req, "recipient");
  if (recipient == NULL) {
  	if (usock->provider == NULL) usock->provider = agent_for_service(usock, "org.arl.unet.Services.TRANSPORT");
  	if (usock->provider == NULL) usock->provider = agent_for_service(usock, "org.arl.unet.Services.ROUTING");
  	if (usock->provider == NULL) usock->provider = agent_for_service(usock, "org.arl.unet.Services.LINK");
  	if (usock->provider == NULL) usock->provider = agent_for_service(usock, "org.arl.unet.Services.PHYSICAL");
  	if (usock->provider == NULL) usock->provider = agent_for_service(usock, "org.arl.unet.Services.DATAGRAM");
  	if (usock->provider == NULL) return -1;
  	fjage_msg_set_recipient(req, usock->provider);
  }
  req = request(usock, req, TIMEOUT);
  if (req != NULL && fjage_msg_get_performative(req) == FJAGE_AGREE) {
  	fjage_msg_destroy(req);
  	return 0;
  }
  fjage_msg_destroy(req);
     return -1;
}

fjage_msg_t unetsocket_receive(unetsocket_t sock) {
  if (sock == NULL) return NULL;
  _unetsocket_t *usock = sock;
  long deadline = _time_in_ms() + usock->timeout;
  // TODO make this list more exhaustive
  const char *list[] = {"org.arl.unet.DatagramNtf", "org.arl.unet.phy.RxFrameNtf"};
  usock->quit = false;
  while (!usock->quit) {
    long time_remaining = 0;
    if (usock->timeout < 0) time_remaining = 15*TIMEOUT;
  	else if (usock->timeout > 0) {
  	  time_remaining = deadline - _time_in_ms();
  	  if (time_remaining < 0) return NULL;
  	}
  	fjage_msg_t msg = fjage_receive_any(usock->gw, list, 2, time_remaining);
  	if (msg != NULL) {
  	  int rv = fjage_msg_get_int(msg, "protocol", 0);
  	  if ((rv == DATA || rv >= USER) && (usock->local_protocol < 0 || usock->local_protocol == rv)) return msg;
  	}
    if (usock->timeout == 0) return NULL;
  }
  usock->quit = false;
  return NULL;
}

void unetsocket_cancel(unetsocket_t sock) {
  _unetsocket_t *usock = sock;
  usock->quit = true;
}

fjage_gw_t unetsocket_get_gateway(unetsocket_t sock) {
  if (sock == NULL) return NULL;
  _unetsocket_t *usock = sock;
  return usock->gw;
}

fjage_aid_t unetsocket_agent_for_service(unetsocket_t sock, const char* svc) {
  if (sock == NULL) return NULL;
  _unetsocket_t *usock = sock;
  return agent_for_service(usock, svc);
}

int unetsocket_agents_for_service(unetsocket_t sock, const char* svc, fjage_aid_t* agents, int max) {
  if (sock == NULL) return -1;
  _unetsocket_t *usock = sock;
  return agents_for_service(usock, svc, agents, max);
}

// NOTE: removed the first argument
fjage_aid_t unetsocket_agent(const char* name) {
  return fjage_aid_create(name);
}

int unetsocket_host(unetsocket_t sock, const char* node_name) {
  if (sock == NULL) return -1;
  _unetsocket_t *usock = sock;
  fjage_msg_t msg;
  fjage_aid_t arp;
  int rv;
  arp = agent_for_service(usock, "org.arl.unet.Services.ADDRESS_RESOLUTION");
  if (arp == NULL) return -1;
  msg = fjage_msg_create("org.arl.unet.addr.AddressResolutionReq", FJAGE_REQUEST);
  fjage_msg_set_recipient(msg, arp);
  fjage_msg_add_int(msg, "index", -1);
  fjage_msg_add_string(msg, "param", "name");
  fjage_msg_add_string(msg, "value", node_name);
  msg = request(usock, msg, 5*TIMEOUT);
  if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_INFORM) {
  	rv = fjage_msg_get_int(msg, "address", 0);
  	fjage_msg_destroy(msg);
  	fjage_aid_destroy(arp);
  	return rv;
  }
  fjage_msg_destroy(msg);
  return -1;
}
