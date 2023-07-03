import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Broker implements Node{

    static ArrayList<NodeInfo> registeredPublishers;
    static ArrayList<NodeInfo> registeredSubscribers;
    static ArrayList<Topic> myTopics;
    static ArrayList<Boolean> disabledTopics;
    static ArrayList<ArrayList<Value>> topicQueues;


    private ServerSocket srv = null;
    private Socket con = null;

    public Broker() {
    }

    public Broker(int id, String ip, int port){
        info.setID(id);
        info.setIP(ip);
        info.setPort(port);

        registeredPublishers = new ArrayList<>();
        registeredSubscribers = new ArrayList<>();
        myTopics = new ArrayList<>();
        disabledTopics = new ArrayList<>();
        topicQueues = new ArrayList<>();

        b.add(new NodeInfo(0,"127.0.0.1",5050));
        b.add(new NodeInfo(1,"127.0.0.1",5051));
        b.add(new NodeInfo(2,"127.0.0.1",5052));
        b.add(new NodeInfo(3,"127.0.0.1",5053));

    }


    public void init(int test){
        try {
            calculateKeys();
            System.out.print("My busLines are:");
            for(int i =0; i<myTopics.size();i++)
                System.out.print(" " + myTopics.get(i).getBusLine());

            System.out.println();

            srv = new ServerSocket(info.getPort());


            while(true){ //Brokers are supposedly always online
                connect();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect(){
        try {
            con = srv.accept();
            new Thread(new brokerThread(con)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect(){}

    private void calculateKeys(){
        Hashing h = new Hashing();
        int[] hashIDs = new int[b.size()];
        int firstTopic;
        int lastTopic;


        int max =0;
        int min = 32;

        for(int i=0; i<b.size();i++){
            hashIDs[i] = h.getIntValue(h.hashMD5(b.get(i).getIP()+b.get(i).getPort())) % 32;
            if (hashIDs[i]>max) max = hashIDs[i];
            if (hashIDs[i]<min) min = hashIDs[i];
            //System.out.println(hashIDs[i]);
        }


        for(int j=0; j<hashIDs.length;j++) {
            lastTopic = hashIDs[j];
            b.get(j).setLast(lastTopic);

            if (!(hashIDs[j] == min)) {
                int mix = 0;
                for (int i = 0; i < hashIDs.length; i++) {
                    if (hashIDs[i] > mix && hashIDs[i] < hashIDs[j])
                        mix = hashIDs[i];
                }
                firstTopic = mix +1;
            }else {
                firstTopic = max + 1;
            }

            b.get(j).setFirst(firstTopic);


            if(j==info.getID()){
                info.setFirst(firstTopic);
                info.setLast(lastTopic);
            }

        }

        //System.out.println(firstTopic + " " + lastTopic);

        determineTopics("./textFiles/busLinesNew.txt",h);


        for(int i=0; i<myTopics.size();i++){
            topicQueues.add(new ArrayList<Value>());
            //System.out.println(myTopics.get(i).getBusLine());

        }
        //System.out.println(topicQueues.size());




    }

    private void determineTopics(String f, Hashing h){

        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String str;
            String[] tokens;
            int hash;


            while((str=br.readLine())!=null) {
                tokens = str.split(",");

                Topic t;


                hash = h.getIntValue(h.hashMD5(tokens[1])) % 32;
                if (info.getLast() < info.getFirst()) {
                    if (hash >= info.getFirst() || hash <= info.getLast()) {
                        t = new Topic(tokens[1]);
                        if (!duplicateTopic(myTopics, t)) {
                            myTopics.add(t);
                        }
                    }
                } else {
                    if (hash <= info.getLast() && hash >= info.getFirst()) {
                        t = new Topic(tokens[1]);
                        if (!duplicateTopic(myTopics, t)) {
                            myTopics.add(t);
                        }
                    }
                }



            }

            br.close();

            for(int i=0; i<myTopics.size();i++)
                disabledTopics.add(false);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean duplicateTopic(ArrayList<Topic> list, Topic t){
        for(int i=0; i<list.size();i++){
            if(list.get(i).compareTo(t)==0) return true;
        }
        return false;
    }
}

