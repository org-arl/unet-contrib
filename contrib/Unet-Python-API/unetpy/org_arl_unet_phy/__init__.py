from fjagepy.org_arl_fjage import Message as _Message
from fjagepy.org_arl_fjage import Performative as _Performative

class Physical:
    CONTROL = 0
    DATA = 1

class TxFrameReq(_Message):

    def __init__(self, **kwargs):
        super().__init__()
        self.to = 0
        self.type = 1
        self.timestamped = None
        self.txtime = None
        self.perf = _Performative.REQUEST
        self.data = None
        self.__dict__.update(kwargs)

    def __str__(self):
        return self.__class__.__name__ + ":" + self.perf + "[type:" + str(self.type) + " to:" + str(self.to) + " protocol:" + " (" + (str(len(self.data)) if self.data else "0") + " bytes)]"

    def _repr_pretty_(self, p, cycle):
       p.text(str(self) if not cycle else '...')

class RangeReq(_Message):

    def __init__(self, **kwargs):
        super().__init__()
        self.to = 0
        self.channel = 0
        self.txBeacon = False
        self.reqBeacon = False
        self.perf = _Performative.REQUEST
        self.__dict__.update(kwargs)

    def __str__(self):
        return self.__class__.__name__ + ":" + self.perf + "[to:" + str(self.to) + " channel:" + str(self.channel) + " txBeacon:" + str(self.txBeacon) + " reqBeacon:" + str(self.reqBeacon) + "]"

    def _repr_pretty_(self, p, cycle):
       p.text(str(self) if not cycle else '...')

class RangeNtf(_Message):

    def __init__(self, **kwargs):
        super().__init__()
        self.to = None
        self.range = float('nan')
        self.isValid = True
        self.timeOffset = None
        self.perf = _Performative.INFORM
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

    def _repr_pretty_(self, p, cycle):
       p.text(str(self) if not cycle else '...')

class RxFrameNtf(_Message):

    def __init__(self, **kwargs):
        super().__init__()
        self.type = None
        self.timestamp = None
        self.is16BitTS = False
        self.rxTime = None
        self.perf = _Performative.INFORM
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

    def _repr_pretty_(self, p, cycle):
       p.text(str(self) if not cycle else '...')
