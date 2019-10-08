from fjagepy import *
from fjagepy import Message as _Message
from fjagepy import MessageClass as _MessageClass
from fjagepy import Performative as _Performative
from fjagepy import Gateway
from fjagepy import AgentID as _AgentID
from types import MethodType as _mt
from warnings import warn as _warn
import threading as _td
import time as _time

#unet
ParameterReq           = _MessageClass('org.arl.unet.ParameterReq')
ParameterRsp           = _MessageClass('org.arl.unet.ParameterRsp')
TestReportNtf          = _MessageClass('org.arl.unet.TestReportNtf')
AbnormalTerminationNtf = _MessageClass('org.arl.unet.AbnormalTerminationNtf')
CapabilityListRsp      = _MessageClass('org.arl.unet.CapabilityListRsp')
CapabilityReq          = _MessageClass('org.arl.unet.CapabilityReq')
ClearReq               = _MessageClass('org.arl.unet.ClearReq')
DatagramCancelReq      = _MessageClass('org.arl.unet.DatagramCancelReq')
DatagramDeliveryNtf    = _MessageClass('org.arl.unet.DatagramDeliveryNtf')
DatagramFailureNtf     = _MessageClass('org.arl.unet.DatagramFailureNtf')
DatagramNtf            = _MessageClass('org.arl.unet.DatagramNtf')
DatagramProgressNtf    = _MessageClass('org.arl.unet.DatagramProgressNtf')
DatagramReq            = _MessageClass('org.arl.unet.DatagramReq')
ParamChangeNtf         = _MessageClass('org.arl.unet.ParamChangeNtf')

# net
DatagramTraceReq       = _MessageClass('org.arl.unet.net.DatagramTraceReq')
RouteDiscoveryReq      = _MessageClass('org.arl.unet.net.RouteDiscoveryReq')
RouteTraceReq          = _MessageClass('org.arl.unet.net.RouteTraceReq')
RouteDiscoveryNtf      = _MessageClass('org.arl.unet.net.RouteDiscoveryNtf')
RouteTraceNtf          = _MessageClass('org.arl.unet.net.RouteTraceNtf')

#phy
FecDecodeReq           = _MessageClass('org.arl.unet.phy.FecDecodeReq')
RxJanusFrameNtf        = _MessageClass('org.arl.unet.phy.RxJanusFrameNtf')
TxJanusFrameReq        = _MessageClass('org.arl.unet.phy.TxJanusFrameReq')
BadFrameNtf            = _MessageClass('org.arl.unet.phy.BadFrameNtf')
BadRangeNtf            = _MessageClass('org.arl.unet.phy.BadRangeNtf')
BeaconReq              = _MessageClass('org.arl.unet.phy.BeaconReq')
ClearSyncReq           = _MessageClass('org.arl.unet.phy.ClearSyncReq')
CollisionNtf           = _MessageClass('org.arl.unet.phy.CollisionNtf')
RangeNtf               = _MessageClass('org.arl.unet.phy.RangeNtf')
RangeReq               = _MessageClass('org.arl.unet.phy.RangeReq')
RxFrameNtf             = _MessageClass('org.arl.unet.phy.RxFrameNtf', DatagramNtf)
RxFrameStartNtf        = _MessageClass('org.arl.unet.phy.RxFrameStartNtf')
SyncInfoReq            = _MessageClass('org.arl.unet.phy.SyncInfoReq')
SyncInfoRsp            = _MessageClass('org.arl.unet.phy.SyncInfoRsp')
TxFrameNtf             = _MessageClass('org.arl.unet.phy.TxFrameNtf')
TxFrameReq             = _MessageClass('org.arl.unet.phy.TxFrameReq', DatagramReq)
TxFrameStartNtf        = _MessageClass('org.arl.unet.phy.TxFrameStartNtf')
TxRawFrameReq          = _MessageClass('org.arl.unet.phy.TxRawFrameReq')

