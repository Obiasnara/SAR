
# Overview: Broker / Channel

A channel is a communication channel, a point-to-point stream of bytes.
Full-duplex, each end point can be used to read or write.
A connected channel is FIFO and lossless, see Section "Disconnecting"
for details about disconnection.

The typical use of channels is by two tasks to establish a full-duplex communication. However, there is no ownership between channel and tasks, any task may read or write in any channel it has the reference to. The following rules apply:

- It is entirely thread-safe for the two tasks to read or write at   either end point of the channels concurrently. 
- Locally, at one end point, two tasks, one reading and the other writing, operating concurrently is safe also. 
- However, concurrent read operations or concurrent write operations are not safe on the same end point.  

A channel is either connected or disconnected. It is created connected and it becomes disconnected when either side requests a disconnect. There is no notion of the end of stream for a connected stream. To mark the end of a stream, the corresponding channel is simply disconnected.

# Connecting

A channel is established, in a fully connected state, when a connect 
matches an accept. When connecting, the given name is the one of the remote broker, the given port is the one of an accept on that remote broker.

There is no precedence between connect and accept, this is a symmetrical rendez-vous: the first operation waits for the second one. Both accept and connect operations are therefore blocking calls, blocking until the rendez-vous happens, both returning a fully connected and usable full-duplex channel.

When connecting, we may want to distinguish between two cases:
(i) there is no accept yet and (ii) there is not such broker. 
When the named broker does not exist, the connect returns null, 
but if the remote broker is found, the connect blocks until 
there is a matching accept otherwise so that a channel can be
constructed and returned. 

Note: we could consider introducing a timeout here, limiting the wait for the rendez-vous to happen.

# Writing

Signature: write(byte[] bytes,int offset,int length)int

