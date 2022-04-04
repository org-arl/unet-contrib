from fjagepy import *
from fjagepy import Message as _Message
from fjagepy import MessageClass as _MessageClass
from fjagepy import Performative as _Performative
from fjagepy import Services as _Services
from fjagepy import Gateway
from fjagepy import AgentID as _AgentID
from types import MethodType as _mt
from warnings import warn as _warn
import threading as _td
import time as _time

#unet
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
RefuseRsp              = _MessageClass('org.arl.unet.RefuseRsp')
FailureNtf             = _MessageClass('org.arl.unet.FailureNtf')

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

#link
LinkStatusNtf          = _MessageClass('org.arl.unet.link.LinkStatusNtf')

#localization
RangeNtf               = _MessageClass('org.arl.unet.localization.RangeNtf')
RangeReq               = _MessageClass('org.arl.unet.localization.RangeReq')
BeaconReq              = _MessageClass('org.arl.unet.localization.BeaconReq')
RespondReq             = _MessageClass('org.arl.unet.localization.RespondReq')
InterrogationNtf       = _MessageClass('org.arl.unet.localization.InterrogationNtf')


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


class Services(_Services):
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
    DEVICE_INFO = 'org.arl.unet.Services.DEVICE_INFO'
    DOA = 'org.arl.unet.Services.DOA'

class Topics:
    """Topics that can be subscribed to.
    """
    PARAMCHANGE = 'org.arl.unet.Topics.PARAMCHANGE'  # Topic for parameter change notification.
    LIFECYCLE = 'org.arl.unet.Topics.LIFECYCLE'      # Topic for abnormal agent termination.

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

    def disconnect(self):
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
                if self.remoteAddress < 0:
                    return False
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
        if self.gw == None:
            return None
        t0 = _time.time() * 1000
        while (self.timeout <= 0 or ((_time.time() * 1000) - t0) < self.timeout):
            self.waiting = True
            ntf = self.gw.receive(DatagramNtf, self.timeout)
            self.waiting = False
            if ntf == None:
                return None
            if isinstance(ntf, DatagramNtf):
                p = ntf.protocol
                if ((p == Protocol.DATA) or (p >= Protocol.USER)):
                    if ((self.localProtocol < 0) or (self.localProtocol == p)):
                        return ntf
        return None

    def cancel(self):
        """Cancels an ongoing blocking receive()."""
        if self.waiting:
            self.gw.cancel = True
            self.gw.cv.acquire()
            self.gw.cv.notify()
            self.gw.cv.release()

    def getGateway(self):
        """Gets a Gateway to provide low-level access to UnetStack."""
        return self.gw

    def agentForService(self, svc):
        """Gets an AgentID providing a specified service for low-level access to UnetStack."""
        if self.gw == None:
            return None
        return self.gw.agentForService(svc)

    def agentsForService(self, svc):
        """Find all agents providing a specified service for low-level access to UnetStack."""
        if self.gw == None:
            return None
        return self.gw.agentsForService(svc)

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
