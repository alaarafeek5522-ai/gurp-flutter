package com.alaa.gurp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.util.Base64;
import java.io.File;
import java.security.MessageDigest;

public class SecurityUtils {

    public static String decrypt(String encrypted) {
        byte[] decoded = Base64.decode(encrypted, Base64.DEFAULT);
        return new String(decoded);
    }

    public static boolean isEmulator() {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.contains("vbox")
                || Build.FINGERPRINT.contains("test-keys")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic"));
    }

    public static boolean isRooted() {
        String[] rootPaths = {
            "/system/app/Superuser.apk", "/sbin/su",
            "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su"
        };
        for (String path : rootPaths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    public static boolean isValidPackage(Context context) {
        return "com.alaa.gurp".equals(context.getPackageName());
    }

    public static String getSignatureHash(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(),
                            PackageManager.GET_SIGNATURES);
            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] digest = md.digest(signature.toByteArray());
                StringBuilder hexString = new StringBuilder();
                for (byte b : digest) {
                    String hex = Integer.toHexString(0xFF & b);
                    if (hex.length() == 1) hexString.append("0");
                    hexString.append(hex);
                }
                return hexString.toString().toUpperCase();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isAppSecure(Context context) {
        if (isEmulator()) return false;
        if (isRooted()) return false;
        if (!isValidPackage(context)) return false;
        return true;
    }
}
