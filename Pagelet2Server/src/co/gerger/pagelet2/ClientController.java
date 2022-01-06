package co.gerger.pagelet2;

import java.util.ArrayList;

public interface ClientController {
    void addMethod(String name, boolean synchronous, boolean publicMethod, ArrayList<String> parameterNames, String returnType, boolean authorizer, boolean needsCredentials);
    String getText();
    String getMethodReturnType(String methodName);
    String getClassName();
    boolean isPublicMethod(String methodName);
    boolean isAuthorizer(String methodName);
    boolean needsCredentials(String methodName);
}