#addr
AddressAllocReq        = _MessageClass('org.arl.unet.addr.AddressAllocReq')
AddressAllocRsp        = _MessageClass('org.arl.unet.addr.AddressAllocRsp')
AddressResolutionReq   = _MessageClass('org.arl.unet.addr.AddressResolutionReq')
AddressResolutionRsp   = _MessageClass('org.arl.unet.addr.AddressResolutionRsp')

#bb
BasebandSignal         = _MessageClass('org.arl.unet.bb.BasebandSignal')
RecordBasebandSignalReq = _MessageClass('org.arl.unet.bb.RecordBasebandSignalReq')
RxBasebandSignalNtf    = _MessageClass('org.arl.unet.bb.RxBasebandSignalNtf', BasebandSignal)
TxBasebandSignalReq    = _MessageClass('org.arl.unet.bb.TxBasebandSignalReq', BasebandSignal)

#mac
ReservationAcceptReq   = _MessageClass('org.arl.unet.mac.ReservationAcceptReq')
ReservationCancelReq   = _MessageClass('org.arl.unet.mac.ReservationCancelReq')
ReservationReq         = _MessageClass('org.arl.unet.mac.ReservationReq')
ReservationRsp         = _MessageClass('org.arl.unet.mac.ReservationRsp')
ReservationStatusNtf   = _MessageClass('org.arl.unet.mac.ReservationStatusNtf')
RxAckNtf               = _MessageClass('org.arl.unet.mac.RxAckNtf')
TxAckReq               = _MessageClass('org.arl.unet.mac.TxAckReq')

#remote
RemoteExecReq          = _MessageClass('org.arl.unet.remote.RemoteExecReq')
RemoteFailureNtf       = _MessageClass('org.arl.unet.remote.RemoteFailureNtf')
RemoteFileGetReq       = _MessageClass('org.arl.unet.remote.RemoteFileGetReq')
RemoteFileNtf          = _MessageClass('org.arl.unet.remote.RemoteFileNtf')
RemoteFilePutReq       = _MessageClass('org.arl.unet.remote.RemoteFilePutReq')
RemoteSuccessNtf       = _MessageClass('org.arl.unet.remote.RemoteSuccessNtf')
RemoteTextNtf          = _MessageClass('org.arl.unet.remote.RemoteTextNtf')
RemoteTextReq          = _MessageClass('org.arl.unet.remote.RemoteTextReq')

#scheduler
AddScheduledSleepReq   = _MessageClass('org.arl.unet.scheduler.AddScheduledSleepReq')
GetSleepScheduleReq    = _MessageClass('org.arl.unet.scheduler.GetSleepScheduleReq')
RemoveScheduledSleepReq = _MessageClass('org.arl.unet.scheduler.RemoveScheduledSleepReq')
SleepScheduleRsp       = _MessageClass('org.arl.unet.scheduler.SleepScheduleRsp')
WakeFromSleepNtf       = _MessageClass('org.arl.unet.scheduler.WakeFromSleepNtf')

#state
ClearStateReq          = _MessageClass('org.arl.unet.state.ClearStateReq')
SaveStateReq           = _MessageClass('org.arl.unet.state.SaveStateReq')

def _short(p):
    return p.split('.')[-1]


def _value(v):
    # TODO: support complex objects
    if isinstance(v, dict):
        if 'clazz' in v:
            if v['clazz'] == 'java.util.Date':
                return v['data']
            if v['clazz'] == 'java.util.ArrayList':
                return v['data']
            p = _GenericObject()
            p.__dict__.update(v)
            return p
        if 'data' in v:
            return v['data']
    return v


class _GenericObject:

    def __init__(self, **kwargs):
        self.__dict__.update(kwargs)

    def __repr__(self):
        return self.__dict__['clazz'] + '(...)'


