package com.homeapps.dictionary.kotlinsoundservice.utility

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.util.*

class GetPhoneNumber(private val context: Context) {

    private var s1name = "NotDefined"
    private var s1code = "NotDefined"
    private var s2name = "NotDefined"
    private var s2code = "NotDefined"

    fun getPhoneNumber(): String {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "give permission"
        }
        val phoneNumber = telephonyManager.line1Number
        Toast.makeText(context, "Phone Number: $phoneNumber", Toast.LENGTH_LONG).show()
        return phoneNumber
    }

    fun getCountryCode(): String {
        // Method to get the country code based on the SIM card or network
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var countryCode = tm.networkCountryIso.uppercase(Locale.ROOT)

        // Return default country code if not found
        if (countryCode.isEmpty()) {
            countryCode = Locale.getDefault().country
        }

        // Convert country code to dial code
        return CountryToPhonePrefix.getPhone(countryCode)
    }

    fun getPhoneNumbers(): List<String> {
        val phoneNumbers = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return phoneNumbers
            }
            val subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList

            if (!subscriptionInfoList.isNullOrEmpty()) {
                if (subscriptionInfoList.size > 0) {
                    val info1 = subscriptionInfoList[0]
                    s1code = "Country Code: +" + getCountryCode(info1.countryIso)
                    s1name = "Phone Number: " + info1.number
                }
                if (subscriptionInfoList.size > 1) {
                    val info2 = subscriptionInfoList[1]
                    s2code = "Country Code: +" + getCountryCode(info2.countryIso)
                    s2name = "Phone Number: " + info2.number
                }
                phoneNumbers.add(s1name)
                phoneNumbers.add(s1code)
                phoneNumbers.add(s2name)
                phoneNumbers.add(s2code)
            }
        }
        return phoneNumbers
    }

    private fun getCountryCode(countryIso: String?): String {
        val iso = countryIso ?: Locale.getDefault().country
        return CountryToPhonePrefix.getPhone(iso)
    }
}
