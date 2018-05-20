from fjagepy.org_arl_fjage import Message


class Services:
    NODE_INFO = "NODE_INFO"
    ADDRESS_RESOLUTION = "ADDRESS_RESOLUTION"
    DATAGRAM = "DATAGRAM"
    PHYSICAL = "PHYSICAL"
    RANGING = "RANGING"
    BASEBAND = "BASEBAND"
    LINK = "LINK"
    MAC = "MAC"
    ROUTING = "ROUTING"
    ROUTE_MAINTENANCE = "ROUTE_MAINTENANCE"
    TRANSPORT = "TRANSPORT"
    REMOTE = "REMOTE"
    STATE_MANAGER = "STATE_MANAGER"


class ParameterReq(Message):
    """This class provides the basic attributes of messages and is typically
    extended by application-specific message classes. To ensure that messages
    can be sent between agents running on remote containers, all attributes
    of a message must be serializable.
    Attributes:
        index
        request
    NOTE: Message will not be send across without a recipient"""

    def __init__(self, **kwargs):

        super(ParameterReq, self).__init__()

        self.index = -1
        self.requests = []
        self.perf = Performative.REQUEST

        self.__dict__.update(kwargs)

    def get(self, param):
        self.requests.append({'param': param});

    def set(self, param, value):
        self.requests.append({'param': param, 'value': value});

    def __str__(self):
        return self.__class__.__name__ + ":" + self.perf + '[ ' + (('index:' + str(self.index)) if self.index > 0 else '') + ' ' + ' '.join([str(request['param']) + ':' + (str(request['value']) if 'value' in request else '?') for request in self.requests]) + ' ]'


class ParameterRsp(Message):
    def __init__(self, **kwargs):

        super(ParameterRsp, self).__init__()

        self.index = -1
        self.values = dict()
        self.perf = Performative.REQUEST

        self.__dict__.update(kwargs)

    def get(self, param):
        self.requests.append({'param': param});

    def set(self, param, value):
        self.requests.append({'param': param, 'value': value});

    def __str__(self):
        return self.__class__.__name__ + ":" + self.perf + '[ ' + (('index:' + str(self.index)) if self.index > 0 else '') + ' ' + ' '.join([str(param) + ':' + str(value) for param, value in self.values.iteritems()]) + ' ]'


class DatagramNtf(Message):
    """docstring for DatagramNtf."""

    def __init__(self, **kwargs):

        super(DatagramNtf, self).__init__()

        self.perf = Performative.INFORM
        self.data = list()
        self.from_ = None
        self.to = None
        self.protocol = None

        # Hack to work around the fact that `from` is a reserved in Python
        if "from" in kwargs:
            kwargs["from_"] = kwargs["from"]
            del kwargs["from"]

        self.__dict__.update(kwargs)

    def __str__(self):
        return self.__class__.__name__ + ":" + self.perf + "[from:" + str(self.from_) + " to:" + str(self.to) + " protocol:" + str(self.protocol) + " (" + (str(len(self.data)) if self.data else "0") + " bytes)]"


class DatagramReq(Message):
    """ docstring for DatagramReq. """

    def __init__(self, **kwargs):

        super(DatagramNtf, self).__init__()

        self.perf = Performative.REQUEST
        self.data = list()
        self.to = None
        self.protocol = None
        self.reliability = None
        self.ttl = None
        self.priority = None

        self.__dict__.update(kwargs)

    def __str__(self):
        return self.__class__.__name__ + ":" + self.perf + "[ to:" + str(self.to) + " protocol:" + str(self.protocol) + " (" + (str(len(self.data)) if self.data else "0") + " bytes)]"
