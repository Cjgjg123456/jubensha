package org.example.jubensha.common;

import java.util.UUID;

public class UidUtil {
    // 生成8位唯一用户UID
    public static String generateUid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}