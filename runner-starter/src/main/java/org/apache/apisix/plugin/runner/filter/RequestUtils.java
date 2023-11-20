//package org.apache.apisix.plugin.runner.filter;
//
//import com.google.common.cache.*;
//import lombok.experimental.*;
//
//import javax.annotation.*;
//import java.util.concurrent.*;
//
///**
// * tracking the request and user id information
// */
//@UtilityClass
//public class RequestUtils {
//    private static final Cache<String, Integer> requestId2WolfUserId = CacheBuilder
//            .newBuilder().expireAfterWrite(10, TimeUnit.MINUTES)
//            .maximumSize(10240).build();
//
//
//    public void trackRequest(String requestId, Integer wolfUserId) {
//        requestId2WolfUserId.put(requestId, wolfUserId);
//    }
//
//    public void removeRequest(String requestId) {
//        requestId2WolfUserId.invalidate(requestId);
//    }
//
//    @Nullable
//    public Integer getRequestUserWolfId(String requestId) {
//        return requestId2WolfUserId.getIfPresent(requestId);
//    }
//}
