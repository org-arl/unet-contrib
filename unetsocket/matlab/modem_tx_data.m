% MODEM_TX_DATA Transmit a data packet.
%
% [status] = modem_tx_data(modem, to, data, type)
%   modem  - object representing the modem connection
%   to     - address of the node to which data is to be transmitted 
%   data   - data to be transmitted 
%   type   - packet type 
%   rsp    - response for the signal transmission request
%   status - returns non-negative value on success

function [rsp, status] = modem_tx_data(modem, to, data, type)

%% check modem object
if ~isjava(modem) || ~strcmp(modem.class,'org.arl.fjage.remote.Gateway')
  error('Invalid modem object');
end

%% check arguments
if nargin < 2 || isempty(to)
    to = 0;
end
if nargin < 3 || isempty(data)
    data = [];
end
if nargin < 4 || isempty(type)
    type = 1;
end

%% subscribe to the agent providing the physical service
phy = modem.agentForService(org.arl.unet.Services.PHYSICAL); 
modem.subscribe(phy);

%% create the message with relevant attributes to be sent to the modem
if type == 1 || type == 2
    req = org.arl.unet.phy.TxFrameReq();
    req.setType(type);
else
    req = org.arl.unet.DatagramReq();
end
req.setTo(to);
req.setData(data);    
req.setRecipient(phy);

%% send the message to the modem and wait for the response 
rsp = modem.request(req, 5000);
if isjava(rsp) && rsp.getPerformative() == org.arl.fjage.Performative.AGREE
    status = 0;
    return
else
    status = -1;
    return
end