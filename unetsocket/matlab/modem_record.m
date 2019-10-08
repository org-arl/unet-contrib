% MODEM_RECORD Summary of this function goes here
%
% [buf, status] = modem_record(modem, nsamples)
%    modem    - object representing the modem connection
%    nsamples - number of samples to record
%    buf      - recorded baseband signal
%    status - return status as 0 when successful, -1 otherwise

function [buf, status] = modem_record(modem, nsamples)

%% check modem object
if ~isjava(modem) || ~strcmp(modem.class,'org.arl.fjage.remote.Gateway')
  error('Invalid modem object');
end

%% check arguments
if nargin < 2 || isempty(nsamples)
  nsamples = 65536;
end

%% subscribe to the agent providing the baseband service
bb = modem.agentForService(org.arl.unet.Services.BASEBAND); 
modem.subscribe(bb);

%% create the message with relevant attributes to be sent to the modem 
req = org.arl.unet.bb.RecordBasebandSignalReq(); 
req.setRecLength(java.lang.Integer(nsamples));
req.setRecipient(bb);

%% send the message to the modem and wait for the response 
rsp = modem.request(req, 5000);
if isjava(rsp) && rsp.getPerformative() == org.arl.fjage.Performative.AGREE
    cls = org.arl.unet.bb.RxBasebandSignalNtf().getClass(); 
    ntf = modem.receive(cls, 10000);
end
if isempty(ntf)
    disp('Recording failed.')
    status = -1;
    return 
else
    buf = ntf.getSignal();
    status = 0;
end