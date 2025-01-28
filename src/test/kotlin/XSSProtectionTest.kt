import com.codeborne.selenide.Configuration
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.Selenide.`$`
import com.codeborne.selenide.Selenide.element
import com.codeborne.selenide.WebDriverRunner
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openqa.selenium.NoAlertPresentException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.junit5.JUnit5Asserter.fail


class XSSProtectionTest {

    companion object {
        const val UPLOAD_URL: String =
            "https://google-gruyere.appspot.com/526707908645020830517766443295239949706/upload.gtl"
        const val FILE_NAME: String = "xss_test.html"
    }

    private var tempFile: Path? = null

    @BeforeEach
    fun setUp() {
        Configuration.browser = "chrome"
        Configuration.timeout = 10000
    }

    @Test
    fun testXSSInUrl() {
        val maliciousUrl =
            "https://google-gruyere.appspot.com/526707908645020830517766443295239949706/<script>alert(document.cookie);</script>"

        Selenide.open(maliciousUrl)

        try {
            val alert = WebDriverRunner.getWebDriver().switchTo().alert()
            val alertText = alert.text
            alert.accept()
            fail("XSS vulnerability detected! Alert text: $alertText")
        } catch (e: NoAlertPresentException) {
            println("No alert detected. XSS vulnerability is not present.")
        }
    }

    @Test
    fun testXSSInFile() {
        tempFile = Files.createTempFile("xss_test", ".html")
        tempFile?.let {
            Files.write(it, "<script>alert(document.cookie);</script>".toByteArray())
            Selenide.open(UPLOAD_URL)
            element("input[name='upload_file']").uploadFile(it.toFile())
            element("input[type='submit']").click()


            // Получаем URL загруженного файла
            extractFileUrlFromPage().let { uploadedFileUrl ->
                // Открываем загруженный файл
                Selenide.open(uploadedFileUrl)

                // Проверяем, появилось ли alert-окно
                try {
                    val alert = WebDriverRunner.getWebDriver().switchTo().alert()
                    val alertText = alert.text
                    alert.accept()
                    fail("XSS vulnerability detected! Alert text: $alertText")
                } catch (e: NoAlertPresentException) {
                    println("No alert detected. XSS vulnerability is not present.")
                }
            } ?: fail("null")
        }
    }

    private fun extractFileUrlFromPage(): String {
        // Получаем текст из элемента <div class="content">
        val contentText = element("div.content").text

        // Извлекаем URL из текста
        // Пример текста: "File uploaded! File accessible at: https://example.com/root/file.html"
        val parts = contentText
            .split("File accessible at:".toRegex())
            .dropLastWhile { it.isEmpty() }.toTypedArray()
        if (parts.size > 1) {
            return parts[1].trim { it <= ' ' }  // Возвращаем URL, удаляя лишние пробелы
        } else {
            throw RuntimeException("URL загруженного файла не найден на странице.")
        }
    }

    @AfterEach
    fun tearDown() {
        Selenide.closeWebDriver()
    }
}