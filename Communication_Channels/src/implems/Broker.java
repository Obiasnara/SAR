package implems;

import abstracts.BrokerAbstract;
import abstracts.ChannelAbstract;

public class Broker extends BrokerAbstract {

	private String name;

	public Broker(String name) {
		this.name = name;
		BrokerManager.getInstance().addBroker(this.name, this);
	}

	@Override
	public ChannelAbstract accept(int port) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChannelAbstract connect(String name, int port) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
