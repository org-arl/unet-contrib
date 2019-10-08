% MODEM_TX_SIGNAL Transmit a signal.
%
% [status] = modem_tx_signal(modem, signal, fc)
%   modem   - object representing the modem connection
%   signal  - baseband or passband signal.
%             Baseband signal must be a sequence of numbers with 
%             alternating real and imaginary values. 
%             Passband signal must be a real time series to transmit.             
%   fc      - Signal carrier frequency in Hz for passband transmission
%             for baseband signal transmission set fc = 24000.
%             for passband signal transmission set fc = 0.
%   rsp     - response for the signal transmission request
%   status  - return status as 0 when successful, -1 otherwise

function [rsp, status] = modem_tx_signal(modem, signal, fc)

%% check modem object
if ~isjava(modem) || ~strcmp(modem.class,'org.arl.fjage.remote.Gateway')
  error('Invalid modem object');
end

%% subscribe to the agent providing baseband service
bb = modem.agentForService(org.arl.unet.Services.BASEBAND);

%% create the message with relevant attributes to be sent to the modem 
msg = org.arl.unet.bb.TxBasebandSignalReq();
msg.setSignal(signal);
msg.setCarrierFrequency(fc);
msg.setRecipient(bb);

%% send the message to modem
rsp = modem.request(msg, 10000);
if isjava(rsp) && rsp.getPerformative() == org.arl.fjage.Performative.AGREE    
    status = 0;
    return
else 
    status = -1;
    return     
end