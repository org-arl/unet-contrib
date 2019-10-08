% MODEM_GETPARAM  Getter for modem parameters.
% [value, status] = modem_getparam(modem, index, target_name, param_name)
%      modem       - object representing the modem connection
%      index       - index is set for indexed parameters, otherwise set to 0
%      target_name - fully qualified service class name/ agent name
%      param_name  - name of the modem parameter to get
%      value       - returns value read
%      status - return status as 0 when successful, -1 otherwise

function [value, status] = modem_getparam(modem, index, target_name, param_name)

%% check modem object
if ~isjava(modem) || ~strcmp(modem.class,'org.arl.fjage.remote.Gateway')
  error('Invalid modem object');
end

%% check arguments
if isempty(index)
    index = -1;
end

%% subscribe to the agent providing the targeted service
agent = modem.agentForService(target_name);
modem.subscribe(agent);

%% create the message with relevant attributes to be sent to the modem
req = org.arl.unet.ParameterReq();
req.get(param_name);
req.setRecipient(agent);
req.setIndex(index);

%% send the message to the modem and wait for the response
rsp = modem.request(req, 5000);
if isjava(rsp) && rsp.getPerformative() == org.arl.fjage.Performative.INFORM
    value = rsp.get(param_name);
    status = 0;
    return 
else
    disp('Failed to get the value');
    status = -1;
    return
end