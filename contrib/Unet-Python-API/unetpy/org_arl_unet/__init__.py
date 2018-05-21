from fjagepy.org_arl_fjage import Message as _Message
from fjagepy.org_arl_fjage import Performative as _Performative
from fjagepy.org_arl_fjage import AgentID as _AgentID
from warnings import warn as _warn

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
            return v['clazz']+'(...)'
        if 'data' in v:
            return v['data']
    return v

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

class ParameterReq(_Message):

    def __init__(self, index=-1, *kwargs):
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

class ParameterRsp(_Message):

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
            p += _short(str(self.param))+':'+str(_value(self.value))+' '
        if 'values' in self.__dict__ and len(self.values) > 0:
            p += ' '.join([_short(str(v))+':'+str(_value(self.values[v])) for v in self.values])
        return self.__class__.__name__ + ':' + self.perf + '[' + (('index:' + str(self.index)) if self.index > 0 else '') + p.strip() + ']'

    def _repr_pretty_(self, p, cycle):
       p.text(str(self) if not cycle else '...')

class DatagramNtf(_Message):

    def __init__(self, **kwargs):
        super().__init__()
        self.perf = _Performative.INFORM
        self.data = list()
        self.from_ = None
        self.to = None
        self.protocol = None
        # Hack to work around the fact that `from` is a reserved in Python
        if 'from' in kwargs:
            kwargs['from_'] = kwargs['from']
            del kwargs['from']
        self.__dict__.update(kwargs)

    def __str__(self):
        return self.__class__.__name__ + ':' + self.perf + '[from:' + str(self.from_) + ' to:' + str(self.to) + ' protocol:' + str(self.protocol) + ' (' + (str(len(self.data)) if self.data else '0') + ' bytes)]'

    def _repr_pretty_(self, p, cycle):
       p.text(str(self) if not cycle else '...')

class DatagramReq(_Message):

    def __init__(self, **kwargs):
        super().__init__()
        self.perf = _Performative.REQUEST
        self.data = list()
        self.to = None
        self.protocol = None
        self.reliability = None
        self.ttl = None
        self.priority = None
        self.__dict__.update(kwargs)

    def __str__(self):
        return self.__class__.__name__ + ':' + self.perf + '[to:' + str(self.to) + ' protocol:' + str(self.protocol) + ' (' + (str(len(self.data)) if self.data else '0') + ' bytes)]'

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
        return rsp.get(param)

    def __setattr__(self, param, value):
        if param in ['name', 'gw', 'is_topic', 'index'] :
            self.__dict__[param] = value
            return value
        rsp = self.request(ParameterReq(index=self.index).set(param, value))
        if rsp is None:
            _warn('Could not set parameter '+param)
            return None
        v = rsp.get(param)
        if v != value:
            _warn('Parameter '+param+' set to '+str(v))
        return v

    def __getitem__(self, index):
        c = AgentID(self.gw, self.name)
        c.index = index
        return c

    def __str__(self):
        peer = self.gw.socket.getpeername()
        return self.name+' on '+peer[0]+':'+str(peer[1])

    def _repr_pretty_(self, p, cycle):
        if cycle:
            p.text('...')
            return
        rsp = self.request(ParameterReq(index=self.index))
        if rsp is None:
            p.text(self.__str__())
            return
        params = rsp.parameters()
        oprefix = ''
        for param in sorted(params):
            pp = param.split('.')
            prefix = '.'.join(pp[:-1]) if len(pp) > 1 else ''
            if prefix != oprefix:
                oprefix = prefix
                p.text('\n['+prefix+']\n')
            p.text('  '+pp[-1]+' = '+str(params[param])+'\n')
