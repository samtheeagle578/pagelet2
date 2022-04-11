package co.gerger.pagelet2;

import java.util.ArrayList;

public interface ClientController {
    void addMethod(String name, boolean synchronous, boolean publicMethod, ArrayList<String> parameterNames, String returnType, boolean authorizer, boolean needsCredentials, String roles);
    String getText();
    String getMethodReturnType(String methodName);
    String getClassName();
    String getSimpleClassName();
    boolean isPublicMethod(String methodName);
    boolean isAuthorizer(String methodName);
    boolean needsCredentials(String methodName);
    boolean canExecute(String methodName, String roleName);
}