package pe.tecsup.paquitobot

import android.app.Application
import com.clerk.api.Clerk

class PaquitoBotApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Clerk.initialize(
            this,
            publishableKey = BuildConfig.CLERK_PUBLISHABLE_KEY
        )
    }
}