When writing, the given byte array contains the bytes to write
from the given offset and for the given length. The range [offset,offset+length[ must be within the array boundaries, without wrapping around at either ends. 

The method "write" returns the number of bytes actually written that
may not be zero or negative. If zero would be return, the write operation blocks instead until it can make some progress.

Nota Bene: a channel is a stream, so although the write operation 
does take a range of bytes to write from an array of bytes, the
semantics is one that writes one byte at a time in the stream.

The method "write" blocks if there is no room to write any byte.
The rationale is to avoid spinning when an application tries to send
a certain number of bytes and the stream can make no progress. 
Here is an example:

  void send(byte[] bytes) {
    int remaining = bytes.length;
    int offset = 0;
    while (remaining!=0) {
      int n = channel.write(bytes,offset,remaining);
      offset += n;
      remaining -= n;
    }
  }

If the method "write" is currently blocked and the channel becomes
disconnected, the method will throw a channel exception. Invoking a write operation on a disconnected also throws a channel exception.

# Reading

Signature: read(byte[] bytes,int offset,int length)int

When reading, the given byte array will contain the bytes read,
starting at the given offset. The given length provides the maximum number of bytes to read. The range [offset,offset+length[ must be within the array boundaries, without wrapping around at either ends.

The method "read" will return the number of bytes actually read, that may not be zero or negative. If zero would be returned, the method "read" blocks instead, until some bytes become available.

The rationale is that a loop trying to read a given length, looping over until all the needed bytes are read will not induce an active polling. Here is an example:

  void receive(byte[] bytes) throws DisconnectedException {
    int remaining = bytes.length;
    int offset = 0;
    while (remaining!=0) {
      int n = channel.read(bytes,offset,remaining);
      offset += n;
      remaining -= n;
    }
  }

The end of stream is the same as being as the channel being disconnected, so the method will throw an exception (DisconnectedException). 

Note: notice that the disconnected exception does not always indicate an error, rarely in fact. The end of stream is an exceptional situation, but it is not an error. Remember that exceptions are not only for errors, but for exceptional situations, hence their name.
The disconnected exception may give some extra information regarding an error if an internal error caused the channel to disconnect.   

# Disconnecting

A channel can be disconnected at any time, from either side. So this requires an asynchronous protocol to disconnect a channel. 

The effect of disconnecting a channel must be specified for both ends, the one that called the method "disconnect" as well as the other end. In the following, we will talk about the local side versus remote side, the local side being the end where the method "disconnect" has been called.

Note: of course, both ends may call the method "disconnect" concurrently and the protocol to disconnect the channel must still work.

Note: since we have not asserted a strict ownership model between tasks and channels, it is possible that a channel be disconnected
while some operations are pending locally. These operations must be interrupted, when appropriate, throwing a disconnected exception.

The local rule is simple, once the method "disconnect" has been called on a channel, it is illegal to invoke the methods "read" or "write". Only the method "disconnected" may be called to check the status of the channel. In other words, if the method "disconnected" returns true, the methods "read" and "write" must not be invoked. If they are invoked nevertheless, the invocation will result in an disconnected
exception being thrown.

The remote rule is more complex to grasp, that is, when the remote side disconnects a channel, how should that be perceived locally?

The main issue is that there may be still bytes in transit, bytes that the local side must be able to reads. By in transit, we mean bytes that were written by that remote side, before it disconnected the channel, and these bytes have not been read on a local side. 
Therefore, if we want the local side to be able to read these last bytes, the local side should not be considered disconnected until all these bytes have been read or the channel is locally disconnected.

This means that the local side will only become disconnected when the remote has been disconnected and there are no more in-transit bytes to read. This means that a local channel appears as not yet disconnected although its far side has already been disconnected. This means that we need to specify how should local write operations behave in 
this half-disconnected state. The simplest is to drop the bytes silently, as if they were written, preserving the local illusion that the channel is still connected. 

This behavior may seem counter-intuitive at first, but it is the only one that is consistent and it is in fact the easiest one on developers. First, allowing to read the last bytes in transit is mandatory since it is likely that a communication will end by writing some bytes and then disconnecting. Something like saying "bye" and 
then hanging up.

Second, dropping written bytes may seem wrong but it is just leveraging an unavoidable truth: written bytes may be dropped even though channels are FIFO and lossless. Indeed, it is not at all different than if the bytes were written before the other side disconnected a channel without reading all pending bytes. In both cases, the bytes would be dropped.

Nota Bene: one should resist the temptation to adopt an immediate synchronous disconnection. Indeed, it would not be possible if our channels would not be implemented over shared memory. Disconnecting would imply sending a control message to inform the other side and thus the disconnect protocol would be asynchronous. 

# Brokers and Multi-tasking

The question about the relationship between tasks, brokers, and channels.

Since a connect is blocking, a task may not try to connect to the same
name and port concurrently, but multiple tasks can. Similarly, only
one task may accept on a given port on a given broker. But different 
tasks on different brokers may accept on the same port number. And 
of course, multiple tasks may accept on different ports on the same
broker.

Since the operations "read" and "write" may block the calling task,
it is important to specify what happens if the channel is disconnected
while tasks are blocked. The blocked operations will throw an exception (DisconnectedException). This must happen when the channel is disconnected from either sides. This means that it is safe for a task to disconnect a channel on the same side that another task is currently blocked on.

We know that each task is related to a broker, by its constructor. But a broker can be used by multiple tasks. Therefore brokers may be shared between tasks, so brokers must be thread-safe, using proper synchronized internally.

Channels are different because of the byte stream nature and the fact that the operation "read" and "write" may operate only partially. Because of that, synchronized operations would not help multiple tasks concurrently writing or read. Indeed, there is no notion of messages, defined as a consistent sequence of bytes, channels are just pushing bytes through or pulling bytes out. 

Therefore, it makes no sense to synchronize the methods "read" and "write" at the level of a channel. The synchronization must occur above, at the level of sending or receiving full messages. 

Yet, multiple tasks may use the same channel. One classical example is a reader task and a writer task, due to the blocking nature of the method "read" and "write". Also, multiple writes may send messages through the same channel if they are properly synchronized.






