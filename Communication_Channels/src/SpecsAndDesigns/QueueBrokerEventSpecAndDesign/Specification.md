# Aim 

The aim of this *eventfull Queue Broker* is to provide a mechanism to allow for the asynchronous communication between different services. 

# QueueBroker

The user can asynchronously bind to another broker. When the connection is established a AcceptListener `accepted` event is triggered.

The user can asynchronously connect to another broker. When the connection is established a ConnectListener `connected` event is triggered. Or if the connection is refused a ConnectListener `refused` event is triggered.re(dfsgè-deft('ders'"))

