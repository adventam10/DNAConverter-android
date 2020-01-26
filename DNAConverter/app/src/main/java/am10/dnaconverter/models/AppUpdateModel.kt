package am10.dnaconverter.models

import android.app.Activity
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class AppUpdateModel(context: Context) {
    val manager = AppUpdateManagerFactory.create(context)
    var listener: InstallStateUpdatedListener? = null
    val REQUEST_CODE = 100
    fun checkAppVersion(activity: Activity, callback: (() -> (Unit))?) {
        listener = makeListener(callback)
        manager.registerListener(listener)
        manager.appUpdateInfo.addOnSuccessListener { info ->
            when (info.updateAvailability()) {
                UpdateAvailability.UPDATE_AVAILABLE -> {
                    manager.startUpdateFlowForResult(info, AppUpdateType.FLEXIBLE, activity, REQUEST_CODE)
                }
                else -> {
                }
            }
        }
    }

    fun addOnSuccessListener(callback: (() -> (Unit))?) {
        manager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                callback?.invoke()
            }
        }
    }

    fun completeUpdate() {
        manager.completeUpdate()
    }

    private fun makeListener(callback: (() -> (Unit))?) : InstallStateUpdatedListener {
        return InstallStateUpdatedListener {
            if (it.installStatus() == InstallStatus.DOWNLOADED) {
                callback?.invoke()
                manager.unregisterListener(listener)
            }
        }
    }
}