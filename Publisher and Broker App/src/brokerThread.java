import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;


public class brokerThread implements Runnable {

    private Socket con;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private ObjectMapper mapper;

    private String message;

    private final static long TIMEOUT = 30000; //30000 millis = 30 sec
    private long timeRequestWasIssued;


    public brokerThread(Socket connection) {
        con = connection;
    }

    public void run() {
        try {
            connect();
            mapper = new ObjectMapper();

            if (in.readUTF().compareTo("pub") == 0) {
                if ((message = in.readUTF()).compareTo("registration") == 0) {
                    acceptPubConnection(new NodeInfo(con.getInetAddress().getHostAddress(), con.getPort()));
                }else if(message.compareTo("Failure")==0){
                    handleFailure();
                }else {
                    publisherUpdate();
                }
            } else {
                Topic t = acceptSubConnection(new NodeInfo(con.getInetAddress().getHostAddress(), con.getPort()));
                if (t != null) {
                    timeRequestWasIssued = System.currentTimeMillis();
                    pull(t);
                }
            }

            disconnect();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connect(){
        try {
            out = new ObjectOutputStream(con.getOutputStream());
            in = new ObjectInputStream(con.getInputStream());
        } catch (IOException e) {
        e.printStackTrace();
        }
    }

    private void disconnect(){
        try {
            in.close();
            out.close();
            con.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void publisherUpdate(){
        Topic t;
        Value v;

        System.out.println(con.getInetAddress().getHostAddress() + " > " + message);
        try {
            t = (Topic) in.readUnshared();
            v = (Value) in.readUnshared();

            addPush(t, v);

            //System.out.println(v);        //DEBUGGING
            //System.out.println();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void acceptPubConnection(NodeInfo pubInfo) {
        try {
            out.writeUTF(Broker.info.getFirst() + "");
            out.writeUTF(Broker.info.getLast() + "");
            out.flush();

            Broker.registeredPublishers.add(pubInfo);

            out.writeUTF("Registration complete. Ready to receive bus location data.");
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Topic acceptSubConnection(NodeInfo info) {
        Topic t = null;

        try {
            int id = Integer.parseInt(in.readUTF());
            System.out.print(info.getIP() + " > " + in.readUTF());
            t = mapper.readValue(in.readUTF(),Topic.class);
            System.out.println(t.getBusLine());

            if (!isRegistered(id)) {
                out.writeUTF("not registered");
                out.flush();

                out.writeUTF(mapper.writeValueAsString(Broker.b));
                out.flush();

                if (!containsTopic(t)) {
                    out.writeUTF("Topic " + t.getBusLine() + " is on another broker.");
                    out.flush();
                    return null;
                } else {
                    Broker.registeredSubscribers.add(new NodeInfo(con.getInetAddress().getHostAddress(), con.getPort()));
                }
            } else {
                out.writeUTF("entry found");
                out.flush();
            }

            if(Broker.disabledTopics.get(correctQueueIndex(t))){
                out.writeUTF("Sensor is down.");
                out.flush();
                return null;
            }else {
                out.writeUTF("Topic located. Standby for updates.");
                out.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return t;
    }

    private int correctQueueIndex(Topic t) {
        int i = 0;

        while (Broker.myTopics.get(i).compareTo(t) != 0) {
            i++;
        }
        return i;
    }

    private void addPush(Topic t, Value v) {
        synchronized (Broker.topicQueues) {
            Broker.topicQueues.get(correctQueueIndex(t)).add(v);
        }
    }

    private boolean containsTopic(Topic t) {
        for (int i = 0; i < Broker.myTopics.size(); i++) {
            if (Broker.myTopics.get(i).compareTo(t) == 0) return true;
        }
        return false;
    }

    private void pull(Topic t) {

            boolean timedOut = false;
            int index = correctQueueIndex(t);
            int lastSize = Broker.topicQueues.get(index).size();
            long timeSinceLastUpdate = timeRequestWasIssued;

            String renewal = "";
            long st;
            while (!timedOut && renewal.compareTo("disconnect") != 0) {

                if ((st = System.currentTimeMillis() - timeSinceLastUpdate) < TIMEOUT) {
                        synchronized(Broker.topicQueues) {

                            if (Broker.disabledTopics.get(index)) {
                                try {
                                    out.writeUTF("Sensor is down.");
                                    out.flush();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                return;
                            }

                            int currentSize = Broker.topicQueues.get(index).size();

                            if (currentSize > lastSize) {
                                try {
                                    out.writeUTF("Sending update...");
                                    out.flush();

                                    out.writeUTF(mapper.writeValueAsString(Broker.topicQueues.get(index).get(currentSize - 1)));
                                    out.flush();

                                    lastSize = currentSize;
                                    renewal = in.readUTF();
                                    timeSinceLastUpdate = System.currentTimeMillis();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        timedOut = true;
                    }


                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(timedOut){
                try {
                    out.writeUTF("TIMEOUT");
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

    }

    //The subscriber's ip would be a better key, but it can't be used when testing with the loopback address
    private boolean isRegistered(int subID) {
        for (int i = 0; i < Broker.registeredSubscribers.size(); i++) {
            if (Broker.registeredSubscribers.get(i).getID() == subID)
                return true;
        }
        return false;
    }

    //Marks the disabled topics so that the broker can respond instantly when a subscriber requests one of them
    private void handleFailure(){
        try {
            Topic t = (Topic) in.readUnshared();

            Broker.disabledTopics.set(correctQueueIndex(t),true);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
