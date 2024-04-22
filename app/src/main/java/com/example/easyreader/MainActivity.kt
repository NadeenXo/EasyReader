package com.example.easyreader

import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true

        //////////////////////////////
        // Enable zoom controls
        // Disable built-in zoom controls
        webSettings.setSupportZoom(false)
        webSettings.builtInZoomControls = false
        webSettings.displayZoomControls = false // Hide the default zoom controls

        // Set initial zoom level (optional)
        webSettings.textZoom = 100 // 100% initial zoom level

/////////////////

        // Access the ePub file from resources using its resource ID
        val epubFileResId = R.raw.q1
        val epubFile = File(filesDir, "s3.epub") // Copy the ePub file to internal storage
        copyRawResourceToFile(epubFileResId, epubFile)

        val htmlContent = extractHtmlFromEpub(epubFile)
        val cssContent = extractCssFromEpub(epubFile)

        applyStylingAndLoadHtml(htmlContent, cssContent)


//        val scrollToSectionScript = """
//    var paragraphs = document.getElementsByTagName("p");
//    for (var i = 0; i < paragraphs.length; i++) {
//        var paragraph = paragraphs[i];
//        var spans = paragraph.getElementsByTagName("span");
//        if (spans.length > 0 && /^\d+-/.test(spans[0].textContent.trim()) && paragraph.style.textAlign === "right") {
//            paragraph.scrollIntoView({ behavior: "smooth", block: "center" });
//        }
//    }
//""".trimIndent()
//
//        webView.webChromeClient = object : WebChromeClient() {
//            override fun onProgressChanged(view: WebView?, newProgress: Int) {
//                super.onProgressChanged(view, newProgress)
//                if (newProgress == 100) {
//                    // Execute JavaScript after the WebView has finished loading
//                    webView.evaluateJavascript(scrollToSectionScript, null)
//                }
//            }.
//        }


    }
    fun zoomInText() {
        webView.settings.textZoom += 10 // Increase text zoom by 10%
    }

    fun zoomOutText() {
        webView.settings.textZoom -= 10 // Decrease text zoom by 10%
    }

    fun zoomIn(view: View) {
        zoomInText()
    }

    fun zoomOut(view: View) {
        zoomOutText()
    }



    private fun copyRawResourceToFile(resourceId: Int, outputFile: File) {
        resources.openRawResource(resourceId).use { inputStream ->
            outputFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    private fun extractHtmlFromEpub(epubFile: File): String? {
        var htmlContent: String? = null

        try {
            ZipInputStream(FileInputStream(epubFile)).use { zipInputStream ->
                var zipEntry: ZipEntry? = zipInputStream.nextEntry
                while (zipEntry != null) {
                    val entryName = zipEntry.name
                    if (entryName.endsWith(".html")) {
                        // Found an HTML file, read its content
                        htmlContent = zipInputStream.reader().readText()
                        break
                    }
                    zipEntry = zipInputStream.nextEntry
                }
            }
        } catch (e: Exception) {
            e.printStackTrace() // Handle error appropriately
        }

        return htmlContent
    }

    private fun extractCssFromEpub(epubFile: File): String? {
        var cssContent: String? = null

        try {
            ZipInputStream(FileInputStream(epubFile)).use { zipInputStream ->
                var zipEntry: ZipEntry? = zipInputStream.nextEntry
                while (zipEntry != null) {
                    val entryName = zipEntry.name
                    if (entryName.endsWith(".css")) {
                        // Found the CSS file, read its content
                        cssContent = zipInputStream.reader().readText()
                        break
                    }
                    zipEntry = zipInputStream.nextEntry
                }
            }
        } catch (e: Exception) {
            e.printStackTrace() // Handle error appropriately
        }

        return cssContent
    }

    private fun applyStylingAndLoadHtml(htmlContent: String?, cssContent: String?) {
        val styledHtml = if (htmlContent != null && cssContent != null) {

            val customCss = """
//            p { color: red; }
//            a{ color: blue; }
//            h3 { color: red; }

        """.trimIndent()

            // Combine the custom CSS with the provided CSS content
            val fullCssContent = "$cssContent\n$customCss"

            "<html><head><style>$fullCssContent</style></head><body>$htmlContent</body></html>"
        } else {
            "<html><head></head><body>No content available</body></html>"
        }
        webView.loadDataWithBaseURL(null, styledHtml, "text/html", "UTF-8", null)
    }
}
