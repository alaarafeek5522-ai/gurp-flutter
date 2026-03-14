package com.alaa.gurp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;
import android.util.Base64;
import java.io.File;
import java.security.MessageDigest;

public class SecurityUtils {

    private static final String VALID_SIGNATURE =
            "A40DA80A59D170CAA950CF15C18C454D47A39B26989D8B640ECD745BA71BF5DC";

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
            Signature[] signatures;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageInfo packageInfo = context.getPackageManager()
                        .getPackageInfo(context.getPackageName(),
                                PackageManager.GET_SIGNING_CERTIFICATES);
                SigningInfo signingInfo = packageInfo.signingInfo;
                if (signingInfo.hasMultipleSigners()) {
                    signatures = signingInfo.getApkContentsSigners();
                } else {
                    signatures = signingInfo.getSigningCertificateHistory();
                }
            } else {
                PackageInfo packageInfo = context.getPackageManager()
                        .getPackageInfo(context.getPackageName(),
                                PackageManager.GET_SIGNATURES);
                signatures = packageInfo.signatures;
            }

            for (Signature signature : signatures) {
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

    public static boolean verifyAppSignature(Context context) {
        String current = getSignatureHash(context);
        return VALID_SIGNATURE.equals(current);
    }

    public static boolean isAppSecure(Context context) {
        if (isEmulator()) return false;
        if (isRooted()) return false;
        if (!isValidPackage(context)) return false;
        if (!verifyAppSignature(context)) return false;
        return true;
    }
}
