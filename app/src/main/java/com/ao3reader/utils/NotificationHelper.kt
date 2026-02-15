package com.ao3reader.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Helper for notification-related functionality.
 */
object NotificationHelper {
    /**
     * Checks if the app has notification permission.
     * On Android 13+, this requires explicit permission.
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Before Android 13, notifications don't require runtime permission
            true
        }
    }

    /**
     * Notification permission request code.
     */
    const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
}
