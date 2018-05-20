from fjagepy.org_arl_fjage import Message
from fjagepy.org_arl_fjage import Performative
from unetpy.org_arl_unet import DatagramNtf
from unetpy.org_arl_unet import DatagramReq


class Physical:
    CONTROL = 0
    DATA = 1


class TxFrameReq(Message):
    """ docstring for TxFrameReq. """

    def __init__(self, **kwargs):

        super(TxFrameReq, self).__init__()

        self.type = 1
        self.timestamped = None
        self.txtime = None
        self.perf = Performative.REQUEST
        self.data = None

        self.__dict__.update(kwargs)

    def __str__(self):
        return self.__class__.__name__ + ":" + self.perf + "[type:" + str(self.type) + " to:" + str(self.to) + " protocol:" + " (" + (str(len(self.data)) if self.data else "0") + " bytes)]"


class RangeReq(Message):
    """This class provides the basic attributes of messages and is typically
    extended by application-specific message classes. To ensure that messages
    can be sent between agents running on remote containers, all attributes
    of a message must be serializable.
    Attributes:
        to
        channel
        txBeacon
        reqBeacon
    NOTE: Message will not be send across without a recipient"""

    def __init__(self, **kwargs):

        super(RangeReq, self).__init__()

        self.to = None
        self.channel = 0
        self.txBeacon = False
        self.reqBeacon = False
        self.perf = Performative.REQUEST

        self.__dict__.update(kwargs)

    def __str__(self):
        return self.__class__.__name__ + ":" + self.perf + "[to:" + str(self.to) + " channel:" + str(self.channel) + " txBeacon:" + str(self.txBeacon) + " reqBeacon:" + str(self.reqBeacon) + "]"


class RangeNtf(Message):
    """docstring for RangeNtf."""

    def __init__(self, **kwargs):

        super(RangeNtf, self).__init__()

        self.to = None
        self.range = float('nan')
        self.isValid = True
        self.timeOffset = None
        self.perf = Performative.INFORM
        self.from_ = None

        # Hack to work around the fact that `from` is a reserved in Python
        if "from" in kwargs:
            kwargs["from_"] = kwargs["from"]
            del kwargs["from"]

        self.__dict__.update(kwargs)

    def __str__(self):
        try:
            val = int(self.range)
        except ValueError:
            return self.__class__.__name__ + ":" + self.perf + "[from:" + str(self.from_) + " woffset:" + str(self.timeOffset) + " Validity:" + str(self.isValid) + "]"
        if (self.timeOffset == None):
            return self.__class__.__name__ + ":" + self.perf + "[from:" + str(self.from_) + " to:" + str(self.to) + " range:" + str(self.range) + " validity:" + str(self.isValid) + "]"
        return self.__class__.__name__ + ":" + self.perf + "[from:" + str(self.from_) + " to:" + str(self.to) + " Range:" + str(self.range) + " offset:" + str(self.timeOffset) + " validity:" + str(self.isValid) + "]"


class RxFrameNtf(Message):
    """docstring for RxFrameNtf."""

    def __init__(self, **kwargs):

        super(DatagramNtf, self).__init__()

        self.type = None
        self.timestamp = None
        self.is16BitTS = False
        self.rxTime = None
        self.perf = Performative.INFORM

        # Hack to work around the fact that `from` is a reserved in Python
        if "from" in kwargs:
            kwargs["from_"] = kwargs["from"]
            del kwargs["from"]

        self.__dict__.update(kwargs)

    def setTxTime(self, time):
        self.timestamp = time

    def getTxTime(self):
        return self.timestamp

    def __str__(self):
        if self.type == Physical.CONTROL:
            typestr = "CONTROL"
        elif self.type == Physical.DATA:
            typestr = "DATA"

        return self.__class__.__name__ + ":" + self.perf + "[type:" + typestr + " from:" + str(self.from_) + " to:" + str(self.to) + " protocol:" + str(self.protocol) + " rxTime:" + str(self.rxTime) + ((" txTime:" + str(self.timestamp)) if self.timestamp else "") + " (" + (str(len(self.data)) if self.data else "0") + " bytes)]"
