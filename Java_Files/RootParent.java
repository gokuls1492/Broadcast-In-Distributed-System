/**
 * Created by gokul on 7/9/2017.
 */
/*
    This class is used to store root, parent and ack count while performing broadcast operation
    Generated Getter and Setter for each attribute used for accessing its value

 */
public class RootParent {
    int root;               //Node from which broadcast initiated
    int parentId;           //Node that sends a particular message
    int ack;                //Count of acknowledgments received from its child nodes

    RootParent(){
        root = 0;
        parentId = 0;
        ack = 0;
    }

    public int getRoot() {
        return root;
    }

    public void setRoot(int root) {
        this.root = root;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getAck() {
        return ack;
    }

    public void setAck(int ack) {
        this.ack = ack;
    }
}
