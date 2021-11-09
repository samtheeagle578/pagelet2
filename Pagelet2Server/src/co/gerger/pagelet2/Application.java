package co.gerger.pagelet2;

public interface Application {
    String getServerMethods();
    String execute(String controllerName, String methodName,String inputs) throws PageletServerException;
}