class Services:
    """Services provided by agents.

    Agents can be looked up based on the services they provide.
    """
    NODE_INFO = 'org.arl.unet.Services.NODE_INFO'
    ADDRESS_RESOLUTION = 'org.arl.unet.Services.ADDRESS_RESOLUTION'
    DATAGRAM = 'org.arl.unet.Services.DATAGRAM'
    PHYSICAL = 'org.arl.unet.Services.PHYSICAL'
    RANGING = 'org.arl.unet.Services.RANGING'
    BASEBAND = 'org.arl.unet.Services.BASEBAND'
    LINK = 'org.arl.unet.Services.LINK'
    MAC = 'org.arl.unet.Services.MAC'
    ROUTING = 'org.arl.unet.Services.ROUTING'
    ROUTE_MAINTENANCE = 'org.arl.unet.Services.ROUTE_MAINTENANCE'
    TRANSPORT = 'org.arl.unet.Services.TRANSPORT'
    REMOTE = 'org.arl.unet.Services.REMOTE'
    STATE_MANAGER = 'org.arl.unet.Services.STATE_MANAGER'

class Topics:
    """Topics that can be subscribed to.
    """
    PARAMCHANGE = 0           # Topic for parameter change notification.
    ABNORMAL_TERMINATION = 1  # Topic for abnormal agent termination.

class Protocol:
    """Well-known protocol number assignments.
    """
    DATA = 0              # Protocol number for user application data.
    RANGING = 1           # Protocol number for use by ranging agents.
    LINK = 2              # Protocol number for use by link agents.
    REMOTE = 3            # Protocol number for use by remote management agents.
    MAC = 4               # Protocol number for use by MAC protocol agents.
    ROUTING = 5           # Protocol number for use by routing agents.
    TRANSPORT = 6         # Protocol number for use by transport agents.
    ROUTE_MAINTENANCE = 7  # Protocol number for use by route maintenance agents.
    LINK2 = 8             # Protocol number for use by secondary link agents.
    USER = 32             # Lowest protocol number allowable for user protocols.
    MAX = 63              # Largest protocol number allowable.

class ReservationStatus:
    """Status indicator for a particular request during the reservation process.
    """
    START = 0             # Start of channel reservation for a reservation request.
    END = 1               # End of channel reservation for a reservation request.
    FAILURE = 2           # Failure to reserve channel for a reservation request.
    CANCEL = 3            # Cancel channel reservation for a reservation request.
    REQUEST = 4           # Request information from a client agent for a reservation request.

class Address:
    """
    Defined constants for addressing.
    """
    BROADCAST = 0         # Broadcast address.

class _ParameterReq(ParameterReq):

    def __init__(self, index=-1, **kwargs):
        super().__init__()
        self.index = index
        self.requests = []
        self.perf = _Performative.REQUEST
        self.__dict__.update(kwargs)

    def get(self, param):
        self.requests.append({'param': param});
        return self

    def set(self, param, value):
        self.requests.append({'param': param, 'value': value});
        return self

    def __str__(self):
        p = ' '.join([_short(str(request['param'])) + ':' + (str(request['value']) if 'value' in request else '?') for request in self.requests])
        return self.__class__.__name__ + ':' + self.perf + '[' + (('index:' + str(self.index)) if self.index > 0 else '') + p.strip() + ']'

    def _repr_pretty_(self, p, cycle):
        p.text(str(self) if not cycle else '...')


class _ParameterRsp(ParameterRsp):

    def __init__(self, **kwargs):
        super().__init__()
        self.index = -1
        self.values = dict()
        self.perf = _Performative.REQUEST
        self.__dict__.update(kwargs)

    def get(self, param):
        if 'param' in self.__dict__ and self.param == param:
            return _value(self.value)
        if 'values' in self.__dict__ and param in self.values:
            return _value(self.values[param])
        if 'param' in self.__dict__ and _short(self.param) == param:
            return _value(self.value)
        if 'values' not in self.__dict__:
            return None
        for v in self.values:
            if _short(v) == param:
                return _value(self.values[v])
        return None

    def parameters(self):
        if 'values' in self.__dict__:
            p = self.values.copy()
        else:
            p = {}
        if 'param' in self.__dict__:
            p[self.param] = self.value
        for k in p:
            if isinstance(p[k], dict):
                p[k] = _value(p[k])
        return p

    def __str__(self):
        p = ''
        if 'param' in self.__dict__:
            p += _short(str(self.param)) + ':' + str(_value(self.value)) + ' '
        if 'values' in self.__dict__ and len(self.values) > 0:
            p += ' '.join([_short(str(v)) + ':' + str(_value(self.values[v])) for v in self.values])
        return self.__class__.__name__ + ':' + self.perf + '[' + (('index:' + str(self.index)) if self.index > 0 else '') + p.strip() + ']'

    def _repr_pretty_(self, p, cycle):
        p.text(str(self) if not cycle else '...')

