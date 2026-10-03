package org.lsposed.corepatch.hook

import android.annotation.SuppressLint
import android.os.Build
import org.lsposed.corepatch.Config
import org.lsposed.corepatch.VerifyingApk
import org.lsposed.corepatch.XposedHelper.hookBefore
import org.lsposed.corepatch.XposedHelper.hostClassLoader

object ScanPackageUtilsHook : BaseHook() {
    override val name = "ScanPackageUtilsHook"

    @SuppressLint("PrivateApi")
    override fun hook() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val scanPackageUtilsClazz =
            hostClassLoader.loadClass("com.android.server.pm.ScanPackageUtils")
        // static void assertMinSignatureSchemeIsValid(AndroidPackage pkg, int parseFlags)
        val assertMinSignatureSchemeIsValidMethod =
            scanPackageUtilsClazz.declaredMethods.first { m -> m.name == "assertMinSignatureSchemeIsValid" }
        hookBefore(assertMinSignatureSchemeIsValidMethod) { callback ->
            if (Config.isBypassVerificationEnabled() &&
                VerifyingApk.isUserApp(packagePath(callback.args[0]))
            ) {
                callback.returnAndSkip(null)
            }
        }
    }

    private fun packagePath(pkg: Any?): String? = runCatching {
        pkg?.javaClass?.getMethod("getPath")?.invoke(pkg) as? String
    }.getOrNull()
}
