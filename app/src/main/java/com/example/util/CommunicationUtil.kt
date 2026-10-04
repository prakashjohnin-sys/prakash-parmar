package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object CommunicationUtil {

    /**
     * ગેરહાજર વિદ્યાર્થીના વાલી માટે ગુજરાતી મેસેજ ટેમ્પ્લેટ
     */
    fun createAbsentMessage(
        studentName: String,
        standard: String,
        rollNo: Int,
        date: String
    ): String {
        return """
            આદરણીય વાલીશ્રી,
            આપનો પુત્ર/પુત્રી $studentName ($standard, રોલ નં: $rollNo) આજે તારીખ $date ના રોજ શાળામાં ગેરહાજર છે. નોંધ લેશો.
            
            - આચાર્યશ્રી / મુખ્ય શિક્ષક
            નગર પ્રાથમિક શાળા નં ૨૯ પુરુષોત્તમનગર
            બાકરોલ, તા.જી. આણંદ
            ડાયસ કોડ: ૨૪૧૫૦૧૦૦૧૦૭
        """.trimIndent()
    }

    /**
     * વાલીના મોબાઈલ નંબર પર સીધો SMS મોકલવા માટે
     */
    fun sendSms(context: Context, phone: String, message: String) {
        val cleanPhone = phone.trim().replace(" ", "").replace("-", "")
        if (cleanPhone.isBlank()) {
            Toast.makeText(context, "વાલીનો મોબાઈલ નંબર ઉપલબ્ધ નથી", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$cleanPhone")
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback generic SMS intent
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    type = "vnd.android-dir/mms-sms"
                    putExtra("address", cleanPhone)
                    putExtra("sms_body", message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (ex: Exception) {
                Toast.makeText(context, "SMS એપ્લિકેશન ખોલી શકાઈ નથી", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * વાલીના મોબાઈલ નંબર પર સીધો WhatsApp મેસેજ મોકલવા માટે
     */
    fun sendWhatsApp(context: Context, phone: String, message: String) {
        var cleanPhone = phone.trim().replace(" ", "").replace("-", "").replace("+", "")
        if (cleanPhone.isBlank()) {
            Toast.makeText(context, "વાલીનો મોબાઈલ નંબર ઉપલબ્ધ નથી", Toast.LENGTH_SHORT).show()
            return
        }
        // Ensure India +91 country code if standard 10 digit number
        if (cleanPhone.length == 10) {
            cleanPhone = "91$cleanPhone"
        }

        try {
            val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp ખોલી શકાયું નથી", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * વાલીને સીધો ફોન કૉલ કરવા માટે
     */
    fun makeCall(context: Context, phone: String) {
        val cleanPhone = phone.trim().replace(" ", "").replace("-", "")
        if (cleanPhone.isBlank()) {
            Toast.makeText(context, "વાલીનો મોબાઈલ નંબર ઉપલબ્ધ નથી", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$cleanPhone")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "ડાયલર ખોલી શકાયું નથી", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * તમામ ગેરહાજર વિદ્યાર્થીઓનો સંયુક્ત રિપોર્ટ WhatsApp / મેસેજમાં શેર કરવા માટે
     */
    fun shareAbsentReport(context: Context, reportSummary: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, reportSummary)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "ગેરહાજર રિપોર્ટ શેર કરો"))
        } catch (e: Exception) {
            Toast.makeText(context, "શેર કરી શકાયું નથી", Toast.LENGTH_SHORT).show()
        }
    }
}
