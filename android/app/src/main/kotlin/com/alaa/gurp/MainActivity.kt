package com.alaa.gurp

import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private val CHANNEL = "com.alaa.gurp/security"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // Layer 1 — فحص عند البدء
        if (!SecurityUtils.isAppSecure(this)) {
            finishAffinity()
            System.exit(0)
        }

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "isRooted" -> result.success(SecurityUtils.isRooted())
                    "isEmulator" -> result.success(SecurityUtils.isEmulator())
                    "isValidPackage" -> result.success(SecurityUtils.isValidPackage(this))
                    "getSignatureHash" -> result.success(SecurityUtils.getSignatureHash(this))
                    "decrypt" -> {
                        val encrypted = call.argument<String>("data") ?: ""
                        result.success(SecurityUtils.decrypt(encrypted))
                    }
                    else -> result.notImplemented()
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Layer 2 — فحص في onResume
        if (!SecurityUtils.isValidPackage(this)) {
            finishAffinity()
            System.exit(0)
        }
    }
}
