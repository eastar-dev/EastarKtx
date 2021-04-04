package dev.eastar.ktx

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class Main : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView()
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
                    repeat(20) {
                        addView(Button(context).apply { text = "" + it })
                    }
                })
            }
        )
    }
}