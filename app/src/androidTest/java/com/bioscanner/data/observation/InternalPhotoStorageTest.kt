package com.bioscanner.data.observation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class InternalPhotoStorageTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val storage = InternalPhotoStorage(context)

    private fun jpeg(width: Int, height: Int, orientation: Int? = null): Uri {
        val file = File(context.cacheDir, "photo_${width}x$height.jpg")
        file.outputStream().use {
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
        if (orientation != null) {
            ExifInterface(file.absolutePath).apply {
                setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
                saveAttributes()
            }
        }
        return Uri.fromFile(file)
    }

    @Test
    fun importsValidPhotoIntoAppStorage() = runBlocking {
        val path = storage.import(jpeg(400, 300).toString())

        val saved = File(path)
        assertTrue(saved.exists())
        assertTrue(saved.parentFile!!.path.endsWith("observations"))
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        assertEquals(400, bounds.outWidth)
        assertEquals(300, bounds.outHeight)
    }

    @Test
    fun appliesExifRotationAndDownscales() = runBlocking {
        val path = storage.import(jpeg(3200, 1600, ExifInterface.ORIENTATION_ROTATE_90).toString())

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        // Girada 90° (fica em pé) e com o lado maior ≤ 1600 px.
        assertEquals(800, bounds.outWidth)
        assertEquals(1600, bounds.outHeight)
    }

    @Test
    fun deleteRemovesSavedCopyButNeverFilesOutsideItsFolder() = runBlocking {
        val original = File(jpeg(100, 100).path!!)
        val copy = storage.import(Uri.fromFile(original).toString())

        storage.delete(copy)
        storage.delete(original.absolutePath)

        assertTrue(!File(copy).exists())
        assertTrue(original.exists())
    }

    @Test(expected = IOException::class)
    fun rejectsFileThatIsNotAnImage(): Unit = runBlocking {
        val file = File(context.cacheDir, "not_an_image.jpg").apply { writeText("isto não é uma foto") }
        storage.import(Uri.fromFile(file).toString())
    }
}
