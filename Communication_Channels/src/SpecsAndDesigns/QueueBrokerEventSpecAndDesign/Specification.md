# Aim 

The aim of this *eventfull Queue Broker* is to provide a mechanism to allow for the event driven communication between two tasks. In this version we give the illusion of full eventfull communication between two tasks. The user can asynchronously bind to another broker. When the connection is established a AcceptListener `accepted` event is triggered. The user can also connect to another broker. When the connection is established a ConnectListener `connected` event is triggered. Or if the connection is refused a ConnectListener `refused` event is triggered. 

The underlying implementation is based on the use of a `Broker` and a `Channel`. The `Broker` is responsible for the binding and connecting to other brokers. The `Channel` is responsible for the sending and receiving of messages. These two classes are threaded but hidden from the user. The user only interacts with the `QueueBroker` and `QueueChannel` classes that support event driven communication.

## QueueBroker

The user can request a bind or an accept to another broker just like before. When the connection is established an AcceptListener `accepted` event is triggered, the listener is passed to the bind method.

The user needs to give an Accept listener in order to use the `bind` method. The `bind` method is asynchronous and returns immediately. The user can also unbind from a port.
```java
public interface AcceptListener {
    void accepted(QueueChannelAbstract queue);
}

public abstract boolean bind(int port, AcceptListener listener);
public abstract boolean unbind(int port);
```

The user can request a connect to another broker. When the connection is established a ConnectListener `connected` event is triggered. Or if the connection is refused a ConnectListener `refused` event is triggered. The listener is passed to the connect method. The `connect` method is asynchronous and returns immediately, false if the broker directly refused the connection, else it returns true.
```java
public interface ConnectListener {
    void connected(QueueChannelAbstract queue);
    void refused();
}

public abstract boolean connect(String name, int port, ConnectListener listener);
```

## QueueChannel

The queue channel is an abstraction of the Threaded Channel class. The user can use it to send a message to another. The user can also set a listener to receive messages from the other broker. The listener is passed to the `setListener` method. The `send` method is asynchronous and returns immediately, false if the broker is closed, else it returns true.

The user can close the channel. The user can also check if the channel is closed. The `closed` method returns true if the channel is closed, else it returns false. If the channel is closed, both sides will receive a `closed` event.

```java

public abstract class QueueChannelAbstract {
	public interface Listener {
		void received(byte[] msg);
		void sent(Message msg);
		void closed();
	}
	
	public abstract void setListener(Listener l);
	
	public abstract boolean send(Message mst);
	
	public abstract void close();
	
	public abstract boolean closed();
}
```

## Task

The task is the took required for the user to be able to use the eventfull communication channels. The user can post a runnable to the EventPump. The user can also kill the task, this will stop all the threads associated with the task. The user can check if the task is killed. The `killed` method returns true if the task is killed, else it returns false. 

If the task is killed, the posted Runnable is removed from the event pump queue (if possible), any other post will silently be dropped. If the task was assigned a queue, the queue is closed.

```java

public abstract class TaskAbstract extends Thread {
	public abstract void post(Runnable r);
	public static TaskAbstract task() { return null; };
	public abstract void kill();
	public abstract boolean killed();
}
```

# Design

[Here is an example of the execution of a basic scenario](./image.png)