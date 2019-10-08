% MODEM_CLOSE Close connection to Unet modem
%
% modem_close(modem)
%   modem  - object representing the modem connection
%   status - returns non-negative value on success

function modem_close(modem)

%% check modem object
if ~isjava(modem) || ~strcmp(modem.class,'org.arl.fjage.remote.Gateway')
  error('Invalid modem object');
end

%% shutdown the gateway
modem.shutdown();
