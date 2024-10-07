# Sample custom Julia PHYSICAL layer implementation for UnetStack 5 based modems.
# Might require minor modifications to work with UnetStack 3 or 4 based modems.
#
# Signal processing based on blog article: https://blog.unetstack.net/custom-phy-in-julia.
#
# Do note that this implementation is intentionally minimalistic, to aid
# understanding. While this may be used as a starting point for your own
# development, the code here isn't directly suitable for production use.
# You'd want to have much better signal processing, error detection,
# error correction, clock synchronization, exception handling, queuing, etc.
# You would also want to support more messages (e.g. ClearReq, TxFrameNtf,
# RxFrameStartNtf), capabilities, and parameters.
#
# Usage:
#   $ julia --project
#   julia> include("MyJuliaPhy.jl")
#   julia> MyJuliaPhy.run("192.168.0.42")  # replace with modem's IP address
#
# Licensed under the MIT license:
#
# Copyright (c) 2024 Mandar Chitre.
#
# Permission is hereby granted, free of charge, to any person obtaining a
# copy of this software and associated documentation files (the "Software"),
# to deal in the Software without restriction, including without limitation
# the rights to use, copy, modify, merge, publish, distribute, sublicense,
# and/or sell copies of the Software, and to permit persons to whom the Software
# is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
# WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
# CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

module MyJuliaPhy

using Fjage
using UnetSockets

## frame types
const CONTROL = 1
const DATA = 2
const FRAMETYPES = CONTROL:DATA

## PDU format: [header(checksum, from, to, protocol, length), data]
const HDRSIZE = 5
const MTU = 8

## modulation parameters
const SPS = 150
const NFREQ = 1/15

### agent definition & startup

@agent struct Physical <: Agent
  node::Union{AgentID,Nothing} = nothing    # NODE_INFO agent
  bbsp::Union{AgentID,Nothing} = nothing    # BASEBAND agent
  bbfs::Float64 = 0.0                       # baseband sampling rate
end

function Fjage.setup(a::Physical)
  register(a, UnetSockets.Services.DATAGRAM)
  register(a, UnetSockets.Services.PHYSICAL)
end

function Fjage.startup(a::Physical)
  a.node = agentforservice(a, UnetSockets.Services.NODE_INFO)
  a.node === nothing && return stop(a, "Node info service not found")
  a.bbsp = agentforservice(a, UnetSockets.Services.BASEBAND)
  a.bbsp === nothing && return stop(a, "Baseband service not found")
  subscribe(a, a.bbsp)
  a.bbfs = a.bbsp.basebandRate
  a.bbsp[CONTROL].capture = (HDRSIZE + MTU) * 8 * SPS
  a.bbsp[DATA].capture = (HDRSIZE + MTU) * 8 * SPS
  @info "MyJuliaPhy ready"
end

### parameters to publish

Fjage.params(::Physical) = [
  "org.arl.unet.DatagramParam.MTU" => :MTU,
  "org.arl.unet.DatagramParam.RTU" => :RTU,
  "org.arl.unet.phy.PhysicalParam.maxPowerLevel" => :maxPowerLevel,
  "org.arl.unet.phy.PhysicalParam.minPowerLevel" => :minPowerLevel,
  "org.arl.unet.phy.PhysicalParam.time" => :time,
  "org.arl.unet.phy.PhysicalParam.busy" => :busy,
  "org.arl.unet.phy.PhysicalParam.rxEnable" => :rxEnable,
  "org.arl.unet.phy.PhysicalParam.timestampedTxDelay" => :timestampedTxDelay,
  "org.arl.unet.phy.PhysicalParam.baseband" => :baseband
]

Fjage.get(a::Physical, ::Val{:title}) = "MyJuliaPhy"
Fjage.get(a::Physical, ::Val{:description}) = "Demo Julia PHY layer"

Fjage.get(a::Physical, ::Val{:MTU}) = MTU
Fjage.get(a::Physical, ::Val{:RTU}) = MTU
Fjage.get(a::Physical, ::Val{:maxPowerLevel}) = a.bbsp.maxPowerLevel
Fjage.get(a::Physical, ::Val{:minPowerLevel}) = a.bbsp.minPowerLevel
Fjage.get(a::Physical, ::Val{:time}) = a.bbsp.time
Fjage.get(a::Physical, ::Val{:busy}) = a.bbsp.busy
Fjage.get(a::Physical, ::Val{:rxEnable}) = true
Fjage.get(a::Physical, ::Val{:timestampedTxDelay}) = 0
Fjage.get(a::Physical, ::Val{:baseband}) = string(a.bbsp)

### indexed parameters to publish

function Fjage.params(a::Physical, n::Int)
  n ∈ FRAMETYPES || return Pair{String,Symbol}[]
  [
    "org.arl.unet.DatagramParam.MTU" => :MTU,
    "org.arl.unet.DatagramParam.RTU" => :RTU,
    "org.arl.unet.phy.PhysicalChannelParam.frameLength" => :frameLength,
    "org.arl.unet.phy.PhysicalChannelParam.maxFrameLength" => :maxFrameLength,
    "org.arl.unet.phy.PhysicalChannelParam.fec" => :fec,
    "org.arl.unet.phy.PhysicalChannelParam.fecList" => :fecList,
    "org.arl.unet.phy.PhysicalChannelParam.powerLevel" => :powerLevel,
    "org.arl.unet.phy.PhysicalChannelParam.frameDuration" => :frameDuration,
    "org.arl.unet.phy.PhysicalChannelParam.dataRate" => :dataRate,
    "org.arl.unet.phy.PhysicalChannelParam.modulation" => :modulation,
    "org.arl.unet.phy.PhysicalChannelParam.modulationList" => :modulationList
  ]
