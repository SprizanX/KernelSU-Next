package com.rifsxd.ksunext.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.rifsxd.ksunext.Natives
import com.rifsxd.ksunext.R
import com.rifsxd.ksunext.ui.MainActivity

class RootStatusWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        refresh(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun refresh(context: Context) {
            try {
                val manager = AppWidgetManager.getInstance(context) ?: return
                val ids = manager.getAppWidgetIds(ComponentName(context, RootStatusWidgetProvider::class.java))
                if (ids.isNullOrEmpty()) return
                refresh(context, manager, ids)
            } catch (_: Exception) {
                // widget updates must never crash the host process
            }
        }

        private fun refresh(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val views = RemoteViews(context.packageName, R.layout.widget_root_status)
            val statusColor: Int
            val statusText: String
            val detailsText: String

            try {
                val supported = Natives.isManager && Natives.version >= Natives.MINIMAL_SUPPORTED_KERNEL
                when {
                    !supported -> {
                        statusText = context.getString(R.string.home_failure)
                        statusColor = Color.parseColor("#FFF87171")
                        detailsText = getKernelLine(context)
                    }
                    Natives.isSafeMode -> {
                        statusText = context.getString(R.string.safe_mode)
                        statusColor = Color.parseColor("#FFEED49F")
                        detailsText = getKernelLine(context)
                    }
                    else -> {
                        statusText = context.getString(R.string.home_working)
                        statusColor = Color.parseColor("#FF9FE870")
                        val tag = Natives.getVersionTag()
                        detailsText = tag ?: context.getString(R.string.home_working_version, Natives.version.toString(), "")
                    }
                }
            } catch (_: Throwable) {
                statusText = context.getString(R.string.home_failure)
                statusColor = Color.parseColor("#FFF87171")
                detailsText = ""
            }

            views.setTextViewText(R.id.widget_status, statusText)
            views.setTextColor(R.id.widget_status, statusColor)
            views.setTextViewText(R.id.widget_details, detailsText)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pending = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pending)

            try {
                manager.updateAppWidget(ids, views)
            } catch (_: Exception) {
            }
        }

        private fun getKernelLine(context: Context): String {
            return try {
                val v = com.rifsxd.ksunext.getKernelVersion()
                context.getString(R.string.widget_kernel_version, v.toString())
            } catch (_: Throwable) {
                ""
            }
        }
    }
}
