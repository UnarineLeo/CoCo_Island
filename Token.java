public class Token {
    private int id;
    private String type;
    private String content;
    private int row;
    private int col;

    Token(int id,String type, String content, int row, int col) {
        this.id = id;
        this.type = type;
        this.content = content;
        this.row = row;
        this.col = col;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public String toString() {
        return "Token(" + type + ", " + content + ")";
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}