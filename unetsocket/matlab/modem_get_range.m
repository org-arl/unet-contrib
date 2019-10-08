% MODEM_GET_RANGE Measure range to the any other node
%
% [range] = modem_get_range(modem, to)
%   modem  - object representing the modem connection
%   to     - address of the node to which range is to be measured
%   range  - returns range measured in meters
%   status - return status as 0 when successful, -1 otherwise

function [range, status] = modem_get_range(modem, to)

%% check modem object
if ~isjava(modem) || ~strcmp(modem.class,'org.arl.fjage.remote.Gateway')
  error('Invalid modem object');
end

%% subscribe to the agent providing the ranging service
ranging = modem.agentForService(org.arl.unet.Services.RANGING); 
modem.subscribe(ranging);

%% create the message with relevant attributes to be sent to the modem
req = org.arl.unet.phy.RangeReq();
req.setTo(to);
req.setRecipient(ranging);

%% send the message to the modem and wait for the response 
rsp = modem.request(req, 5000);

%% check if the message was successfully sent and extract range
if isjava(rsp) && rsp.getPerformative() == org.arl.fjage.Performative.AGREE
    cls = org.arl.unet.phy.RangeNtf().getClass();
    ntf = modem.receive(cls, 5000);
end
if isempty(ntf)
    disp('Range measurement failed.')
    status = -1;
    return 
else
    range = ntf.getRange();
    status = 0;
    return 
end