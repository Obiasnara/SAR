package tests;

import implems.*;

/**
 * Cette classe met en place un test simple de communication entre deux tâches via un broker et des canaux.
 * 
 * - Un brokers est créé : `broker1`.
 * - Task 1 (associée à `broker1`) attend une connexion sur le port 8080 et lit les données reçues via un canal.
 * - Task 2 (associée à `broker1`) se connecte à `broker1` via le port 8080, envoie un message, puis se déconnecte.
 * - Les deux tâches sont exécutées simultanément et une fois la communication terminée, un message indique la fin.
 * 
 * Ce test illustre l'utilisation de l'API pour établir une communication bidirectionnelle entre tâches.
 */
public class example {

	public static void main(String[] args) {
       
        BrokerImplem broker1 = new BrokerImplem("Broker1");
       
        TaskImplem task1 = new TaskImplem(broker1, new Runnable() {
            @Override
            public void run() {
                ChannelImplem ChannelImplem = (ChannelImplem) broker1.accept(8080); 
                byte[] buffer = new byte[1024];
                int bytesRead = ChannelImplem.read(buffer, 0, buffer.length);
                System.out.println("Task 1 received: " + new String(buffer, 0, bytesRead));
                ChannelImplem.disconnect();
            }
        });

        TaskImplem task2 = new TaskImplem(broker1, new Runnable() {
            @Override
            public void run() {
                ChannelImplem ChannelImplem = (ChannelImplem) broker1.connect("Broker1", 8080);
                String message = "Hello from Task 2";
                ChannelImplem.write(message.getBytes(), 0, message.length());
                ChannelImplem.disconnect();
            }
        });

        task1.start();
        task2.start();

        try {
            task1.join();  
            task2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Communication complete.");
    }

}
