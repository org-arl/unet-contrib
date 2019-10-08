% MODEM_FLUSH Clear incoming messages from modem
%
% modem_flush(modem)
%   modem - object representing the modem connection

function modem_flush(modem)

while ~isempty(modem.receive())
  % do nothing, simply eat up messages
end

