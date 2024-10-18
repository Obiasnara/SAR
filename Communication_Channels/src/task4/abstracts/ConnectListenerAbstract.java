package task4.abstracts;


public abstract class ConnectListenerAbstract extends Listener {

	public abstract void refused();

    public abstract void connected(ChannelAbstract queue);

}
