from unetpy.org_arl_unet import Services, ParameterReq, ParameterRsp, DatagramNtf, DatagramReq, AgentID
from unetpy.org_arl_unet_phy import Physical, RangeReq, RangeNtf, RxFrameNtf, TxFrameReq, TxFrameNtf, RxFrameStartNtf, BadFrameNtf
from unetpy.org_arl_unet_bb import RecordBasebandSignalReq, TxBasebandSignalReq, RxBasebandSignalNtf

from fjagepy.org_arl_fjage import Message as _Message
from fjagepy.org_arl_fjage_remote import Gateway as _Gateway
from types import MethodType as _mt


def _repr_pretty_(self, p, cycle):
    if cycle:
        p.text('...')
    elif self.perf is not None:
        p.text(self.perf)


setattr(_Message, '_repr_pretty_', _repr_pretty_)


class UnetGateway(_Gateway):

    def __init__(self, hostname, port=1100, name=None):
        super().__init__(hostname, port, name)

    def topic(self, name):
        a = super().topic(name)
        return AgentID(self, a.name, a.is_topic)

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
