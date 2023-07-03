import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Publisher implements Node{
    private ArrayList<Topic> myTopics;
    private ArrayList<Integer> topicLineCodes ;
    private HashMap<Integer, NodeInfo>  brokerTopics;
    private HashMap<Integer, LineInfo> myLines;

    private Socket con;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private NodeInfo receiver;


    public Publisher(String ip, int port){
        info.setIP(ip);
        info.setPort(port);

        b.add(new NodeInfo(0,"127.0.0.1",5050));
        b.add(new NodeInfo(1,"127.0.0.1",5051));
        b.add(new NodeInfo(2,"127.0.0.1",5052));
        b.add(new NodeInfo(3,"127.0.0.1",5053));


        myTopics = new ArrayList<>();
        topicLineCodes = new ArrayList<>();
        brokerTopics = new HashMap<>();
        myLines = new HashMap<>();
    }


    //Works with one publisher if the correct substrings are removed from the file names.
    public void init(int part){ //part can only be 1 or 2 at the moment
            loadTopics("./textFiles/busLinesNew-" + part +"of2.txt");

            loadLineInformation("./textFiles/RouteCodesNew-" + part +"of2.txt");

            System.out.print("My busLines are:");
            for(int i =0; i<myTopics.size();i++)
                System.out.print(" " + myTopics.get(i).getBusLine());

            System.out.println();

            System.out.println("Acquiring update from brokers...");

            getBrokerList();

            System.out.println("\nPushing data...");

            startSending("./textFiles/busPositionsNew-" + part + "of2.txt");

            System.out.println("Exiting");

        }

    public void connect(){
        try {
            con = new Socket(receiver.getIP(), receiver.getPort());
            out = new ObjectOutputStream(con.getOutputStream());
            in = new ObjectInputStream(con.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            out.close();
            in.close();
            con.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getBrokerList(){

        for(int i =0; i<b.size();i++) {
            try {
                receiver = b.get(i);

                connect();

                out.writeUTF("pub");
                out.writeUTF("registration");
                out.flush();

                int firstTopic = Integer.parseInt(in.readUTF());
                int lastTopic = Integer.parseInt(in.readUTF());

                System.out.println("Broker " + b.get(i).getIP() + " > " + in.readUTF());

                disconnect();

                b.get(i).setFirst(firstTopic);
                b.get(i).setLast(lastTopic);

                if(firstTopic > lastTopic) {
                    for (int j = firstTopic; j < 32; j++)
                        brokerTopics.put(j, b.get(i));
                    for (int j = lastTopic; j >= 0; j--)
                        brokerTopics.put(j, b.get(i));
                }else {
                    for (int j = firstTopic; j <= lastTopic; j++) {
                        brokerTopics.put(j, b.get(i));
                    }
                }

                System.out.println("Broker number " + b.get(i).getID() + "\nHashed topic range: " + b.get(i).getFirst() + " - " + b.get(i).getLast());
                System.out.println();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int hashTopic(Topic t){
        Hashing h = new Hashing();

        return h.getIntValue(h.hashMD5(t.getBusLine()))%32;
    }

    private void push(Topic t, Value v){
        receiver = brokerTopics.get(hashTopic(t));

        try {
            connect();

            out.writeUTF("pub");
            out.flush();

            out.writeUTF("Topic update for busLine " + t.getBusLine());
            out.flush();

            out.writeObject(t);
            out.flush();

            out.writeObject(v);
            out.flush();

            disconnect();
        } catch (IOException e) {
            notifyFailure();
            e.printStackTrace();
        }

    }

    private void notifyFailure(){

        for(int i = 0; i<myTopics.size();i++){
            receiver = brokerTopics.get(hashTopic(myTopics.get(i)));

            try{
                connect();

                out.writeUTF("pub");
                out.flush();

                out.writeUTF("Failure");
                out.flush();

                out.writeObject(myTopics.get(i));
                out.flush();

                disconnect();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadTopics(String f){
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String str;
            String[] tokens;
            Topic t;

            while((str=br.readLine())!=null){
                tokens = str.split(",");

                t = new Topic(tokens[1]);

                topicLineCodes.add(Integer.parseInt(tokens[0]));
                myTopics.add(t);

            }

            br.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLineInformation(String f){
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String str;
            String[] tokens;
            LineInfo LI;

            while((str = br.readLine())!=null){
                tokens = str.split(",");

                LI = new LineInfo(tokens[3],Integer.parseInt(tokens[2]));

                myLines.put(Integer.parseInt(tokens[0]),LI);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startSending(String f) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String str;
            String[] tokens;
            Value v;
            Bus bus;
            String lineName;
            String busLineId;


            while((str=br.readLine())!=null) {

                tokens = str.split(",");

                lineName = myLines.get(Integer.parseInt(tokens[1])).getLineName();
                busLineId = myTopics.get(topicLineCodes.indexOf(Integer.parseInt(tokens[0]))).getBusLine();
                bus = new Bus(tokens[0],tokens[1],tokens[2],lineName,busLineId,tokens[5]);

                v = new Value(bus,Double.parseDouble(tokens[3]),Double.parseDouble(tokens[4]));

                push(new Topic(busLineId),v);

                Thread.sleep(1000);
            }

            notifyFailure();



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            notifyFailure();
            e.printStackTrace();
        } catch (InterruptedException e) {
            notifyFailure();
            e.printStackTrace();
        }

    }
}
