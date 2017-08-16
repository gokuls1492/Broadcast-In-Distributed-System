/*
This class stores all the node related information

*/

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by gokul on 6/16/2017.
 */
public class Node {
    private int nodeId;
    private String hostName;
    private int port;
    private ArrayList<Integer> neighborList = new ArrayList<Integer>();
    static ArrayList<Integer> treeNeighbors = new ArrayList<Integer>();     //is used to store neighbors ffor each node
    static ConcurrentHashMap<Integer, RootParent> rootParentMap = new ConcurrentHashMap<Integer, RootParent>();
    static int IsVisited =0;             // to keep track of whether node previously visited while traversing
    static int sum = 0;                  //to track whether all broadcasted messages are received correctly
                                         // If successful - sum values at all node must equal

    public Node(){
        this.nodeId=0;
        this.hostName=null;
        this.port=0;
        this.neighborList=null;
    }

    public Node(int id, String name, int port, ArrayList<Integer> neighbors){
        this.nodeId=id;
        this.hostName=name;
        this.port=port;
        this.neighborList = neighbors;
    }


    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ArrayList<Integer> getNeighborList() {
        return neighborList;
    }

    public void setNeighborList(ArrayList<Integer> neighborList) {
        this.neighborList = neighborList;
    }

}
