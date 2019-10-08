% MODEM_SETRECORDINGRATE Summary of this function goes here
%
% [buf, status] = modem_record(modem, nsamples)
%    modem    - object representing the modem connection
%    recordrate - record sampling rate 
%                 [supported rates are 96000 and 192000 sps]
%    status - return status as 0 when successful, -1 otherwise

function [status] = modem_setrecordingrate(modem, recordrate)

%% check modem object
if ~isjava(modem) || ~strcmp(modem.class,'org.arl.fjage.remote.Gateway')
  error('Invalid modem object');
end

%% select the approrpiate down converter ratio
if recordrate == 96000
    downconvRatio = 4;
end
if recordrate == 192000
    downconvRatio = 8;
end

%% set modem parameter appropriately
if modem_setparam(modem, [], org.arl.unet.Services.PHYSICAL, org.arl.yoda.ModemParam.downconvRatio, downconvRatio) < 0
    status = -1;
    disp('Failed to set record sampling rate..');
    return;
else
    status = 0;
    return;
end
