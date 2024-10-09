package tests;


import java.util.concurrent.Semaphore;

import abstracts.event_queue.QueueBrokerAbstract.AcceptListener;
import abstracts.event_queue.QueueBrokerAbstract.ConnectListener;
import abstracts.event_queue.QueueChannelAbstract;
import implems.BrokerManager;
import implems.event_queue.QueueBroker;


public class tests_v2_romain_rosano {
	public static void main(String[] args) {
		try {
			BrokerManager.getInstance().removeAllBrokers();
			test1();
			BrokerManager.getInstance().removeAllBrokers();
			test2(1, 1);
			BrokerManager.getInstance().removeAllBrokers();
			test2(10, 2);
			BrokerManager.getInstance().removeAllBrokers();
			test3(20);
			BrokerManager.getInstance().removeAllBrokers();
			test4();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("	The test has failed : " + e.getMessage());
		}
	}

	// Echo server (from the specification doc)
	public static void test1() throws Exception {
		System.out.println("Test 1 in progress...");
		Semaphore sm = new Semaphore(0); // Allows to block the execution until the echo message

		QueueBroker client = new QueueBroker("client");
		QueueBroker server = new QueueBroker("server");
		int connection_port = 6969;

		client.connect("server", connection_port, new ConnectListener() {
			@Override
			public void refused() {
				System.out.println("Connection refused");
			}

			@Override
			public void connected(QueueChannelAbstract queue) {
				// TODO Auto-generated method stub
				
			}
		});

		server.bind(connection_port, new AcceptListener() {
			@Override
			public void accepted(QueueChannelAbstract queue) {
				// TODO Auto-generated method stub
				
			}
		});

		sm.acquire(); // Waits the end of the test
		System.out.println("Test 1 done !\n");
	}

	private static void echo_client(QueueBroker client, int connection_port, Semaphore sm) {
		client.connect("server", connection_port, new ConnectListener() {
			@Override
			public void refused() {
				System.out.println("Connection refused");
			}

			@Override
			public void connected(QueueChannelAbstract queue) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	private static void echo_server(QueueBroker server, int connection_port) {
		server.bind(connection_port, new AcceptListener() {
			public void accepted(QueueChannelAbstract queue) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	// Echo server with several clients on same port
	public static void test2(int nbre_clients, int test_number) throws Exception {
		System.out.println("Test 2." + test_number + " in progress...");
		Semaphore sm = new Semaphore(1 - nbre_clients); // Allows to block the execution until the echo message

		int connection_port = 6969;
		QueueBroker server = new QueueBroker("server");

		for (int i = 0; i < nbre_clients; i++) {
			QueueBroker client = new QueueBroker("client" + i);
			echo_client(client, connection_port, sm);
			echo_server(server, connection_port);
		}

		sm.acquire(); // Waits the end of the test
		System.out.println("Test 2." + test_number + " done !\n");
	}

	// Echo server with several clients on different ports
	public static void test3(int nbre_clients) throws Exception {
		System.out.println("Test 3 in progress...");
		Semaphore sm = new Semaphore(1 - nbre_clients); // Allows to block the execution until the echo message

		int connection_port = 6969;
		QueueBroker server = new QueueBroker("server");

		for (int i = 0; i < nbre_clients; i++) {
			QueueBroker client = new QueueBroker("client" + i);
			echo_client(client, connection_port + i, sm);
			echo_server(server, connection_port + i);
		}

		sm.acquire(); // Waits the end of the test
		System.out.println("Test 3 done !\n");
	}

	// Test the return statement of method connection
	public static void test4() throws Exception {
		System.out.println("Test 4 in progress...");

		QueueBroker client = new QueueBroker("client");
		int connection_port = 6969;

		// Initialization of server's method tests
		boolean client_connect_test = false;

		client_connect_test = client.connect("server", connection_port, null); // False
		if (client_connect_test)
			throw new Exception("The client tries to connect a not existing broker !");

		QueueBroker server = new QueueBroker("server");

		client_connect_test = client.connect("server", connection_port, null); // True
		if (!client_connect_test)
			throw new Exception("The client doesn't find the broker !");

		// Initialization of client's method tests
		boolean server_bind_test = false;
		boolean server_unbind_test = false;

		server_unbind_test = server.unbind(connection_port); // False
		if (server_unbind_test)
			throw new Exception("The server tries to unbind a not connected port !");

		server_bind_test = server.bind(connection_port, null); // True
		if (!server_bind_test)
			throw new Exception("The server can't bind a connection port !");

		server_bind_test = server.bind(connection_port, null); // False
		if (server_bind_test)
			throw new Exception("The server tries to bind an existing port !");

		server_unbind_test = server.unbind(connection_port); // True
		if (!server_unbind_test)
			throw new Exception("The server can't unbind a connected port !");

		System.out.println("Test 4 done !\n");
	}
}

