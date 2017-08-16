/*
This class is used to parse the configuration file, extract all required information and store them in appropriate variables
Function - to get random delay using exponential distribution given mean
*/


import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Created by gokul on 6/16/2017.
 */
public class Configuration {
    static HashMap<Integer, Node> nodeHashMap = new HashMap<Integer, Node>(); // Map stores nodes and its corr. id,port,host & neighbors
    static ArrayList<Integer> portList = new ArrayList<Integer>();      //List of all port numbers
    static ArrayList<String> hostNames = new ArrayList<String>();      //List of all hosts
    static int noOfBroadcast;               //Specifies number of times broadcast operations need to executed
    public static Random rand = new Random(System.currentTimeMillis());
    public static int meanDelay;            //Specifies delay between each broadcast operation

    public static void readFile() {
        String fileName = "/home/012/g/gx/gxs161530/AOS/Final/config.txt";//"D:/UTD/AOS/Project1/config.txt";
        int noOfNodes=0;
        String line = null;

        try {
            BufferedReader  reader = new BufferedReader(new FileReader(fileName));
            if (reader == null){
                System.out.println("Error while reading file");
                return;
            }
            while ((line=reader.readLine())!=null){
                line = line.trim();                 // Ignore empty lines
                if(line.isEmpty()){
                    continue;
                }                                  // Reads no. of nodes in the graph
                else if (line.contains("# Number of nodes")) {
                    line = reader.readLine();
                    line.replaceAll("\\s+","");
                    noOfNodes = Integer.parseInt(line);
                }                                   // Reads no. of broadcast operations to perform
                else if (line.contains("# Number of broadcast")) {
                    line = reader.readLine();
                    line.replaceAll("\\s+","");
                    noOfBroadcast = Integer.parseInt(line);
                }                                     // Reads mean delay
                else if (line.contains("# Mean delay")) {
                    line = reader.readLine();
                    line.replaceAll("\\s+","");
                    meanDelay = Integer.parseInt(line);
                }                                   //This block reads each node's host name, port & neighbors and
                else if (line.contains("# Hostname")){          // stores in corr. lists and map
                    for (int k=1; k<=noOfNodes; k++){
                        line = reader.readLine();
                        if (line != null) {
                            String[] entries = line.split("\\s+");
                            hostNames.add(entries[0]);
                            portList.add(Integer.parseInt(entries[1]));
                            ArrayList<Integer> neighbors = new ArrayList<Integer>();
                            for (int j = 2; j < entries.length; j++) {
                                neighbors.add(Integer.parseInt(entries[j]));
                            }
                            nodeHashMap.put(k, new Node(k, hostNames.get(k - 1), portList.get(k - 1), neighbors));
                        }
                    }
                }
                else{
                    continue;
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // This block is used to get random number using exponential distribution given mean delay
   public static double getExpDist(double lambda) {
       if (lambda > 0.0)
           return (Math.log(1 - rand.nextDouble()))/(-lambda);
       else
           throw new IllegalArgumentException("Not Positive");
   }
}