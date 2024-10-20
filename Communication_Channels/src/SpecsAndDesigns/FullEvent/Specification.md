# Aim

The aim of this **full event** version of the broker and channel is to remove any threads. Will only remain one event pump running in one thread that handles everything.

The task3 and task4 implementations will offer the user a choice in terms of paradigm.

## The new Broker

```java
package abstracts.full_event;

public abstract class BrokerAbstract {
	//Broker(String name) {};
	abstract boolean accept(int port, AcceptListener acl);
	abstract boolean connect(int port, String name, ConnectListener cnl);
	abstract boolean disconnect(int port, String name, ConnectListener cnl);
}
```

The main challenge is to handle the "*rendez-vous*" eventfully without blocking.

When an accept or connect is called, the broker should generate an event when a connection is established. The event should contain the created channel.

When a disconnect is called, the broker should generate an event when the connection is closed. The event should contain nothing.

One call to accept will open a port on the Broker and listen to connect events, if a connection is closed, the broker should return to listening on that openned port.

### Chain connect/disconnect complexity 

In order to ensure that the system is robust enough for future extensive use, the broker will need to handle the following scenario:
- A accept is called on a port (the broker is listening)
REPEAT {
- A connect is called on a port (creating the channels)
- A disconnect is called on the same port (the broker is listening again)
} UNTIL 1M times

## The new Channel

Nothing for now.