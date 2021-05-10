package dev.eastar.ktx

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
            override fun invoke(context: Context) = CustomDialog.Builder(context)
        }
    }


    private fun setContentView() {
        val context = this
        setContentView(
            ScrollView(context).apply {
                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(Button(context).apply {
                        text = "start force with path"
                        setOnClickListener {
                            kotlin.runCatching {
                                val edit = EditText(context)
                                AlertDialog.Builder(context).setView(edit)
                                    .positiveButton("yes")
                                    .negativeButton("no")
                                    .show()
                            }.onFailure {
                                toast("=> ready to realre start <=")
                            }
                        }
                    })
                    addView(Button(context).apply {
                        text = "alert"
                        setOnClickListener {
                            AlertDialog.Builder(it.context)
                                .setTitle("tit")
                                .setPositiveButton("po") { it, _ -> Log.e(it, "po") }
                                .setNegativeButton("ne") { it, _ -> Log.e(it, "ne") }
                                .setOnDismissListener { Log.e(it, "di") }
                                .setOnCancelListener { Log.e(it, "ca") }
                                .create()
                                .show()
                        }
                    })
                    addView(Button(context).apply {
                        text = "alert c"
                        setOnClickListener {
                            dlg = alert("msg") {
                                positiveButton("po") { Log.e(it, "po") }
                                negativeButton("ne") { Log.e(it, "ne") }
                                //onDismiss { Log.e(it, "di") }
                                setOnDismissListener { Log.e(it, "di") }
                                setOnCancelListener { Log.e(it, "ca") }
                            }
                        }
                    })
                    addView(Button(context).apply {
                        text = "alert dismiss"
                        setOnClickListener {
                            dlg.dismiss()
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