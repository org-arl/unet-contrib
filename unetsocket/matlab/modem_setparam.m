% MODEM_SETPARAM  Setter for modem parameters.
%
% [value, status] = modem_setparam(modem, index, target_name, param_name, value)
%      modem       - object representing the modem connection
%      index       - index is set for indexed parameters, otherwise set to 0
%      target_name - fully qualified service class name/ agent name
%      param_name  - name of the modem parameter to set
%      value       - value to set
%      status      - return status as 0 when successful, -1 otherwise

function [status] = modem_setparam(modem, index, target_name, param_name, value)

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
req.set(param_name, value);
req.setRecipient(agent);
req.setIndex(index);

%% send the message to the modem and wait for the response
rsp = modem.request(req, 5000);
if isjava(rsp) && rsp.getPerformative() == org.arl.fjage.Performative.INFORM
    status = 0;
    return 
else
    disp('Failed to set the value');
    status = -1;
    return
end