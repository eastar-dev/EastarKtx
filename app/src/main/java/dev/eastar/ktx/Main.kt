package dev.eastar.ktx

import android.annotation.SuppressLint
import android.content.Context
import android.log.Log
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class Main : AppCompatActivity() {
    private lateinit var dlg: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView()
        newBuilder = object : NewBuilder {
            override fun invoke(context: Context) = CustomDialogBuilder(context)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setContentView() {
        val context = this
        setContentView(ScrollView(context).apply {
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                addView(Button(context).apply {
                    text = "start force with path"
                    setOnClickListener {
                        val edit = EditText(context)
                        AlertDialog.Builder(context).setView(edit)
                            .onPositive("yes")
                            .onNegative("no")
                            .show()
                    }
                })
                addView(Button(context).apply {
                    text = "alert c"
                    setOnClickListener {
                        dlg = alert("msg") {
                            onPositive("po")
                        }
                    }
                })
                addView(Button(context).apply {
                    text = "alert c"
                    setOnClickListener {
                        dlg = alert("msg") {
                            onPositive("po") { Log.e(it, "po") }
                            onNegative("ne") { Log.e(it, "ne") }
                            unCancelable
                            onDismiss { Log.e(it, "di") }
                        }
                    }
                })
                addView(Button(context).apply {
                    text = "alert c"
                    setOnClickListener {
                        dlg = alert("msg") {
                            onPositive("po") { Log.e(it, "po") }
                            onNegative("ne") { Log.e(it, "ne") }
                            unCancelable
                            onDismissFinish
                        }
                    }
                })
                addView(Button(context).apply {
                    text = "alert c"
                    setOnClickListener {
                        dlg = alert("msg") {
                            onPositive("po") { Log.e(it, "po") }
                            onNegative("ne") { Log.e(it, "ne") }
                            unCancelable
                            onDismissFinish
                        }
                    }
                })
                addView(Button(context).apply {
                    text = "alert c"
                    setOnClickListener {
                        dlg = CustomDialogBuilder(it.context).alert("msg") {
                            onPositive("po") { Log.e(it, "po") }
                            onNegative("ne") { Log.e(it, "ne") }
                            setOnDismissListener { Log.e(it, "di") }
                            setOnCancelListener { Log.e(it, "ca") }
                        }
                    }
                })
                repeat(20) {
                    addView(Button(context).apply { text = "" + it })
                }
            })
        }
        )
    }
}