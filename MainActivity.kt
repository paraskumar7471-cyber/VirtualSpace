package com.example.virtualspace

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*

class MainActivity : Activity() {
    private lateinit var dpm: DevicePolicyManager
    private lateinit var admin: ComponentName
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        admin = ComponentName(this, AdminReceiver::class.java)

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 60, 40, 40) }
        status = TextView(this).apply { textSize = 16f }
        root.addView(status)

        val isProfile = dpm.isProfileOwnerApp(packageName)
        if (!isProfile) {
            root.addView(Button(this).apply {
                text = "Virtual Space बनाएँ (Work Profile)"
                setOnClickListener { createProfile() }
            })
            status.text = "अभी Virtual Space नहीं है। नीचे बटन दबाकर बनाएँ।"
        } else {
            status.text = "आप Virtual Space (Work Profile) के अंदर हैं।\nयहाँ इंस्टॉल किए ऐप मुख्य फ़ोन से अलग रहते हैं।"
            val pkg = EditText(this).apply { hint = "पैकेज नाम, जैसे com.whatsapp" }
            root.addView(pkg)
            root.addView(Button(this).apply {
                text = "सिस्टम ऐप यहाँ चालू करें"
                setOnClickListener {
                    try {
                        dpm.enableSystemApp(admin, pkg.text.toString().trim())
                        toast("चालू हो गया")
                    } catch (e: Exception) { toast("नहीं हुआ: ${e.message}") }
                }
            })
            root.addView(Button(this).apply {
                text = "APK फ़ाइल चुनकर इंस्टॉल करें"
                setOnClickListener {
                    startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "application/vnd.android.package-archive"
                    }, 1)
                }
            })
        }
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun createProfile() {
        val intent = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE).apply {
            putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME, admin)
        }
        if (intent.resolveActivity(packageManager) != null) startActivityForResult(intent, 2)
        else toast("आपका फ़ोन Work Profile सपोर्ट नहीं करता")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data?.data != null) {
            val uri: Uri = data.data!!
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            })
        }
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()
}
