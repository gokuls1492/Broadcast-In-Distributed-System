/*Server class which opens at server port and waits for client to connect
Node 1 initiates the spanning tree construction*/

import java.nio.ByteBuffer;
import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;

import java.net.InetSocketAddress;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by gokul on 6/24/2017.
 */
public class ServerSCTP implements Runnable {
    private static final int MSG_SIZE = 1000;
    private Charset charset = Charset.forName("ISO-8859-1");
    private CharsetDecoder decoder = charset.newDecoder();
    private int serverPort=0, nodeId=0, root=0, cNCK=0;
    private String hostName = null, send = null, rootTemp= null;
    private String msg = null, temp = null;
    private String pHost = null;
    private int pPort = 0, ackBC=0, senderId =0, parentId=0, digit;
    Random rand = new Random();

    public ServerSCTP(int id){
        nodeId = id;
    }

    @Override
    public void run(){
        for (Map.Entry<Integer,Node> pair : Configuration.nodeHashMap.entrySet()){
            if (pair.getKey() == nodeId){
                serverPort = pair.getValue().getPort();
                hostName = pair.getValue().getHostName();
                System.out.println("Server: "+ hostName+" listening at "+serverPort);
                break;
            }
        }

        try{
            Client client = new Client(nodeId);
            //Open server channel and bind port to the channel
            SctpServerChannel serverChannel = SctpServerChannel.open();
            InetSocketAddress inetSockAddress = new InetSocketAddress(serverPort);
            serverChannel.bind(inetSockAddress);

            String recvMsg = null;
            ByteBuffer byteBufferRecv = ByteBuffer.allocate(MSG_SIZE);
            ByteBuffer byteBufferSend = ByteBuffer.allocate(MSG_SIZE);

            Charset charset = Charset.forName("ISO-8859-1");
            CharsetEncoder encoder = charset.newEncoder();

            Thread.sleep(1000);
            //For node 1 : Start client & initiate spanning tree proc
            if (Init.myNodeId == Init.serial){
                Node.IsVisited =1;
                new Thread(new Client(Init.myNodeId)).start();
            }
            //System.out.println("Waiting for Connection"+ Init.myNodeId);
            while(true) {
                //Server starts accepting when the port is opened
                SctpChannel sctpChannel = serverChannel.accept();

                //Message will be received with binded information
                MessageInfo messageInfo = sctpChannel.receive(byteBufferRecv, null, null);

                byteBufferRecv.flip();
                if (byteBufferRecv.remaining() > 0) {  // actual length of received packet
                    recvMsg = decoder.decode(byteBufferRecv).toString();
                }
                byteBufferRecv.clear();
                CharBuffer cbuf = CharBuffer.allocate(100);
                RootParent rootParent = new RootParent();

                //Extract sender and root id from received msg
                send = new Scanner(recvMsg).useDelimiter("[^\\d]+").next();
                senderId = Integer.parseInt(send);
                recvMsg = recvMsg.substring(send.length());
                if( new Scanner(recvMsg).useDelimiter("[^\\d]+").hasNextInt()){
                    rootTemp = new Scanner(recvMsg).useDelimiter("[^\\d]+").next();
                    root = Integer.parseInt(rootTemp);
                    if(recvMsg.contains("*"))
                        digit = Integer.parseInt(recvMsg.substring(recvMsg.lastIndexOf("*") + 1));
                    recvMsg = recvMsg.replaceAll("[0-9*]","");
                }

                //This block handles spanning tree construction
                if (recvMsg != null){
                    if (recvMsg.equalsIgnoreCase("REQ_MSG")) {
                        if (Node.IsVisited == 0) {
                            Node.IsVisited = 1;
                            //System.out.println("For node:"+nodeId+" Adding node:"+ senderId);
                            Node.treeNeighbors.add(senderId);
                            msg = nodeId + "ACK_MSG";
                            byteBufferSend.clear();
                            pHost = Configuration.hostNames.get(senderId-1);
                            pPort = Configuration.portList.get(senderId-1);
                            client.sendMessage(pHost, pPort, byteBufferSend, msg);
                            new Thread(new Client(nodeId)).start();

                        } else if (Node.IsVisited == 1) {
                            //Send NACK if node has already been visited
                            msg = nodeId + "NACK_MSG";
                            //System.out.println(msg + " sent from " + nodeId + " to "+ senderId);
                            byteBufferSend.clear();
                            pHost = Configuration.hostNames.get(senderId-1);
                            pPort = Configuration.portList.get(senderId-1);
                            client.sendMessage(pHost, pPort, byteBufferSend, msg);
                        }
                    }

                    //Associates with the node as parent
                    else if (recvMsg.equalsIgnoreCase("ACK_MSG")) {
                        if (! Node.treeNeighbors.contains(senderId)) {
                            //System.out.println("For node:"+nodeId+"Adding node:"+ senderId);
                            Node.treeNeighbors.add(senderId);
                         //   Node.treeChildren.add(senderId);
                        }

                    }

                    //If node has already been associated with parent
                    else if (recvMsg.equalsIgnoreCase("NACK_MSG")) {
                        cNCK++;
                        //System.out.println("NACK message received from " + senderId+" to "+nodeId);
                    }

                    //This block begins Broadcast
                    else if (recvMsg.equalsIgnoreCase("BROADCAST_MSG")) {
                        System.out.println("\n"+"BROADCAST_MSG received at "+nodeId+" from "+senderId+" with root: "+ root);
                        //parentId = senderId;
                        rootParent.setParentId(senderId);
                        rootParent.setRoot(root);
                        Node.rootParentMap.put(root, rootParent);
                        if (Node.treeNeighbors.size() > 1) {
                            client.beginBroadCast(nodeId, rootParent.getParentId(), rootParent.getRoot(), digit);
                        } else {
                            Node.sum += digit;
                            msg = nodeId + "BROADCAST_ACK" + rootParent.getRoot();
                            System.out.println("\n"+"BROADCAST_ACK sent from: "+nodeId+" to: "+rootParent.getParentId()+" with root: "+ root);
                            pHost = Configuration.hostNames.get(rootParent.getParentId() - 1);
                            pPort = Configuration.portList.get(rootParent.getParentId() - 1);
                            client.sendMessage(pHost, pPort, byteBufferSend, msg);
                        }
                    }

                    //This block handles Converge Casting
                    //Map used for handling concurrent broadcast operation
                    else if (recvMsg.equalsIgnoreCase("BROADCAST_ACK")) {
                        System.out.println("\n"+"BROADCAST_ACK received at: "+nodeId+" from: "+senderId+" with root: "+root);
                        for (Map.Entry<Integer,RootParent> pair : Node.rootParentMap.entrySet()){
                            if (root == pair.getKey()) {
                                parentId = pair.getValue().getParentId(); //Value = ParentId
                                ackBC = pair.getValue().getAck()+1;
                                pair.getValue().setAck(ackBC);
                                if (parentId == 0 && pair.getValue().getAck() == Node.treeNeighbors.size()) {
                                    if (Configuration.noOfBroadcast > 1) {
                                        Configuration.noOfBroadcast--;
                                        try {
                                            Thread.sleep(Math.round(Configuration.getExpDist(1.0/Configuration.meanDelay)));
                                        }catch (InterruptedException e){
                                            e.printStackTrace();
                                        }
                                        System.out.println("\n"+"Remaining broadcast :"+Configuration.noOfBroadcast+" for node:"+nodeId);
                                        RootParent rootParent1 = new RootParent();
                                        int random = rand.nextInt(100);
                                        Node.rootParentMap.put(root,rootParent1);
                                        new Client(nodeId).beginBroadCast(nodeId, 0, root, random);

                                    } else {
                                        System.out.println("\n"+"Broadcast ended for Node: " + nodeId + "\n");
                                    }
                                } else if (parentId != 0 && pair.getValue().getAck() == Node.treeNeighbors.size() - 1) {
                                    //Send ack to its parent
                                    msg = nodeId + "BROADCAST_ACK" + root;
                                    System.out.println("\n"+"BROADCAST_ACK sent from: "+nodeId+" to: "+parentId+" with root: "+ root);
                                    pHost = Configuration.hostNames.get(parentId - 1);
                                    pPort = Configuration.portList.get(parentId - 1);
                                    client.sendMessage(pHost, pPort, byteBufferSend, msg);
                                }
                            }
                        }
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