def getter(self, param):
    rsp = self.request(_ParameterReq(index=self.index).get(param))
    if rsp is None:
        return None
    ursp = _ParameterRsp()
    ursp.__dict__.update(rsp.__dict__)
    if 'value' in list(ursp.__dict__.keys()):
        return ursp.get(param)
    else:
        return None

setattr(_AgentID, '__getattr__', getter)

def setter(self, param, value):
    if param in ['name', 'owner', 'is_topic', 'index']:
        self.__dict__[param] = value
        return value
    rsp = self.request(_ParameterReq(index=self.index).set(param, value))
    if rsp is None:
        _warn('Could not set parameter ' + param)
        return None
    ursp = _ParameterRsp()
    ursp.__dict__.update(rsp.__dict__)
    v = ursp.get(param)
    if v != value:
        _warn('Parameter ' + param + ' set to ' + str(v))
    return v

setattr(_AgentID, '__setattr__', setter)

def __getitem__(self, index):
    c = AgentID(self.name, owner=self.owner)
    c.index = index
    return c

setattr(_AgentID, '__getitem__', __getitem__)

def __str__(self):
    peer = self.owner.socket.getpeername()
    return self.name + ' on ' + peer[0] + ':' + str(peer[1])

setattr(_AgentID, '__str__', __str__)

def _repr_pretty_(self, p, cycle):
    if cycle:
        p.text('...')
        return
    rsp = self.request(_ParameterReq(index=self.index))
    if rsp is None:
        p.text(self.__str__())
        return
    ursp = _ParameterRsp()
    ursp.__dict__.update(rsp.__dict__)
    params = ursp.parameters()
    if 'title' in params:
        p.text('<<< ' + str(params['title']) + ' >>>\n')
    else:
        p.text('<<< ' + str(self.name).upper() + ' >>>\n')
    if 'description' in params:
        p.text('\n' + str(params['description']) + '\n')
    oprefix = ''
    for param in sorted(params):
        pp = param.split('.')
        prefix = '.'.join(pp[:-1]) if len(pp) > 1 else ''
        if prefix == '':
            continue
        if prefix != oprefix:
            oprefix = prefix
            p.text('\n[' + prefix + ']\n')
        p.text('  ' + pp[-1] + ' = ' + str(params[param]) + '\n')

setattr(_AgentID, '_repr_pretty_', _repr_pretty_)

