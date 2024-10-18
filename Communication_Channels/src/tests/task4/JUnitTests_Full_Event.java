package tests.task4;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import task4.abstracts.ChannelAbstract;
import task4.abstracts.TaskAbstract;
import task4.implems.Broker;
import task4.implems.Broker.AcceptListener;
import task4.implems.Broker.ConnectListener;
import task4.implems.EventPump;
import task4.implems.Task;

public class JUnitTests_Full_Event {
	
	protected Broker testBroker;
	
	@BeforeAll
	static void init(){
		
	}
	
	@BeforeEach
	void cleanUp() {
		testBroker = new Broker("Broker1");
	}
	
	@RepeatedTest(1)
	public void connectionTest() {
		try {
			TaskAbstract t1 = Task.task();
			TaskAbstract t2 = Task.task();
			
			ConnectListener cl = new ConnectListener() {
	
				@Override
				public void refused() {
					System.err.println("Should not refuse");				
				}
	
				@Override
				public void connected(ChannelAbstract queue) {
					System.out.println("Connected !!");
				}
				
			};
			
			AcceptListener ac = new AcceptListener() {
				@Override
				public void accepted(ChannelAbstract queue) {
					System.out.println("Accepted !!");
				}
			};
			
			t1.post(()-> testBroker.accept(8080, ac), "Accept");
			t2.post(()-> testBroker.connect(8080, "Broker1", cl), "Connect1");
			t2.post(()-> testBroker.connect(8080, "Broker100", cl), "Connect2");
			
			EventPump.getInstance().start();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
