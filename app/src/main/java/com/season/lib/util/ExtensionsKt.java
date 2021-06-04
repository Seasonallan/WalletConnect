package com.season.lib.util;

import com.season.lib.lib.Numeric;


public class ExtensionsKt {
    
    public static final String toHexString(byte[] $this$toHexString) {
        String var10000 = Numeric.toHexString($this$toHexString, 0, $this$toHexString.length, false);
        return var10000;
    }

    
    public static final byte[] toByteArray(String $this$toByteArray) {
        byte[] var10000 = Numeric.hexStringToByteArray($this$toByteArray);
        return var10000;
    }
}
