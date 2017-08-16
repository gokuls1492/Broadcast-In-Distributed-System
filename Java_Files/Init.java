/*
This is a main class
    - Accepts node id as argument
    - Calls function to parse config file
    - Each node starts its server which initiate spanning tree construction
    - Displays its neighboring nodes
    - Finally, initiates broadcast operation for each node
*/
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.Random;

/**
 * Created by gokul on 6/22/2017.
 */
public class Init {
    static int serial = 1;
    static int myNodeId;
    static Thread serverThread;
    static String initHost = null;
    static int root = 0;

    public static void main(String[] args) {
        myNodeId = Integer.parseInt(args[0]);           //Current node id
        Random rand = new Random();

        Configuration.readFile();                       //Config file is parsed and values stored in variables

                                                        //Initiates Server thread for each node
        serverThread = new Thread(new ServerSCTP(myNodeId));
        serverThread.start();
                                                        //Waits for spanning tree construction
        try{
            Thread.sleep(3000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
                                                        //Prints neighbors for each node
        System.out.println("\n"+"Neighbors of Node: "+ myNodeId);
        for (int i=0; i< Node.treeNeighbors.size(); i++){
            System.out.println(Node.treeNeighbors.get(i));
        }

        try{
            Thread.sleep(4000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        RootParent rootParent = new RootParent();
        root = myNodeId;
        int random = rand.nextInt(100);                 //Random no. generated at each node
                                                        //Starts broadcast operation at each node & transmits random no. along with it
        try {
            for (Map.Entry<Integer,Node> pair : Configuration.nodeHashMap.entrySet()) {
                initHost = pair.getValue().getHostName()+".utdallas.edu";
                if (initHost.equalsIgnoreCase(InetAddress.getLocalHost().getHostName())) {
                    Node.rootParentMap.put(root,rootParent);
                    new Client(myNodeId).beginBroadCast(myNodeId, 0, root, random);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
                                                        //Waits for broadcast to complete
        try{
            Thread.sleep(6000);
        }catch (InterruptedException e){
            e.printStackTrace();;
        }
                                                        //Display sum of random no. broadcasted at each node
        System.out.println("\n"+"Total sum at "+myNodeId+" is: "+Node.sum);
    }
}
