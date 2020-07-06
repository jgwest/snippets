

An example implementation of how to consume the `odo component status -o json` logs and `odo push -o json` JSON events.

## Prerequisites

Ensure that:
- Java 8 (or above) is installed, Maven is installed
- A recent version of ODO is available
- You have an existing odo component (preferably one that has already been pushed) in a folder 

## How to run

```
git clone https://github.com/jgwest/snippets
git checkout odo-event-consumer

cd odo-event-consumer
mvn package

java -jar "target/odo-event-consumer-0.0.1-SNAPSHOT.jar"  "path to odo executable"  "path to existing, pushed odo component"  "(optional) path to kubernetes context file, if specified will set KUBECONFIG"`

```

## Example output

(Lines beginning with `#` are comments)

```
[C:\Users\JONATHANWest\go\src\github.com\openshift\odo\odo, component, status, --follow, -o, json]
- Status: Running  { ComponentStatus: errorOccurred=null, podRunning=true, containerRunning=true, supervisordProgramRunning=true }

# Next, in another terminal window, I kill the pod

- Status: Not running - Container not running { ComponentStatus: errorOccurred=null, podRunning=true, containerRunning=false, supervisordProgramRunning=false }
# The pod is immediately restarted by k8s, but the container is not yet running
- Status: Not running - Supervisord program not running { ComponentStatus: errorOccurred=null, podRunning=true, containerRunning=true, supervisordProgramRunning=false }
# The pod restart is detected, and the container is restarted, but the status stays at 'Not running', since the supervisord program is not restarted.
# Also observe that the error substatus is updated based on the kubernetes pod status, container status, and supervisord status, as additional data is received.

# Next, in another terminal window, I repush the component
- Status: Running  { ComponentStatus: errorOccurred=null, podRunning=true, containerRunning=true, supervisordProgramRunning=true }
# The push is detected and the status goes back to 'Running'
```

You can try other failure modes and observe that they are correctly reported by the tool:
- Exec into the container of the component pod, and stop the supervisord-managed program in the container, and observe that the component status shifts to 'Not running'
- Disable your network connection, and observe that an error is reported and the component status shifts to 'Not running'
- Re-enable your network connection, and observe that the status returns to 'Running'


