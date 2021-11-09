package co.gerger.pagelet2;

import java.util.ArrayList;

public interface ClientController {
    void addMethod(String name, boolean synchronous, boolean publicMethod, ArrayList<String> parameterNames, String returnType);
    String getText();
    String getMethodReturnType(String methodName);
    boolean isPublicMethod(String methodName);
}