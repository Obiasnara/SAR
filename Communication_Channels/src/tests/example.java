package tests;

import implems.*;
import java.util.ArrayList;

/**
 * Cette classe met en place un test simple de communication entre deux tâches
 * via un broker et des canaux.
 * 
 * - Un brokers est créé : `broker1`.
 * - Task 1 (associée à `broker1`) attend une connexion sur le port 8080 et lit
 * les données reçues via un canal.
 * - Task 2 (associée à `broker1`) se connecte à `broker1` via le port 8080,
 * envoie un message, puis se déconnecte.
 * - Les deux tâches sont exécutées simultanément et une fois la communication
 * terminée, un message indique la fin.
 * 
 * Ce test illustre l'utilisation de l'API pour établir une communication
 * bidirectionnelle entre tâches.
 */
public class example {

    public final static String LOREM_IPSUM = "\n" + //
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

    public static byte[] getMessageSize(int size) {
        byte[] sizeBytes = new byte[4];
        sizeBytes[0] = (byte) (size >> 24);
        sizeBytes[1] = (byte) (size >> 16);
        sizeBytes[2] = (byte) (size >> 8);
        sizeBytes[3] = (byte) size;
        return sizeBytes;
    }

    public static int getSizeFromMessage(byte[] sizeBytes) {
        return (sizeBytes[0] << 24) | (sizeBytes[1] << 16) | (sizeBytes[2] << 8) | sizeBytes[3];
    }

    public static int readMessageSize(Channel channel) {
        byte[] sizeBytes = new byte[4];
        // We need to use a While loop to make sure we read all 4 bytes
        int bytesRead = 0;
        int response = 0;
        while (bytesRead < 4) {
            response = channel.read(sizeBytes, bytesRead, 4 - bytesRead);
            if (response == -1) {
                return -1;
            }
            bytesRead += response;
        }
        return getSizeFromMessage(sizeBytes);
    }

    public static byte[] readSizeAndMessage(Channel channel) {
        int messageSize = readMessageSize(channel);
        if (messageSize <= 0) {
            return null;
        }

        byte[] buffer = new byte[messageSize];
        int bytesRead = 0;

        while (bytesRead < messageSize) {
            int response = channel.read(buffer, bytesRead, messageSize - bytesRead);

            if (response == -1) {
                return null;
            }

            bytesRead += response;
        }

        if (VERBOSE) {
            System.out.println("Received message: " + new String(buffer, 0, buffer.length));
        }

        return buffer;
    }

    public static void writeSizeAndMessage(Channel channel, byte[] message) {
        byte[] sizeBytes = getMessageSize(message.length);
        byte[] buffer = new byte[sizeBytes.length + message.length];
        System.arraycopy(sizeBytes, 0, buffer, 0, sizeBytes.length);
        System.arraycopy(message, 0, buffer, sizeBytes.length, message.length);

        int bytesWritten = 0;
        while (bytesWritten < buffer.length) {
            int response = channel.write(buffer, bytesWritten, buffer.length - bytesWritten);
            if (response == -1) {
                return;
            }
            bytesWritten += response;
        }

        if (VERBOSE) {
            System.out.println("Sent message: " + new String(message, 0, message.length));
        }
    }

    protected static Boolean VERBOSE = true;
    public static void main(String[] args) {
        // Create a new test object
        example test = new example();
        // Run the test
        //test.test1();
        //test.test2();
        //test.test3();

        test.test4();
    }

    protected class EchoServer implements Runnable {
        protected Broker broker;
        protected boolean isAccept;
        protected String brokerName;
        protected int port;

