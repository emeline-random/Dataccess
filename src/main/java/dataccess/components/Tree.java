package dataccess.components;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

public class Tree extends DefaultTreeNode {

    public Tree(Object data, TreeNode parent) {
        super(data, parent);
    }

    public TreeNode getNode(String name ){
        for (TreeNode t : this.getChildren()) {
            if (t.getData().toString().equalsIgnoreCase(name)) return t;
            for (TreeNode tn : t.getChildren()) {
                if (tn.getData().toString().equalsIgnoreCase(name)) return tn;
            }
        }
        return null;
    }

}
