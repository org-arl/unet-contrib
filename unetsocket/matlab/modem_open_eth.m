% MODEM_OPEN_ETH Open a connection to the Unet modem
%
% [modem] = modem_open_eth(ip_address, port)
%   ip_address - IP address of the modem
%   port       - Port number (optional)
%   modem      - object representing the modem connection

function [modem] = modem_open_eth(ip_address, port)

%% create the modem gateway
org.arl.unet.JsonTypeAdapter.enable();
platform = org.arl.fjage.RealTimePlatform();
if nargin < 2 || isempty(port)
  port = 1100;
end
modem = org.arl.fjage.remote.Gateway(platform, ip_address, port);


