package com.github.kermetian.ribbit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.PackageManagerCompat

class ShareActivity: Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Check we got a `send` shared intent starting this share activity with text
        val shareUri = if (intent?.action == Intent.ACTION_SEND) {
            // guard against text being included only (no extra streams to remove tracking from)
            // call scrub as method reference to regex match and scrub split on whitespace
            // to handle against
            intent?.getStringExtra(Intent.EXTRA_TEXT)?.let(::scrub)
        } else null

        if (shareUri != null) {
            // Open a new share action intent chooser, ribbit-ing through without as many trackers
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareUri)
            }
            if (intent.resolveActivity(this.packageManager) != null) {
                val chooser = Intent.createChooser(intent, getString(R.string.share_link_title)).apply {
                    putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, arrayOf(this@ShareActivity.componentName))
                }
                startActivity(chooser)
            }
        }

        finish()
        return
    }
}