end

Fjage.get(a::Physical, ::Val{:MTU}, i::Int) = i ∈ FRAMETYPES ? MTU : nothing
Fjage.get(a::Physical, ::Val{:RTU}, i::Int) = i ∈ FRAMETYPES ? MTU : nothing
Fjage.get(a::Physical, ::Val{:frameLength}, i::Int) = i ∈ FRAMETYPES ? MTU + HDRSIZE : nothing
Fjage.get(a::Physical, ::Val{:maxFrameLength}, i::Int) = i ∈ FRAMETYPES ? MTU + HDRSIZE : nothing
Fjage.get(a::Physical, ::Val{:powerLevel}, i::Int) = i ∈ FRAMETYPES ? 0 : nothing
Fjage.get(a::Physical, ::Val{:frameDuration}, i::Int) = i ∈ FRAMETYPES ? frameduration(a) : nothing
Fjage.get(a::Physical, ::Val{:dataRate}, i::Int) = i ∈ FRAMETYPES ? 8 * (MTU + HDRSIZE) / frameduration(a) : nothing

### transmission request processing

function Fjage.processrequest(a::Physical, req::DatagramReq)
  i = something(req.type, DATA)
  i ∈ FRAMETYPES || return RefuseRsp(req, "Invalid type")
  data = UInt8.(mod.(something(req.data, Int[]), 256))
  length(data) > MTU && return RefuseRsp(req, "Data too long")
  buf = zeros(UInt8, HDRSIZE + MTU)       # PDU buffer
  buf[2] = something(a.node.address, 0)   # from address
  buf[3] = req.to                         # to address
  buf[4] = req.protocol                   # protocol number
  buf[5] = length(data)                   # data length
  buf[6:6+length(data)-1] .= data         # data bytes
  buf[1] = reduce(xor, buf[2:end])        # parity byte (checksum)
  x = bytes2signal(buf)                   # modulate the data
  send(a, TxBasebandSignalReq(recipient=a.bbsp, preamble=i, signal=x))
  return Message(req, Performative.AGREE)
end

### reception processing

function Fjage.processmessage(a::Physical, msg::RxBasebandSignalNtf)
  msg.preamble ∈ FRAMETYPES || return
  buf = signal2bytes(msg.signal[msg.preambleLength+1:end])
  length(buf) == HDRSIZE + MTU || return
  if reduce(xor, buf[1:end]) == 0          # parity (checksum) check
    # good frame, check if addressed to us (otherwise publish on snoop)
    forme = buf[3] ∈ (Address.BROADCAST, something(a.node.address, 0))
    ntf = RxFrameNtf(
      recipient = forme ? topic(AgentID(a)) : topic(AgentID(a), "snoop"),
      type = msg.preamble,
      rxStartTime = msg.rxStartTime,
      location = msg.location,
      rssi = msg.rssi,
      from = buf[2],
      to = buf[3],
      protocol = buf[4],
      data = buf[6:5+buf[5]]
    )
  else
    # bad frame, publish as such
    ntf = BadFrameNtf(
      recipient = topic(AgentID(a)),
      type = msg.preamble,
      rxStartTime = msg.rxStartTime,
      location = msg.location,
      rssi = msg.rssi,
      data = buf
    )
  end
  send(a, ntf)
end

### entry point: start a slave container and load agent

function run(ip, port=1100, name="phy2")
  c = SlaveContainer(ip, port)
  add(c, name, Physical())
  start(c)
end

### signal processing

function frameduration(a)
  n = (HDRSIZE + MTU) * 8 * SPS
  return n / a.bbfs
end

# adapted from https://blog.unetstack.net/custom-phy-in-julia
function bytes2signal(buf)
  signal = Array{ComplexF32}(undef, length(buf) * 8 * SPS)
  p = 1
  for b in buf
    for j in 0:7
      bit = (b >> j) & 0x01
      f = bit == 1 ? -NFREQ : NFREQ
      signal[p:p+SPS-1] .= cis.(2pi * f * (0:SPS-1))
      p += SPS
    end
  end
  return signal
end

# adapted from https://blog.unetstack.net/custom-phy-in-julia
function signal2bytes(signal)
  n = length(signal) ÷ (SPS * 8)
  buf = zeros(Int8, n)
  p = 1
  for i in 1:length(buf)
    for j in 0:7
      s = @view signal[p:p+SPS-1]
      p += SPS
      x = cis.(2pi * NFREQ .* (0:SPS-1))
      s0 = sum(s .* conj.(x))
      s1 = sum(s .* x)
      if abs(s1) > abs(s0)
        buf[i] = buf[i] | (0x01 << j)
      end
    end
  end
  return buf
end

end
