/******************************************************************************
Copyright (c) 2018, Prasad Anjangi, Mandar Chitre
This file is released under Simplified BSD License.
Go to http://www.opensource.org/licenses/BSD-3-Clause for full license details.
******************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#include <math.h>
#include <pthread.h>
#include "fjage.h"
#include "unet.h"

#define PHYSICAL             "org.arl.unet.Services.PHYSICAL"
#define BASEBAND             "org.arl.unet.Services.BASEBAND"
#define RANGING              "org.arl.unet.Services.RANGING"
#define SHELL                "org.arl.fjage.shell.Services.SHELL"

typedef struct {
  fjage_gw_t gw;
  fjage_aid_t phy, baseband, ranging, shell;
  pthread_t tid;
  pthread_mutex_t rxlock, txlock;
  modem_rxcb_t rxcb;
  modem_txcb_t txcb;
  bool quit;
} _modem_t;

static void* monitor(void* p) {
  _modem_t* mm = p;
  while (!mm->quit) {
    pthread_mutex_lock(&mm->rxlock);
    fjage_msg_t msg = fjage_receive(mm->gw, NULL, NULL, 15000);
    pthread_mutex_unlock(&mm->rxlock);
    if (msg != NULL) {
      const char* clazz = fjage_msg_get_clazz(msg);
      if (!strcmp(clazz, "org.arl.unet.phy.RxFrameNtf") && mm->rxcb != NULL) {
        int from = fjage_msg_get_int(msg, "from", 0);
        int to = fjage_msg_get_int(msg, "to", 0);
        int type = fjage_msg_get_int(msg, "type", 0);
        long time = fjage_msg_get_long(msg, "rxTime", 0);
        int nbytes = fjage_msg_get_byte_array(msg, "data", NULL, 0);
        void* data = malloc(nbytes);
        if (data != NULL) {
          fjage_msg_get_byte_array(msg, "data", data, nbytes);
          (*(mm->rxcb))(from, to, type, data, nbytes, time);
          free(data);
        }
      }
      if (!strcmp(clazz, "org.arl.unet.phy.TxFrameNtf") && mm->txcb != NULL) {
        const char* id = fjage_msg_get_in_reply_to(msg);
        int type = fjage_msg_get_int(msg, "type", 0);
        long time = fjage_msg_get_long(msg, "txTime", 0);
        (*(mm->txcb))(id, type, time);
      }
      fjage_msg_destroy(msg);
    }
    pthread_mutex_lock(&mm->txlock);
    pthread_mutex_unlock(&mm->txlock);
  }
  return NULL;
}

static fjage_msg_t receive(_modem_t* mm, const char* clazz, const char* id, long timeout) {
  pthread_mutex_lock(&mm->txlock);
  fjage_interrupt(mm->gw);
  pthread_mutex_lock(&mm->rxlock);
  fjage_msg_t msg = fjage_receive(mm->gw, clazz, id, timeout);
  pthread_mutex_unlock(&mm->rxlock);
  pthread_mutex_unlock(&mm->txlock);
  return msg;
}

static fjage_msg_t request(_modem_t* mm, const fjage_msg_t request, long timeout) {
  pthread_mutex_lock(&mm->txlock);
  fjage_interrupt(mm->gw);
  pthread_mutex_lock(&mm->rxlock);
  fjage_msg_t msg = fjage_request(mm->gw, request, timeout);
  pthread_mutex_unlock(&mm->rxlock);
  pthread_mutex_unlock(&mm->txlock);
  return msg;
}

static fjage_aid_t agent_for_service(_modem_t* mm, const char* service) {
  pthread_mutex_lock(&mm->txlock);
  fjage_interrupt(mm->gw);
  pthread_mutex_lock(&mm->rxlock);
  fjage_aid_t aid = fjage_agent_for_service(mm->gw, service);
  pthread_mutex_unlock(&mm->rxlock);
  pthread_mutex_unlock(&mm->txlock);
  return aid;
}

modem_t modem_open_eth(char* ip_address, int port) {
  _modem_t* mm = malloc(sizeof(_modem_t));
  if (mm == NULL) return NULL;
  mm->gw = fjage_tcp_open(ip_address, port);
  if (mm->gw == NULL) {
    free(mm);
    return NULL;
  }
  mm->phy = fjage_agent_for_service(mm->gw, PHYSICAL);
  mm->baseband = fjage_agent_for_service(mm->gw, BASEBAND);
  mm->ranging = fjage_agent_for_service(mm->gw, RANGING);
  mm->shell = fjage_agent_for_service(mm->gw, SHELL);
  if (mm->phy != NULL) fjage_subscribe_agent(mm->gw, mm->phy);
  if (mm->ranging != NULL) fjage_subscribe_agent(mm->gw, mm->ranging);
  pthread_mutex_init(&mm->rxlock, NULL);
  pthread_mutex_init(&mm->txlock, NULL);
  if (pthread_create(&mm->tid, NULL, monitor, mm) < 0) {
    pthread_mutex_destroy(&mm->rxlock);
    pthread_mutex_destroy(&mm->txlock);
    fjage_close(mm->gw);
    free(mm);
    return NULL;
  }
  mm->quit = false;
  mm->rxcb = NULL;
  mm->txcb = NULL;
  return mm;
}

int modem_close(modem_t modem) {
  if (modem == NULL) return -1;
  _modem_t* mm = modem;
  mm->quit = true;
  fjage_interrupt(mm->gw);
  pthread_join(mm->tid, NULL);
  pthread_mutex_destroy(&mm->rxlock);
  pthread_mutex_destroy(&mm->txlock);
  fjage_aid_destroy(mm->phy);
  fjage_aid_destroy(mm->baseband);
  fjage_aid_destroy(mm->ranging);
  fjage_aid_destroy(mm->shell);
  fjage_close(mm->gw);
  free(mm);
  return 0;
}

int modem_tx_data(modem_t modem, int to, void* data, int nbytes, modem_packet_t type, char* id) {
  if (modem == NULL) return -1;
  if (nbytes < 0) return -1;
  if (nbytes > 0 && data == NULL) return -1;
  _modem_t* mm = modem;
  fjage_msg_t msg;
  if (type == CONTROL_FRAME || type == DATA_FRAME) {
    msg = fjage_msg_create("org.arl.unet.phy.TxFrameReq", FJAGE_REQUEST);
    fjage_msg_add_int(msg, "type", type);
  } else {
    msg = fjage_msg_create("org.arl.unet.DatagramReq", FJAGE_REQUEST);
  }
  fjage_msg_set_recipient(msg, mm->phy);
  fjage_msg_add_int(msg, "to", to);
  if (data != NULL) fjage_msg_add_byte_array(msg, "data", data, nbytes);
  if (id != NULL) strcpy(id, fjage_msg_get_id(msg));
  msg = request(mm, msg, 1000);
  if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_AGREE) {
    fjage_msg_destroy(msg);
    return 0;
  }
  return -1;
}

int modem_set_rx_callback(modem_t modem, modem_rxcb_t callback) {
  if (modem == NULL) return -1;
  _modem_t* mm = modem;
  mm->rxcb = callback;
  return 0;
}

int modem_set_tx_callback(modem_t modem, modem_txcb_t callback) {
  if (modem == NULL) return -1;
  _modem_t* mm = modem;
  mm->txcb = callback;
  return 0;
}

int modem_get_range(modem_t modem, int to, float* range) {
  if (modem == NULL) return -1;
  _modem_t* mm = modem;
  fjage_msg_t msg;
  msg = fjage_msg_create("org.arl.unet.phy.RangeReq", FJAGE_REQUEST);
  fjage_msg_set_recipient(msg, mm->ranging);
  fjage_msg_add_int(msg, "to", to);
  msg = request(mm, msg, 1000);
  if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_AGREE) {
    fjage_msg_destroy(msg);
    msg = receive(mm, "org.arl.unet.phy.RangeNtf", NULL, 10000);
    if (msg != NULL) {
      *range = fjage_msg_get_float(msg, "range", 0);
      fjage_msg_destroy(msg);
      return 0;
    }
  }
  return -1;
}


int modem_tx_signal(modem_t modem, float* signal, int nsamples, float fc, char* id) {
  if (modem == NULL) return -1;
  if (nsamples < 0) return -1;
  if (nsamples > 0 && signal == NULL) return -1;
  _modem_t* mm = modem;
  fjage_msg_t msg;
  msg = fjage_msg_create("org.arl.unet.bb.TxBasebandSignalReq", FJAGE_REQUEST);
  fjage_msg_set_recipient(msg, mm->baseband);
  if (fc >= 0) fjage_msg_add_float(msg, "fc", fc);
  if (signal != NULL) fjage_msg_add_float_array(msg, "signal", signal, (fc?2:1)*nsamples);
  if (id != NULL) strcpy(id, fjage_msg_get_id(msg));
  msg = request(mm, msg, 5000);
  if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_AGREE) {
    fjage_msg_destroy(msg);
    return 0;
  }
  return -1;
}

int modem_record(modem_t modem, float* buf, int nsamples) {
  if (modem == NULL) return -1;
  _modem_t* mm = modem;
  fjage_msg_t msg;
  msg = fjage_msg_create("org.arl.unet.bb.RecordBasebandSignalReq", FJAGE_REQUEST);
  fjage_msg_set_recipient(msg, mm->baseband);
  fjage_msg_add_int(msg, "recLen", nsamples);
  msg = request(mm, msg, 5000);
  if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_AGREE) {
    fjage_msg_destroy(msg);
    msg = receive(mm, "org.arl.unet.bb.RxBasebandSignalNtf", NULL, 5000);
    if (msg != NULL) {
      fjage_msg_get_float_array(msg, "data", buf, 2*nsamples);
      fjage_msg_destroy(msg);
      return 0;
    }
  }
  return -1;
}

int modem_iset(modem_t modem, int index, char* target_name ,char* param_name, int value) {
  if (modem == NULL) return -1;
  _modem_t* mm = modem;
  fjage_msg_t msg;
  fjage_aid_t aid;
  aid = agent_for_service(mm, target_name);
  if (aid == NULL) {
    aid = fjage_aid_create(target_name);
  }
  msg = fjage_msg_create("org.arl.unet.ParameterReq", FJAGE_REQUEST);
  fjage_msg_set_recipient(msg, aid);
  if (index==0) index = -1;
  fjage_msg_add_int(msg, "index", index);
  fjage_msg_add_string(msg, "param", param_name);
  fjage_msg_add_int(msg, "value", value);
  msg = request(mm, msg, 5000);
  if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_INFORM) {
    fjage_msg_destroy(msg);
    fjage_aid_destroy(aid);
    return 0;
  }
  fjage_aid_destroy(aid);
  return -1;
}

int modem_fset(modem_t modem, int index, char* target_name ,char* param_name, float value) {
  if (modem == NULL) return -1;
  _modem_t* mm = modem;
  fjage_msg_t msg;
  fjage_aid_t aid;
  aid = agent_for_service(mm, target_name);
  msg = fjage_msg_create("org.arl.unet.ParameterReq", FJAGE_REQUEST);
  fjage_msg_set_recipient(msg, aid);
  if (index==0) index = -1;
  fjage_msg_add_int(msg, "index", index);
  fjage_msg_add_string(msg, "param", param_name);
  fjage_msg_add_float(msg, "value", value);
  msg = request(mm, msg, 5000);
  if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_INFORM) {
    fjage_msg_destroy(msg);
    fjage_aid_destroy(aid);
    return 0;
  }
  fjage_aid_destroy(aid);
  return -1;
}

int modem_bset(modem_t modem, int index, char* target_name ,char* param_name, bool value) {
  if (modem == NULL) return -1;
  _modem_t* mm = modem;
  fjage_msg_t msg;
  fjage_aid_t aid;
  aid = agent_for_service(mm, target_name);
  if (aid == NULL) {
    aid = fjage_aid_create(target_name);
  }
  msg = fjage_msg_create("org.arl.unet.ParameterReq", FJAGE_REQUEST);
  fjage_msg_set_recipient(msg, aid);
  if (index==0) index = -1;
  fjage_msg_add_int(msg, "index", index);
  fjage_msg_add_string(msg, "param", param_name);
  fjage_msg_add_bool(msg, "value", value);
  msg = request(mm, msg, 5000);
  if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_INFORM) {
    fjage_msg_destroy(msg);
    fjage_aid_destroy(aid);
    return 0;
  }
  fjage_aid_destroy(aid);
  return -1;
}

int modem_sset(modem_t modem, int index, char* target_name, char* param_name, char* value) {
  if (modem == NULL) return -1;
  _modem_t* mm = modem;
  fjage_msg_t msg;
  fjage_aid_t aid;
  aid = agent_for_service(mm, target_name);
  msg = fjage_msg_create("org.arl.unet.ParameterReq", FJAGE_REQUEST);
  fjage_msg_set_recipient(msg, aid);
  if (index==0) index = -1;
  fjage_msg_add_int(msg, "index", index);
  fjage_msg_add_string(msg, "param", param_name);
  fjage_msg_add_string(msg, "value", value);
  msg = request(mm, msg, 5000);
  if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_INFORM) {
    fjage_msg_destroy(msg);
    fjage_aid_destroy(aid);
    return 0;
  }
  fjage_aid_destroy(aid);
  return -1;
}

int modem_iget(modem_t modem, int index, char* target_name, char* param_name, int* value) {
  if (modem == NULL) return -1;
  _modem_t* mm = modem;
  fjage_msg_t msg;
  fjage_aid_t aid;
  aid = agent_for_service(mm, target_name);
  if (aid == NULL) {
    aid = fjage_aid_create(target_name);
  }
  msg = fjage_msg_create("org.arl.unet.ParameterReq", FJAGE_REQUEST);
  fjage_msg_set_recipient(msg, aid);
  if (index==0) index = -1;
  fjage_msg_add_int(msg, "index", index);
  fjage_msg_add_string(msg, "param", param_name);
  msg = request(mm, msg, 5000);
  if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_INFORM) {
    *value = fjage_msg_get_int(msg, "value", 0);
    fjage_msg_destroy(msg);
    fjage_aid_destroy(aid);
    return 0;
  }
  fjage_aid_destroy(aid);
  return -1;
}

int modem_fget(modem_t modem, int index, char* target_name, char* param_name, float* value) {
  if (modem == NULL) return -1;
  _modem_t* mm = modem;
  fjage_msg_t msg;
  fjage_aid_t aid;
  aid = agent_for_service(mm, target_name);
  if (aid == NULL) {
    aid = fjage_aid_create(target_name);
  }
  msg = fjage_msg_create("org.arl.unet.ParameterReq", FJAGE_REQUEST);
  fjage_msg_set_recipient(msg, aid);
  if (index==0) index = -1;
  fjage_msg_add_int(msg, "index", index);
  fjage_msg_add_string(msg, "param", param_name);
  msg = request(mm, msg, 5000);
  if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_INFORM) {
    *value = fjage_msg_get_float(msg, "value", 0);
    fjage_msg_destroy(msg);
    fjage_aid_destroy(aid);
    return 0;
  }
  fjage_aid_destroy(aid);
  return -1;
}

int modem_bget(modem_t modem, int index, char* target_name, char* param_name, bool* value) {
  if (modem == NULL) return -1;
  _modem_t* mm = modem;
  fjage_msg_t msg;
  fjage_aid_t aid;
  aid = agent_for_service(mm, target_name);
  if (aid == NULL) {
    aid = fjage_aid_create(target_name);
  }
  msg = fjage_msg_create("org.arl.unet.ParameterReq", FJAGE_REQUEST);
  fjage_msg_set_recipient(msg, aid);
  if (index==0) index = -1;
  fjage_msg_add_int(msg, "index", index);
  fjage_msg_add_string(msg, "param", param_name);
  msg = request(mm, msg, 5000);
  if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_INFORM) {
    *value = fjage_msg_get_bool(msg, "value", 0);
    fjage_msg_destroy(msg);
    fjage_aid_destroy(aid);
    return 0;
  }
  fjage_aid_destroy(aid);
  return -1;
}

int modem_sget(modem_t modem, int index, char* target_name, char* param_name, char* buf, int buflen) {
  if (modem == NULL) return -1;
  _modem_t* mm = modem;
  fjage_msg_t msg;
  fjage_aid_t aid;
  aid = agent_for_service(mm, target_name);
  if (aid == NULL) {
    aid = fjage_aid_create(target_name);
  }
  msg = fjage_msg_create("org.arl.unet.ParameterReq", FJAGE_REQUEST);
  fjage_msg_set_recipient(msg, aid);
  if (index==0) index = -1;
  fjage_msg_add_int(msg, "index", index);
  fjage_msg_add_string(msg, "param", param_name);
  msg = request(mm, msg, 5000);
  if (msg != NULL && fjage_msg_get_performative(msg) == FJAGE_INFORM) {
    if (buf != NULL) strcpy(buf, fjage_msg_get_string(msg, "value"));
    fjage_msg_destroy(msg);
    fjage_aid_destroy(aid);
    return 0;
  }
  fjage_aid_destroy(aid);
  return -1;
}
