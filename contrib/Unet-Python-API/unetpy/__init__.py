from fjagepy import *
from fjagepy import Message as _Message
from fjagepy import MessageClass as _MessageClass
from fjagepy import Performative as _Performative
from fjagepy import Gateway as _Gateway
from fjagepy import AgentID as _AgentID
from types import MethodType as _mt
from warnings import warn as _warn

_ParameterReq = _MessageClass('org.arl.unet.ParameterReq')
_ParameterRsp = _MessageClass('org.arl.unet.ParameterRsp')
_DatagramReq = _MessageClass('org.arl.unet.DatagramReq')


def _repr_pretty_(self, p, cycle):
    if cycle:
        p.text('...')
    elif self.perf is not None:
        p.text(self.perf)


setattr(_Message, '_repr_pretty_', _repr_pretty_)


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
    USER = 32             # Lowest protocol number allowable for user protocols.
    MAX = 63              # Largest protocol number allowable.


class ParameterReq(_ParameterReq):

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


class ParameterRsp(_ParameterRsp):

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


class AgentID(_AgentID):

    def __init__(self, gw, name, is_topic=False):
        self.is_topic = is_topic
        self.name = name
        self.index = -1
        self.gw = gw

    def send(self, msg):
        msg.recipient = self.name
        return self.gw.send(msg)

    def request(self, msg, timeout=1000):
        msg.recipient = self.name
        return self.gw.request(msg, timeout)

    def __lshift__(self, msg):
        return self.request(msg)

    def __getattr__(self, param):
        rsp = self.request(ParameterReq(index=self.index).get(param))
        if rsp is None:
            return None
        ursp = ParameterRsp()
        ursp.__dict__.update(rsp.__dict__)
        if 'value' in list(ursp.__dict__.keys()):
            return ursp.get(param)
        else:
            return None

    def __setattr__(self, param, value):
        if param in ['name', 'gw', 'is_topic', 'index']:
            self.__dict__[param] = value
            return value
        rsp = self.request(ParameterReq(index=self.index).set(param, value))
        if rsp is None:
            _warn('Could not set parameter ' + param)
            return None
        ursp = ParameterRsp()
        ursp.__dict__.update(rsp.__dict__)
        v = ursp.get(param)
        if v != value:
            _warn('Parameter ' + param + ' set to ' + str(v))
        return v

    def __getitem__(self, index):
        c = AgentID(self.gw, self.name)
        c.index = index
        return c

    def __str__(self):
        peer = self.gw.socket.getpeername()
        return self.name + ' on ' + peer[0] + ':' + str(peer[1])

    def _repr_pretty_(self, p, cycle):
        if cycle:
            p.text('...')
            return
        rsp = self.request(ParameterReq(index=self.index))
        if rsp is None:
            p.text(self.__str__())
            return
        ursp = ParameterRsp()
        ursp.__dict__.update(rsp.__dict__)
        params = ursp.parameters()
        oprefix = ''
        for param in sorted(params):
            pp = param.split('.')
            prefix = '.'.join(pp[:-1]) if len(pp) > 1 else ''
            if prefix != oprefix:
                oprefix = prefix
                p.text('\n[' + prefix + ']\n')
            p.text('  ' + pp[-1] + ' = ' + str(params[param]) + '\n')


class UnetGateway(_Gateway):

    def __init__(self, hostname, port=1100, name=None):
        super().__init__(hostname, port, name)

    def topic(self, name):
        a = super().topic(name)
        return AgentID(self, a.name, a.is_topic)

    def agent(self, name):
        return AgentID(self, name)

    def agentForService(self, service):
        a = super().agentForService(service)
        if a is not None:
            if isinstance(a, str):
                a = AgentID(self, a)
            else:
                a = AgentID(self, a.name, a.is_topic)
        return a

    def agentsForService(self, service):
        a = super().agentsForService(service)
        if a is not None:
            for j in range(len(a)):
                if isinstance(a[j], str):
                    a[j] = AgentID(self, a[j])
                else:
                    a[j] = AgentID(self, a[j].name)
        return a

    def agent(self, name):
        return AgentID(self, name)

    def socket(self):
        gw = self

        class Socket:
            """Unet socket for transmission/reception of datagrams.
            """
            REQUEST_TIMEOUT = 1000
            localProtocol = -1
            remoteAddress = -1
            remoteProtocol = 0
            timeout = -1
            provider = None
            waiting = None

            def __init__(self):
                """Creates a socket.
                """
                alist = gw.agentsForService(Services.DATAGRAM)
                for a in alist:
                    gw.subscribe(gw.topic(a))

            def close(self):
                """Closes the socket. The socket functionality may not longer be accessed after
                   this method is called.
                """
                gw.shutdown()
                gw = None

            def isClosed(self):
                """Checks if a socket is closed.

                :returns: true if closed, false if open
                """
                return gw == None

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
                if gw == None:
                    return -1
                nodeinfo = gw.agentForService(Services.NODE_INFO)
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

            def send(self, req=None):
                """Transmits a datagram to the specified node address using the specified protocol.

                   | Protocol numbers between Protocol.DATA+1 to Protocol.USER-1 are considered reserved,
                   | and cannot be used for sending datagrams using the socket.

                :param req: datagram transmission request
                :returns: True on success, False on failure
                """
                if gw == None:
                    return False
                protocol = req.protocol
                if protocol != Protocol.DATA and (protocol < Protocol.USER or protocol > Protocol.MAX):
                    return False
                if req.recipient == None:
                    if provider == None:
                        provider = gw.agentForService(Services.TRANSPORT)
                    if provider == None:
                        provider = gw.agentForService(Services.ROUTING)
                    if provider == None:
                        provider = gw.agentForService(Services.LINK)
                    if provider == None:
                        provider = gw.agentForService(Services.PHYSICAL)
                    if provider == None:
                        provider = gw.agentForService(Services.DATAGRAM)
                    if provider == None:
                        return False
                    req.recipient = provider
                rsp = gw.request(req, self.REQUEST_TIMEOUT)
                return (rsp != None and rsp.perf == _Performative.AGREE)
