/*
Client class used to create connection with the server using SCTP
Used to send 'REQ_MSG' for creating spanning tree
and 'BROADCAST_MSG' to its corresponding children
*/

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * Created by gokul on 6/25/2017.
 */
public class Client implements Runnable{
    private static final int MSG_SIZE = 1000;
    private int nodeId = 0, port=0, nId=0;
    private String hostName = null;
    private ArrayList<Integer> connectedList = new ArrayList<Integer>();
    private Charset charset = Charset.forName("ISO-8859-1");
    private CharsetEncoder encoder = charset.newEncoder();
    private CharBuffer cbuf = CharBuffer.allocate(100);
    private String message = null;
    private ByteBuffer byteBufferSend = ByteBuffer.allocate(MSG_SIZE);

    public Client(int id){
        this.nodeId = id;
    }

    //This block handles spanning tree construction by sending request to all its neighboring nodes
    public void run(){
        for (Map.Entry<Integer,Node> pair : Configuration.nodeHashMap.entrySet()){
            if (pair.getValue().getNodeId() == nodeId){
                connectedList = pair.getValue().getNeighborList();
            }
        }

//        ByteBuffer byteBufferRecv  = ByteBuffer.allocate(MSG_SIZE);
        CharBuffer cbuf = CharBuffer.allocate(100);
        message = null;
        try {
            for (int i=0; i<connectedList.size(); i++) {
                nId = connectedList.get(i);
                hostName = Configuration.hostNames.get(nId-1);
                port = Configuration.portList.get(nId-1);
                message = nodeId+"REQ_MSG";
                //Send Message
                sendMessage(hostName,port,byteBufferSend, message);
                //System.out.println(" ");
                //System.out.println(message+" sent from "+nodeId+" to "+nId);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //This block handles sending msg to to specific destination using SCTP
    public void sendMessage(String host, int port, ByteBuffer byteBufferSend, String msg) {
        try {
            InetSocketAddress socketAddress = new InetSocketAddress(host, port);
            SctpChannel sctpChannel = SctpChannel.open(socketAddress,0,0);
            //sctpChannel.bind(new InetSocketAddress(port+1));
            //sctpChannel.connect(socketAddress);
            MessageInfo messageInfo = MessageInfo.createOutgoing(null, 0);
            cbuf.clear();
            cbuf.put(msg.toCharArray()).flip();
            encoder.encode(cbuf, byteBufferSend, true);
            byteBufferSend.flip();
            sctpChannel.send(byteBufferSend, messageInfo);
            byteBufferSend.clear();
            cbuf.clear();
            sctpChannel.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //This block handles broadcast operation by sending msg to all its neighbors
    public void beginBroadCast(int nodeId, int parent, int root, int random){
        Node.sum += random;
        for (Integer node : Node.treeNeighbors){
            if (node != parent) {
                hostName = Configuration.hostNames.get(node - 1);
                port = Configuration.portList.get(node - 1);
                message = nodeId + "BROADCAST_MSG" + root + "*" + random;
                //Send BroadCast Message
                sendMessage(hostName, port, byteBufferSend, message);
                //System.out.println(" ");
                System.out.println("\n"+"BROADCAST_MSG sent from " + nodeId + " to " + node + " with root: "+ root);
            }
        }
    }
}