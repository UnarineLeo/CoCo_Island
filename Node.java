import java.util.ArrayList;
import java.util.List;

public class Node {
    private String type;
    private String content;
    private int id;
    public List<Node> children;

    public Node(int id, String type, String content) {
        this.type = type;
        this.content = content;
        this.id = id;
        children = new ArrayList<Node>();
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public int getId() {
        return id;
    }

    ///Copied from 212 Past Practicals
    @Override
    public String toString() {
        return "Node{id=" + id + ", type='" + type + "', content='" + content + "'}";
    }

    public String toStringTree() {
        return toStringTree(this, "", true);
    }

    private String toStringTree(Node node, String prefix, boolean end) {
        String res = "";
        if (!node.children.isEmpty()) {
            for (int i = 0; i < node.children.size() - 1; i++) {
                res += toStringTree(node.children.get(i), prefix + (end ? "    " : "│   "), false);
            }
            res += toStringTree(node.children.get(node.children.size() - 1), prefix + (end ? "    " : "│   "), true);
        }
        res += prefix + (end ? "└── " : "├── ") + node.getContent() + "\n";
        return res;
    }
}