package co.gerger.pagelet2;

public class MethodParameterImpl {
    String name = null;
    String type = null;
    public MethodParameterImpl(String name, String type) {
        super();
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