        public EchoServer(Broker broker, boolean isAccept, String brokerName, int port) {
            this.broker = broker;
            this.isAccept = isAccept;
            this.brokerName = brokerName;
            this.port = port;
        }
        @Override
        public void run() {
            try {
                Channel serverChannel;
                int nbMessages = 0;
                if (isAccept) {
                    serverChannel = (Channel) broker.accept(this.port);
                
                    while (nbMessages < 10) {
                        byte[] buffer = readSizeAndMessage(serverChannel);
    
                        writeSizeAndMessage(serverChannel, buffer);
    
                        nbMessages++;
                    }

                } else {
                    serverChannel = (Channel) broker.connect(this.brokerName, this.port);

                    while (nbMessages < 10) {

                        String message = "Broker " + brokerName + " message number " + nbMessages;
                        writeSizeAndMessage(serverChannel, message.getBytes());

                        readSizeAndMessage(serverChannel);
    
                        nbMessages++;
                    }
                }
                
                serverChannel.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }

    public void test1() {
        Broker broker = new Broker("Broker1");

        Task serverTask = new Task(broker, new Runnable() {
            @Override
            public void run() {
                try {
                    // Listen on port 8080 for incoming connections
                    Channel serverChannel = (Channel) broker.accept(8080);
                    if (serverChannel == null) {
                        return;
                    }
                    int nbMessages = 0;

                    while (nbMessages < 10) {
                        // Read message
                        byte[] buffer = readSizeAndMessage(serverChannel);


                        // Echo the message back to the client
                        writeSizeAndMessage(serverChannel, buffer);


                        nbMessages++;
                    }
                    serverChannel.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Task clientTask = new Task(broker, new Runnable() {
            @Override
            public void run() {
                try {
                    // Connect to the server on port 8080
                    Channel clientChannel = (Channel) broker.connect("Broker1", 8080);
                    

                    int nbMessages = 0;

                    while (nbMessages < 10) {
                        String message = "Message " + nbMessages;

                        writeSizeAndMessage(clientChannel, message.getBytes());


                        // Read the message from the channel

                        byte[] echoBuffer = readSizeAndMessage(clientChannel);

                        assert echoBuffer != null;
                        assert new String(echoBuffer, 0, echoBuffer.length).equals(message);
                        assert echoBuffer.length == message.length();

                        nbMessages++;
                    }

                    clientChannel.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            serverTask.join();
            clientTask.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void test2() {
        Broker broker = new Broker("Broker1");
        Broker broker2 = new Broker("Broker2");

        Task serverTask = new Task(broker, new Runnable() {
            @Override
            public void run() {
                try {
                    // Listen on port 8080 for incoming connections
                    Channel serverChannel = (Channel) broker.accept(8080);

                    int nbMessages = 0;

                    while (nbMessages < 10) {
                        // Read message
                        byte[] buffer = readSizeAndMessage(serverChannel);

                        // Echo the message back to the client
                        writeSizeAndMessage(serverChannel, buffer);

                        nbMessages++;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Task clientTask = new Task(broker2, new Runnable() {
            @Override
            public void run() {
                try {
                    // Connect to the server on port 8080
                    Channel clientChannel = (Channel) broker2.connect("Broker1", 8080);
                    

                    int nbMessages = 0;

                    while (nbMessages < 10) {
                        String message = "Message " + nbMessages;

                        writeSizeAndMessage(clientChannel, message.getBytes());

                        // Read the message from the channel

                        byte[] echoBuffer = readSizeAndMessage(clientChannel);

                        assert echoBuffer != null;
                        assert new String(echoBuffer, 0, echoBuffer.length).equals(message);
                        assert echoBuffer.length == message.length();

                        nbMessages++;
                    }

                    clientChannel.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            serverTask.join();
            clientTask.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void test3() {
        ArrayList<Broker> brokers = new ArrayList<Broker>();
        ArrayList<Task> tasks = new ArrayList<Task>();

        for (int i = 0; i < 10; i++) {
            Broker broker = new Broker("Broker" + i);
            brokers.add(broker);
            Task serverTask = new Task(broker, new EchoServer(broker, true, "Broker" + i, i));
            Task clientTask = new Task(broker, new EchoServer(broker, false, "Broker" + i, i));
            tasks.add(serverTask);
            tasks.add(clientTask);
        }

        for (Task task : tasks) {
            try {
                task.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void test4() {
        Broker broker = new Broker("Broker1");

        Task serverTask = new Task(broker, new Runnable() {
            @Override
            public void run() {
                try {
                    // Listen on port 8080 for incoming connections
                    Channel serverChannel = (Channel) broker.accept(8080);

                    int nbMessages = 0;
                    try {
                        while (nbMessages < 10) {
                            // Read message
                            byte[] buffer = readSizeAndMessage(serverChannel);

                            // Echo the message back to the client
                            writeSizeAndMessage(serverChannel, buffer);

                            if (nbMessages == 5) {
                                serverChannel.disconnect();
                            }

                            nbMessages++;
                        }  
                    } catch (IllegalStateException e) {
                        if (VERBOSE) {
                            System.out.println("Server disconnected");
                        }
                        if(nbMessages != 6) {
                            throw e;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Task clientTask = new Task(broker, new Runnable() {
            @Override
            public void run() {
                try {
                    // Connect to the server on port 8080
                    Channel clientChannel = (Channel) broker.connect("Broker1", 8080);
                    

                    int nbMessages = 0;

                    while (nbMessages < 6) {
                        String message = "Message " + nbMessages;

                        writeSizeAndMessage(clientChannel, message.getBytes());

                        // Read the message from the channel

                        byte[] echoBuffer = readSizeAndMessage(clientChannel);

                        assert echoBuffer != null;
                        assert new String(echoBuffer, 0, echoBuffer.length).equals(message);
                        assert echoBuffer.length == message.length();

                        nbMessages++;
                    }

                    clientChannel.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            serverTask.join();
            clientTask.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
