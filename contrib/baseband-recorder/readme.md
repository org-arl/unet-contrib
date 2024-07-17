# Baseband Recorder

The `BasebandRecorder` is a simple agent which triggers baseband recordings (which are then stored in a `signals` file) periodically. The duration of the recording (`recLength`) and the `interval` between recordings can be configured as agent parameters.

The `BasebandRecorder` agent requests recordings from the [Baseband](https://unetstack.net/handbook/unet-handbook.html#_baseband_service) service using [RecordBasebandSignalReq](https://unetstack.net/javadoc/3.3/index.html?org/arl/unet/bb/RecordBasebandSignalReq.html) and sends those recordings to the [BasebandMonitor](https://unetstack.net/handbook/unet-handbook.html#_baseband_service#_baseband_signal_monitor) agent for storage. The `BasebandMonitor` agent must be present in the stack for the `BasebandRecorder` agent to work.

## Usage

The agent can be added to a running UnetStack instance by copying the `BasebandRecorder.groovy` file to the `classes` directory of the UnetStack installation, and then adding the agent to the stack using the `container.add` command.

For example the following command adds a `BasebandRecorder` agent to the stack with a recording length of 1024 samples and an interval of 60 seconds:

```groovy
container.add 'recorder', new BasebandRecorder(recLength: 1024, interval: 60000)
```

The constructor of the `BasebandRecorder` agent takes two optional parameters: `recLength` and `interval` that map to the Agent parameters of the same name.

- `recLength` (default: 1024) - the number of samples to record each time the recording is triggered.
- `interval` (default: 10000) - the time between recordings in milliseconds. A value of 0 recording is disabled.

The agent parameters may also be modified afterwards by setting them from the shell or over an API. For example, to set the recording length to 2048 samples and the interval to 30 seconds, use the following commands:

```groovy
recorder.recLength = 2048
recorder.interval = 30000
```

## Notes

- Running this agent will set the `enable` parameter of the `BasebandMonitor` agent to `true` if it is not already set. This is to ensure that the `BasebandMonitor` agent is active and ready to receive recordings.
