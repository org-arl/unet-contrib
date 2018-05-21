from fjagepy.org_arl_fjage import Message as _Message
from fjagepy.org_arl_fjage import Performative as _Performative
import numpy as _np

class RecordBasebandSignalReq(_Message):

    def __init__(self, **kwargs):
        super().__init__()
        self.perf = _Performative.REQUEST
        self.recTime = None
        self.recLen = None
        self.__dict__.update(kwargs)

    def __str__(self):
        return self.__class__.__name__ + ':' + self.perf + '[(' + (str(self.recLen) if self.recLen else 'maximum number of') + ' baseband samples)]'

    def _repr_pretty_(self, p, cycle):
       p.text(str(self) if not cycle else '...')

class BasebandSignal(_Message):

    def __init__(self, **kwargs):
        super().__init__()
        self.fc = 0
        self.fs = 0
        self.preamble = 0
        self.perf = _Performative.INFORM
        self.__dict__.update(kwargs)

    @property
    def signal(self):
        if 'signal' not in self.__dict__:
            return None
        if self.fc == 0:
            return _np.array(self.__dict__['signal'], dtype=_np.float)
        else:
            x = _np.array(self.__dict__['signal'], dtype=_np.complex)
            x = x[::2] + 1j*x[1::2]
            return x

    @signal.setter
    def signal(self, x):
        if isinstance(x, _np.ndarray):
            if _np.any(_np.iscomplex(x)):
                x = np.ravel(np.array([x.real, x.imag]).T)
                self.__dict__['signal'] = list(x)
                return
        self.__dict__['signal'] = list(x)

    def __str__(self):
        s = self.__class__.__name__ + ':' + self.perf
        sig = self.signal
        if sig is not None:
            s += '[(' + str(len(sig)) + (' baseband' if self.fc > 0 else '') + ' samples)]'
        return s

    def _repr_pretty_(self, p, cycle):
       p.text(str(self) if not cycle else '...')

class RxBasebandSignalNtf(BasebandSignal):

    def __init__(self, **kwargs):
        super().__init__()
        self.perf = _Performative.INFORM
        self.rxTime = None
        self.__dict__.update(kwargs)

class TxBasebandSignalReq(BasebandSignal):

    def __init__(self, **kwargs):
        super().__init__()
        self.perf = _Performative.REQUEST
        self.txTime = None
        self.dac = None
        self.fc = None
        self.fs = None
        self.channels = None
        self.__dict__.update(kwargs)
