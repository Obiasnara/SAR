package tests;

import abstracts.thread_queue.QueueBrokerAbstract;
import abstracts.thread_queue.QueueChannelAbstract;
import implems.BrokerManager;
import implems.thread_queue.QueueBroker;
import implems.thread_queue.Task;

public class Tests_romain_rosano {

    protected static final String CLIENT_MESSAGE = "\n" + //
            "\n" + //
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed id purus eu est viverra euismod ac sit amet est. Duis tincidunt aliquam condimentum. Donec a aliquam arcu, in gravida ligula. Quisque auctor nisi quam, vitae molestie risus consectetur in. Donec lacinia, velit cursus vulputate laoreet, ante magna consequat dolor, in ornare lectus lacus molestie est. Fusce in sodales nisl. Nullam a augue aliquet, accumsan nibh vitae, viverra nulla. Curabitur molestie libero id iaculis aliquet. Nulla facilisi. Integer ut pharetra est. Vestibulum molestie suscipit elementum. Sed interdum dui eros, in euismod erat suscipit eu.\n"
            + //
            "\n" + //
            "In sit amet accumsan justo. Quisque in nunc diam. Fusce non semper massa, non vestibulum mauris. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Vivamus rhoncus viverra tellus. Aliquam tincidunt augue tortor, id lacinia velit luctus in. Donec pretium ligula at posuere interdum. Nullam eros nisl, lacinia tincidunt mauris ac, sollicitudin aliquam ex. Morbi tempor tempor neque sed finibus. Vestibulum eget tortor sollicitudin, vestibulum ex elementum, tristique nunc. Ut congue lacinia lacus, vitae pretium mauris ultricies sit amet. Praesent faucibus venenatis tortor ac interdum. Quisque vel tincidunt neque. Ut eget efficitur massa.\n"
            + //
            "\n" + //
            "Nunc eget libero nec orci mollis convallis maximus et lectus. Donec ut malesuada mauris, at sollicitudin velit. Etiam et ex commodo, interdum enim et, faucibus arcu. Cras mollis mi massa, vitae efficitur mi ultrices vel. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Morbi aliquam lectus vel sem pharetra facilisis. Nullam eget nisl at ex tempor suscipit. Vestibulum viverra orci id sapien luctus, nec faucibus lectus sagittis. Aenean maximus enim at dolor mollis, non tincidunt purus lacinia. Aliquam aliquam nisi felis. Curabitur elit arcu, fringilla non augue vitae, tempus ornare metus. Integer condimentum massa nec odio gravida scelerisque. Integer scelerisque vitae magna nec imperdiet.\n"
            + //
            "\n" + //
            "Suspendisse libero nisi, efficitur vel mauris vel, mollis fermentum diam. Nam accumsan lectus vitae tincidunt finibus. In tempus interdum arcu. Fusce dignissim venenatis ante id scelerisque. Integer finibus tristique lectus in feugiat. In sed sollicitudin metus, in tempor ligula. In commodo, nisi ut pellentesque convallis, arcu ligula ultricies metus, in faucibus odio quam non nisl. In nunc enim, scelerisque sollicitudin mattis id, ullamcorper nec dui. Mauris gravida mollis neque vitae facilisis. Donec nunc mi, condimentum nec metus in, congue hendrerit magna. Cras nunc eros, porttitor ut turpis in, hendrerit congue diam. Etiam id bibendum sem. Donec tempus erat at arcu maximus, id consequat nibh mattis. Cras diam orci, interdum at accumsan ut, fringilla vitae justo. Sed at efficitur eros, in volutpat odio.\n"
            + //
            "\n" + //
            "Pellentesque ornare est at quam rutrum, efficitur vehicula nibh convallis. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam eleifend est viverra odio convallis eleifend. Vivamus nec ipsum vitae nisl efficitur fermentum sit amet nec nisl. Sed eget eros blandit, semper turpis sit amet, faucibus felis. Vestibulum molestie, lacus in blandit lacinia, nulla odio viverra mi, aliquam aliquam est turpis ullamcorper magna. Donec erat diam, vehicula a magna et, finibus commodo velit. Praesent tempus aliquet sem. Curabitur a scelerisque odio. ";


