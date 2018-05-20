from fjagepy.org_arl_fjage import Message
from fjagepy.org_arl_fjage import Performative


class RecordBasebandSignalReq(Message):
    """docstring for RecordBasebandSignalReq."""

    def __init__(self, **kwargs):

        super(RecordBasebandSignalReq, self).__init__()

        self.perf = Performative.REQUEST
        self.recTime = None
        self.recLen = None

        self.__dict__.update(kwargs)

    def __str__(self):
        return self.__class__.__name__ + ":" + self.perf + " (" + (str(self.recLen) if self.recLen else "maximum number of") + " baseband samples)"


class BasebandSignal(Message):
    """docstring for BasebandSignal."""

    def __init__(self, **kwargs):
        super(BasebandSignal, self).__init__()

        self.signal = None
        self.fc = 0
        self.fs = 0
        self.preamble = 0
        self.perf = Performative.INFORM

        self.__dict__.update(kwargs)

    def __str__(self):
        return self.__class__.__name__ + ":" + self.perf + (" (" + str(self.signal.length / 2) + " baseband samples)" if self.signal else "")


class RxBasebandSignalNtf(BasebandSignal):
    """docstring for RxBasebandSignalNtf."""

    def __init__(self, **kwargs):

        super(RxBasebandSignalNtf, self).__init__()

        self.perf = Performative.INFORM
        self.rxTime = None

        self.__dict__.update(kwargs)


class TxBasebandSignalReq(BasebandSignal):
    """ docstring for TxBasebandSignalReq. """

    def __init__(self, **kwargs):
        super(TxBasebandSignalReq, self).__init__()

        self.perf = Performative.REQUEST
        self.txTime = None
        self.dac = None
        self.fc = None
        self.fs = None
        self.channels = None

        self.__dict__.update(kwargs)
