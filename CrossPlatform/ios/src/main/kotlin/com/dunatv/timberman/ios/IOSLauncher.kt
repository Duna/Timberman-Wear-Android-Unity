@file:JvmName("IOSLauncher")

package com.dunatv.timberman.ios

import org.robovm.apple.foundation.NSAutoreleasePool
import org.robovm.apple.uikit.UIApplication
import com.badlogic.gdx.backends.iosrobovm.IOSApplication
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration
import com.dunatv.timberman.TimbermanGame

class IOSLauncher : IOSApplication.Delegate() {
    override fun createApplication(): IOSApplication {
        val config = IOSApplicationConfiguration().apply {
            useAccelerometer = false
            useCompass = false
        }
        return IOSApplication(TimbermanGame(IOSFirebaseService()), config)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val pool = NSAutoreleasePool()
            val principalClass: Class<UIApplication>? = null
            val delegateClass = IOSLauncher::class.java
            UIApplication.main(args, principalClass, delegateClass)
            pool.close()
        }
    }
}
