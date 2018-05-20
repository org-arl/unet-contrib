from unetpy.org_arl_unet import Services, ParameterReq, ParameterRsp, DatagramNtf, DatagramReq, UnetAgentID
from unetpy.org_arl_unet_phy import Physical, RangeReq, RangeNtf, RxFrameNtf, TxFrameReq
from unetpy.org_arl_unet_bb import RecordBasebandSignalReq, TxBasebandSignalReq, RxBasebandSignalNtf

from fjagepy.org_arl_fjage import Message as _Message
from types import MethodType as _mt

def _repr_pretty_(self, p, cycle):
    if cycle:
        p.text('...')
    elif self.perf is not None:
        p.text(self.perf)

setattr(_Message, '_repr_pretty_', _repr_pretty_)