class UnetSocket():
    """Unet socket for transmission/reception of datagrams.

    :param hostname: IP address of the modem
    :param port: Port number (1100 => automatic selection)

    >>> from unetpy import UnetSocket
    >>> sock = UnetSocket('192.168.1.10')
    """
    gw = None
    REQUEST_TIMEOUT = 1000
    NON_BLOCKING = 0
    BLOCKING = -1
    localProtocol = -1
    remoteAddress = -1
    remoteProtocol = 0
    timeout = -1
    provider = None
    waiting = None

    def __init__(self, hostname, port=1100):
        self.gw = Gateway(hostname, port)
        self.init()

    def init(self):
        """Creates a socket.
        """
        alist = self.gw.agentsForService(Services.DATAGRAM)
        for a in alist:
            self.gw.subscribe(self.gw.topic(a))

    def close(self):
        """Closes the socket. The socket functionality may not longer be accessed after
        this method is called.
        """
        self.gw.close()
        self.gw = None

    def isClosed(self):
        """Checks if a socket is closed.
        :returns: true if closed, false if open
        """
        return self.gw == None

    def bind(self, protocol):
        """Binds a socket to listen to a specific protocol datagrams.
        Protocol numbers between Protocol.DATA+1 to Protocol.USER-1 are reserved
        protocols and cannot be bound. Unbound sockets listen to all unreserved
        protocols.
        :param protocol: protocol number to listen for
        :returns: true on success, false on failure
        """
        if protocol == Protocol.DATA or (protocol >= Protocol.USER and protocol <= Protocol.MAX):
            self.localProtocol = protocol
            return True
        return False

    def unbind(self):
        """Unbinds a socket so that it listens to all unreserved protocols.
        Protocol numbers between Protocol.DATA+1 to Protocol.USER-1 are considered
        reserved.
        """
        self.localProtocol = -1

    def isBound(self):
        """Checks if a socket is bound.
        :returns: true if bound to a protocol, false if unbound
        """
        return self.localProtocol >= 0

    def connect(self, to, protocol):
        """Sets the default destination address and destination protocol number for
        datagrams sent using this socket. The defaults can be overridden for specific
        send() calls.
        The default protcol number when a socket is opened is Protcol.DATA. The default
        node address is undefined. Protocol numbers between Protocol.DATA+1 to
        Protocol.USER-1 are considered reserved, and cannot be used for sending datagrams
        using the socket.
        :param to: default destination node address
        :param protocol: default protocol number
        :returns: true on success, false on failure
        """
        if to >= 0 and (protocol == Protocol.DATA or (protocol >= Protocol.USER and protocol <= Protocol.MAX)):
            self.remoteAddress = to
            self.remoteProtocol = protocol
            return True
        return False

    def disconnnect(self):
        """Resets the default destination address to undefined, and the default protocol
        number to Protocol.DATA.
        """
        self.remoteAddress = -1
        self.remoteProtocol = 0

    def isConnected(self):
        """Checks if a socket is connected, i.e., has a default destination address and
        protocol number.
        :returns: True if connected, False otherwise
        """
        return self.remoteAddress >= 0

    def getLocalAddress(self):
        """Gets the local node address.
        :returns: local node address, or -1 on error
        """
        if self.gw == None:
            return -1
        nodeinfo = self.gw.agentForService(Services.NODE_INFO)
        if nodeinfo == None:
            return -1
        if nodeinfo.address != None:
            return nodeinfo.address
        else:
            return -1

    def getLocalProtocol(self):
        """Gets the protocol number that the socket is bound to.
        :returns: protocol number if socket is bound, -1 otherwise
        """
        return self.localProtocol

    def getRemoteAddress(self):
        """Gets the default destination node address for a connected socket.
        :returns: default destination node address if connected, -1 otherwise
        """
        return self.remoteAddress

    def getRemoteProtocol(self):
        """Gets the default transmission protocol number.
        :returns: default protocol number used to transmit a datagram
        """
        return self.remoteProtocol

    def setTimeout(self, ms):
        """Sets the timeout for datagram reception. The default timeout is infinite,
        i.e., the :func:`~receive()` call blocks forever. A timeout of 0 means the
        :func:`~receive()` call is non-blocking.
        :param ms: timeout in milliseconds, or -1 for infinite timeout
        """
        if ms < 0:
            ms = -1
        self.timeout = ms

    def getTimeout(self):
        """Gets the timeout for datagram reception.
        :retruns: timeout in milliseconds, 0 for non-blocking, or -1 for infinite
        """
        return self.timeout

    def send(self, data, to=None, protocol=None):
        """Transmits a datagram to the specified node address using the specified protocol.
           | Protocol numbers between Protocol.DATA+1 to Protocol.USER-1 are considered reserved,
           | and cannot be used for sending datagrams using the socket.
        :param data: bytes to transmit (or) datagram transmission request
        :param to: destination node address
        :param protocol: protocol number
        :returns: True on success, False on failure
        """
        if self.gw == None:
            return None
        if not isinstance(data, Message):
            req = DatagramReq()
            if isinstance(data, str):
                data = list(bytearray(data, encoding='utf-8'))
            elif isinstance(data, bytes):
                data = list(data)
            req.data = data
            if to is None and protocol is None:
                req.to = self.remoteAddress
                req.protocol = self.remoteProtocol
            elif to is not None and protocol is None:
                req.to = to
                req.protocol = self.remoteProtocol
            elif to is not None and protocol is not None:
                req.to = to
                req.protocol = protocol
                if protocol != Protocol.DATA and (protocol < Protocol.USER or protocol > Protocol.MAX):
                    return False
        else:
            req = data
            if isinstance(req.data, str):
                req.data = list(bytearray(req.data, encoding='utf-8'))
            elif isinstance(req.data, bytes):
                req.data = list(req.data)
        if req.recipient == None:
            if self.provider == None:
                self.provider = self.gw.agentForService(Services.TRANSPORT)
            if self.provider == None:
                self.provider = self.gw.agentForService(Services.ROUTING)
            if self.provider == None:
                self.provider = self.gw.agentForService(Services.LINK)
            if self.provider == None:
                self.provider = self.gw.agentForService(Services.PHYSICAL)
            if self.provider == None:
                self.provider = self.gw.agentForService(Services.DATAGRAM)
            if self.provider == None:
                return False
            req.recipient = self.provider
        rsp = self.gw.request(req, self.REQUEST_TIMEOUT)
        return (rsp != None and rsp.perf == _Performative.AGREE)

    def receive(self):
        """Receives a datagram sent to the local node and the bound protocol number. If the
        socket is unbound, then datagrams with all unreserved protocols are received. Any
        broadcast datagrams are also received.
           | This call blocks until a datagram is availbale, the socket timeout is reached,
           | or until :func:`~unetpy.UnetSocket.cancel()` is called.
        """
        try:
            if self.gw == None:
                return None
            self.waiting = _td.Event()
            deadline = -1
            if self.timeout == 0:
                deadline = 0
            elif self.timeout > 0:
                deadline = int(round(_time.time() * 1000)) + self.timeout
            ntf = None
            while not self.waiting.is_set():
                timeRemaining = -1
                if self.timeout == 0:
                    timeRemaining = 0
                elif self.timeout > 0:
                    timeRemaining = deadline - int(round(_time.time() * 1000))
                    if timeRemaining <= 0:
                        return None
                ntf = self.gw.receive(DatagramNtf, timeRemaining)
                if ntf == None:
                    return None
                if isinstance(ntf, DatagramNtf):
                    dg = ntf
                    p = dg.protocol
                    if (p == Protocol.DATA or p >= Protocol.USER):
                        if (self.localProtocol < 0):
                            return dg
                        if (self.localProtocol == p):
                            return dg
        except:
            self.waiting = None
        return None

    def cancel(self):
        """Cancels an ongoing blocking receive()."""
        if self.waiting == None:
            return
        self.waiting.set()

    def getGateway(self):
        """Gets a Gateway to provide low-level access to UnetStack."""
        return self.gw

    def agentForService(self, svc):
        """Gets an AgentID providing a specified service for low-level access to UnetStack."""
        if self.gw == None:
            return None
        return self.gw.agentForService(svc)

    def agent(self, name):
        """Gets a named AgentID for low-level access to UnetStack."""
        if self.gw == None:
            return None
        return self.gw.agent(name)

    def host(self, nodeName):
        """Resolve node name to node address.
        :param nodeName: name of the node to resolve
        :returns: address of the node, or null if unable to resolve
        """
        arp = self.agentForService(Services.ADDRESS_RESOLUTION)
        if arp == None:
            return None
        req = AddressResolutionReq()
        req.name = nodeName
        req.recipient = arp
        rsp = self.gw.request(req, self.REQUEST_TIMEOUT)
        if rsp == None:
            return None
        return  rsp.address