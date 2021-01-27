package dev.eastar.ktx

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class Main : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        newBuilder?.invoke(asContext) ?: (asContext as? IOnAlertBuilder)?.onCreateAlertBuilder() ?: AlertDialog.Builder(asContext)
    }
}