    public static void main(String[] args) {
        try {
            BrokerManager.getInstance().removeAllBrokers();
            test1();
            BrokerManager.getInstance().removeAllBrokers();
            test2(8080, 1, 1);
            BrokerManager.getInstance().removeAllBrokers();
            test2(67294, 5, 2);
            BrokerManager.getInstance().removeAllBrokers();
            test3();
            BrokerManager.getInstance().removeAllBrokers();
            test4(true);
            BrokerManager.getInstance().removeAllBrokers();
            test4(false);
            System.out.println("All tests have been done successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("The test has failed: " + e.getMessage());
        }
    }

    // Connection test
    public static void test1() throws Exception {
        System.out.println("Test 1 in progress...");

        QueueBrokerAbstract clientBroker = new QueueBroker("Client");
        QueueBrokerAbstract clientQueueBroker = (QueueBrokerAbstract) clientBroker;
        Task clientTask = new Task(clientQueueBroker, new Runnable() {
            @Override
            public void run() {
                try {
                    QueueChannelAbstract mq = clientQueueBroker.connect("Server", 6969);
                    mq.close();

                    if (!mq.closed() || !clientQueueBroker.name().equals("Client")) {
                        throw new Exception("Channel closed or wrong client name.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        });

        QueueBrokerAbstract serverBroker = new QueueBroker("Server");
        QueueBrokerAbstract serverQueueBroker = (QueueBrokerAbstract) serverBroker;
        Task serverTask = new Task(serverQueueBroker, new Runnable() {
            @Override
            public void run() {
                try {
                    QueueChannelAbstract mq = serverQueueBroker.accept(6969);
                    Thread.sleep(500);
                    mq.close();

                    if (!mq.closed() || !serverQueueBroker.name().equals("Server")) {
                        throw new Exception("Channel closed or wrong server name.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        });

        clientTask.join();
        serverTask.join();

        System.out.println("Test 1 done!\n");
    }

    // Simple echo message
    private static void test2(int port, int times, int testInstance) throws Exception {
        System.out.println("Test 2." + testInstance + " in progress...");

        QueueBrokerAbstract clientBroker = new QueueBroker("Client");
        QueueBrokerAbstract clientQueueBroker = (QueueBrokerAbstract) clientBroker;
        Task clientTask = new Task(clientQueueBroker,
                clientRunnable(clientQueueBroker, port, "Server", CLIENT_MESSAGE, times, true));

        QueueBrokerAbstract serverBroker = new QueueBroker("Server");
        QueueBrokerAbstract serverQueueBroker = (QueueBrokerAbstract) serverBroker;
        Task serverTask = new Task(serverQueueBroker, serverRunnable(serverQueueBroker, port, times));

        clientTask.join();
        serverTask.join();

        System.out.println("Test 2." + testInstance + " done!\n");
    }

    // Create different clients on different servers
    private static void test3() throws Exception {
        System.out.println("Test 3 in progress...");
        int clientCount = 1;
        Task[] tasks = new Task[clientCount * 2];
        int port = 10000;
        int times = 10;

        for (int i = 0; i < clientCount; i++) {
        	QueueBrokerAbstract clientBroker = new QueueBroker("Client " + (i + 1));
            QueueBrokerAbstract clientQueueBroker = (QueueBrokerAbstract) clientBroker;
            tasks[2 * i] = new Task(clientQueueBroker,
                    clientRunnable(clientQueueBroker, port + i, "Server " + (i + 1), CLIENT_MESSAGE, times, true));

            QueueBrokerAbstract serverBroker = new QueueBroker("Server " + (i + 1));
            QueueBrokerAbstract serverQueueBroker = (QueueBrokerAbstract) serverBroker;
            tasks[2 * i + 1] = new Task(serverQueueBroker, serverRunnable(serverQueueBroker, port + i, times));
        }

        for (Task task : tasks) {
            task.join();
        }

        System.out.println("Test 3 done!\n");
    }

    // Overload a server with multiple clients
    private static void test4(boolean waitForEcho) throws Exception {
        System.out.println("Test 4 in progress...");
        int clientCount = 10;
        Task[] tasks = new Task[clientCount];
        int port = 1234;

        QueueBrokerAbstract serverBroker = new QueueBroker("Server");
        QueueBrokerAbstract serverQueueBroker = (QueueBrokerAbstract) serverBroker;
        Task serverTask = new Task(serverQueueBroker, realServerRunnable(serverQueueBroker, port));

        for (int i = 0; i < clientCount; i++) {
        	QueueBrokerAbstract clientBroker = new QueueBroker("Client " + i);
            QueueBrokerAbstract clientQueueBroker = (QueueBrokerAbstract) clientBroker;
            tasks[i] = new Task(clientQueueBroker,
                    clientRunnable(clientQueueBroker, port, "Server", CLIENT_MESSAGE, 1, waitForEcho));
        }

        for (Task task : tasks) {
            task.join();
        }

        serverTask.interrupt();

        System.out.println("Test 4 done!\n");
    }

    private static Runnable clientRunnable(QueueBrokerAbstract queueBroker, int port, String receiverName, String message,
                                           int times, boolean waitForEcho) {
        return () -> {
            QueueChannelAbstract mq = null;
            try {
                mq = queueBroker.connect(receiverName, port);
                for (int i = 0; i < times; i++) {
                    String msg = message + (i + 1);
                    byte[] byteMessage = msg.getBytes();
                    mq.send(byteMessage, 0, byteMessage.length);

                    if (waitForEcho) {
                        byte[] received = mq.receive();
                        String echo = new String(received);

                        if (!msg.equals(echo)) {
                            throw new Exception("Messages do not match: Client's message: " + msg + " Echo message: " + echo);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            } finally {
                if (mq != null) mq.close();
            }
        };
    }

    private static Runnable serverRunnable(QueueBrokerAbstract queueBroker, int port, int times) {
        return () -> {
            QueueChannelAbstract mq = null;
            try {
                mq = queueBroker.accept(port);
                for (int i = 0; i < times; i++) {
                    byte[] received = mq.receive();
                    mq.send(received, 0, received.length);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            } finally {
                if (mq != null) mq.close();
            }
        };
    }

    private static Runnable realServerRunnable(QueueBrokerAbstract queueBroker, int port) {
        return () -> {
            QueueChannelAbstract mq = null;
            while (true) {
                try {
                    mq = queueBroker.accept(port);
                    byte[] received = mq.receive();
                    mq.send(received, 0, received.length);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                } finally {
                    if (mq != null) mq.close();
                }
            }
        };
    }
}
