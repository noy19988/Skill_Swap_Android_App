package com.example.skill_swap_app.utils

import android.content.Context
import android.net.Uri
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class CloudinaryManager(private val context: Context) {

    private val cloudinary: Cloudinary

    init {
        val config = mapOf(
            "cloud_name" to "dvhrc7in9",
            "api_key" to "848466133438994",
            "api_secret" to "sl6Elxv64RpMfnn9l-0RIVfVeP0"
        )
        cloudinary = Cloudinary(config)
    }

    suspend fun uploadImage(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val uniqueFileName = "image_${UUID.randomUUID()}.jpg"
                val uploadResult = cloudinary.uploader().upload(
                    inputStream,
                    ObjectUtils.asMap(
                        "public_id", uniqueFileName,
                        "folder", "SkillSwap_Images",
                        "resource_type", "image"
                    )
                )
                uploadResult["secure_url"] as? String
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
