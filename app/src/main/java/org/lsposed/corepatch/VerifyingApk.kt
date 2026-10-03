package org.lsposed.corepatch

/**
 * Tracks the APK the current thread is verifying, so hooks without a path argument
 * (StrictJarVerifier, ApkSigningBlockUtils, MessageDigest) only bypass checks for apps
 * under /data/app. System partition scans keep the native verification results.
 */
object VerifyingApk {
    private val paths = ThreadLocal.withInitial { ArrayDeque<String>() }

    fun enter(path: String) {
        paths.get().addLast(path)
    }

    fun exit() {
        paths.get().removeLastOrNull()
    }

    fun isUserApp(path: String?) = path != null && path.contains("/data/app")

    fun isCurrentUserApp() = isUserApp(paths.get().lastOrNull())
}
