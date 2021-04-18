import Pkg
Pkg.instantiate()

using ArgParse
import JSON

### analysis functions

function groups(data; init=Tuple{String,Any}[])
  if "events" ∈ keys(data)
    events = data["events"]
    n = length(init)
    for e ∈ events
      groups(e; init)
    end
    length(init) == n && push!(init, (data["group"], events))
  end
  init
end

function findtrace(cache, ev)
  if "threadID" ∈ keys(ev)
    id = ev["threadID"]
    id ∈ keys(cache) && return cache[id]
  end
  if "stimulus" ∈ keys(ev)
    id = ev["stimulus"]["messageID"]
    id ∈ keys(cache) && return cache[id]
  end
  0
end

function addtocache!(cache, ev, trace)
  if "threadID" ∈ keys(ev)
    id = ev["threadID"]
    cache[id] = trace
  end
  if "stimulus" ∈ keys(ev)
    id = ev["stimulus"]["messageID"]
    cache[id] = trace
  end
  if "response" ∈ keys(ev)
    id = ev["response"]["messageID"]
    cache[id] = trace
  end
end

function splitcomponent(s)
  m = match(r"^(?<name>.*)::(?<clazz>[^/]*)/?(?<node>.*)$", s)
  m === nothing && return "", "", ""
  m[:name], m[:clazz], m[:node]
end

function prettymessage(msg)
  clazz = msg["clazz"]
  p = findlast('.', clazz)
  p === nothing || (clazz = clazz[p+1:end])
  sender = "sender" ∈ keys(msg) ? msg["sender"] : ""
  recipient = "recipient" ∈ keys(msg) ? msg["recipient"] : ""
  "$clazz ⟦ $sender → $recipient ⟧"
end

function analyze(events)
  traces = Tuple{Int64,String,Vector{Any}}[]
  cache = Dict{String,Int64}()
  for ev ∈ events
    trace = findtrace(cache, ev)
    if trace == 0
      cname, cclazz, cnode = splitcomponent(ev["component"])
      cnode != "" && (cnode = "[$cnode] ")
      desc = cnode * cname
      if "stimulus" ∈ keys(ev)
        desc = cnode * prettymessage(ev["stimulus"])
      elseif "response" ∈ keys(ev)
        desc = cnode * prettymessage(ev["response"])
      end
      push!(traces, (ev["time"], desc, [ev]))
      trace = length(traces)
    else
      push!(traces[trace][3], ev)
    end
    addtocache!(cache, ev, trace)
  end
  traces
end

function capture!(actors, msgs, origin, node, agent, t, msg, stimulus)
  "recipient" ∉ keys(msg) && return
  recipient = msg["recipient"]
  if startswith(recipient, '#')
    stimulus || return
    recipient = agent
  end
  onode = node
  okey = msg["messageID"] * "→" * recipient
  if okey ∈ keys(origin)
    n = origin[okey]
    onode == n && return
    onode = n
  end
  stimulus && "sender" ∉ keys(msg) && return
  sender = "sender" ∈ keys(msg) ? msg["sender"] : agent
  if node != ""
    sender = sender * "/" * onode
    recipient = recipient * "/" * node
  end
  clazz = msg["clazz"]
  if clazz == "org.arl.fjage.Message"
    clazz = msg["performative"]
  else
    p = findlast('.', clazz)
    p === nothing || (clazz = clazz[p+1:end])
  end
  origin[okey] = node
  sender == recipient && return
  push!(actors, (onode, sender))
  push!(actors, (node, recipient))
  push!(msgs, (t, clazz, sender, recipient))
end

function sequence(events)
  actors = Tuple{String,String}[]
  msgs = Tuple{Int64,String,String,String}[]
  origin = Dict{String,String}()
  for ev ∈ events
    cname, cclazz, cnode = splitcomponent(ev["component"])
    "stimulus" ∈ keys(ev) && capture!(actors, msgs, origin, cnode, cname, ev["time"], ev["stimulus"], true)
    "response" ∈ keys(ev) && capture!(actors, msgs, origin, cnode, cname, ev["time"], ev["response"], false)
  end
  unique!(sort!(actors; by = x -> x[1]))
  actors, msgs
end

function mermaid(actors, msgs)
  println("sequenceDiagram")
  for a ∈ actors
    id = replace(a[2], '/' => '_')
    println("  participant $id as $(a[2])")
  end
  for m ∈ msgs
    id1 = replace(m[3], '/' => '_')
    id2 = replace(m[4], '/' => '_')
    print("  $id1")
    m[2] == "AGREE" && print('-')
    println("->>$id2: $(m[2])")
  end
end

### main

aps = ArgParseSettings()
@add_arg_table! aps begin
  "--group", "-g"
    help = "event group number"
    arg_type = Int
    default = 0
  "--trace", "-t"
    help = "trace number"
    arg_type = Int
    default = 0
  "filename"
    help = "JSON trace file"
    default = "trace.json"
end
args = parse_args(ARGS, aps)

if !isfile(args["filename"])
  println(args["filename"], " not found")
  exit(1)
end

grps = groups(JSON.parsefile(args["filename"]))

if length(grps) < 1
  println("no traces found")
  exit(1)
end

g = args["group"]
length(grps) == 1 && g == 0 && (g = 1)

if g == 0
  println("Specify an event group:")
  for (i, g1) ∈ enumerate(grps)
    println(i, ": ", g1[1], " (", length(g1[2]), " events)")
  end
  exit(1)
end

if g < 1 || length(grps) < g
  println("invalid event group number")
  exit(1)
end

traces = analyze(grps[g][2])

t = args["trace"]
length(traces) == 1 && t == 0 && (t = 1)

if t == 0
  println("Specify a trace:")
  for (i, t1) ∈ enumerate(traces)
    println(i, ": ", t1[1], " ", t1[2], " (", length(t1[3]), " events)")
  end
  exit(1)
end

if t < 1 || length(traces) < t
  println("invalid trace number")
  exit(1)
end

mermaid(sequence(traces[t][3])...)
