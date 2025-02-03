package com.p4th.backend.util;

import java.security.SecureRandom;
import java.util.Base64;

public class PassCodeUtil {

    public static String generatePassCode() {
        byte[] randomBytes = new byte[12];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
