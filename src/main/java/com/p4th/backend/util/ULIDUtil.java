package com.p4th.backend.util;

import com.github.f4b6a3.ulid.UlidCreator;

public class ULIDUtil {
    public static String getULID() {
        return UlidCreator.getUlid().toString();
    }
}
