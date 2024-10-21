package task4.abstracts;

import task4.implems.Channel.ReadListener;
import task4.implems.Channel.WriteListener;

public abstract class ChannelAbstract {
	  public abstract void setListener(ReadListener listener);
	  public abstract boolean read(byte[] bytes, int offset, int length);
	  public abstract boolean write(byte[] bytes, int offset, int length, WriteListener listener);
	  public abstract void disconnect();
	  public abstract boolean disconnected();
}