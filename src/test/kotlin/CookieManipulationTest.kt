import com.codeborne.selenide.Condition.text
import com.codeborne.selenide.Configuration
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.Selenide.element
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.util.Date
import kotlin.random.Random
import kotlin.test.Test

class CookieManipulationTest {
    companion object {
        const val BASE_URL = "https://google-gruyere.appspot.com/526707908645020830517766443295239949706"
    }

    @BeforeEach
    fun setUp() {
        Configuration.browser = "chrome"
        Configuration.timeout = 10000
    }

    @Test
    fun testAdminUserCreationByUrl() {
        val randomInt = Random(Date().time).nextInt()

        val url = "$BASE_URL/saveprofile?action=new&uid=test$randomInt|administrator|admin|author&pw=secret"

        Selenide.open(url)
        element("div.message").shouldNotHave(text("Account created."))
    }

    @Test
    fun testAdminUserCreationByInput() {
        val url = "$BASE_URL/newaccount.gtl"
        val randomInt = Random(Date().time).nextInt(0,1000)
        val username = "t${randomInt}|admin"
        Selenide.open(url)

        element("input[name='uid']")
            .setValue(username)

        element("input[name='pw']")
            .setValue("qwer")

        element("input[type='submit']").click()

        Thread.sleep(100)
        element("div.message").shouldNotHave(text("Account created."))
    }

    @AfterEach
    fun tearDown() {
        Selenide.closeWebDriver()
    }
}