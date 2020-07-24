package com.example.candra.contactapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


const val REQUEST_SELECT_PHONE_NUMBER = 1

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonRetrieveContact.setOnClickListener { selectContact() }
    }

    private fun selectContact() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1){
            //if permission not yet granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                //then request the permission
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), 101)
            }

        } else {
            //if version codes less than lollipop, then just straight to do the intent, because it's not yet supported to request permission on the fly.
            selectContactIntent()
        }

    }

    private fun selectContactIntent() {
        // Start an activity for the user to pick a phone number from contacts
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_SELECT_PHONE_NUMBER)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            101 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectContactIntent()
                }
                else {
                    Toast.makeText(this, "Read Contact Permission Denied! Cannot Retrieve Contact Infos", Toast.LENGTH_SHORT).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SELECT_PHONE_NUMBER && resultCode == Activity.RESULT_OK) {
            // Get the URI and query the content provider for the phone number
            data?.let {

                val contactUri: Uri = data.data
                val projection: Array<String> = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)

                contentResolver.query(contactUri, projection, null, null, null).use { cursor ->

                    cursor?.let {
                        // If the cursor returned is valid, get the phone number
                        if (cursor.moveToFirst()) {
                            val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                            val number = cursor.getString(numberIndex)
                            val displayName = cursor.getString(nameIndex)

                            //display phoneNumber in text view
                            val displayToView = "$displayName 's phone number: $number"
                            textView.text = displayToView
                            textView.visibility = View.VISIBLE

                        }
                    }

                }

            }

        }
    }
}
