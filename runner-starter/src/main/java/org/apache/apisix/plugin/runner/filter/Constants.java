package org.apache.apisix.plugin.runner.filter;

public class Constants {
    public static final String HEADER_USER_ID = "x-userid";
    public static final String HEADER_RESPONSEBODY_ENCRYPTED_FLAG = "x-response-encrypt";
    public static final String HEADER_REQUESTBODY_ENCRYPTED_FLAG = "x-request-decrypt";

    public static final String ERROR_NOT_FOUND = "Error: user not found";
    public static final String ERROR_DECRYPT_REQUEST_FAILURE = "Error: decrypt request body failure";
}
