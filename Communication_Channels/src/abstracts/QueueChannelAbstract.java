package abstracts;

public abstract class QueueChannelAbstract {
    public abstract void send(byte[] bytes, int offset, int length);
    public abstract byte[] receive();
    public abstract void close();
    public abstract boolean closed();

}
