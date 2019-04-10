package readinglist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ReadingListApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class IntegrationTest {

	@Value("${local.server.port}")
	private int port;

	private static FirefoxDriver browser;

	@Ignore
	@Test(expected = HttpClientErrorException.class)
	public void pageNotFound() {
		try {
			RestTemplate rest = new RestTemplate();
			rest.getForObject("http://localhost:{port}/bogusPage", String.class, port);
			fail("Should result in HTTP 404");
		} catch (HttpClientErrorException e) {
			assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
			throw e;
		}
	}

	@BeforeClass
	public static void openBrowser() {
		//必须下载geckodriver并设置system property，否则driver无法工作
		System.setProperty("webdriver.gecko.driver", 
				"D:\\geckodriver-v0.24.0-win64\\geckodriver.exe");
		browser = new FirefoxDriver();
		browser.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
	}

	@Test
	public void addBookToEmptyList() throws InterruptedException {
		WebDriverWait wait = new WebDriverWait(browser, 5);
		
		String baseUrl = "http://localhost:" + port;
		browser.get(baseUrl);
		
		//因为加了security保护，需要先登录
		String currentUrl = browser.getCurrentUrl();
		assertEquals(baseUrl + "/login", currentUrl);
		
		browser.findElementByName("username").sendKeys("a");
		Thread.sleep(1000); //为了可以看到操作过程而已
		browser.findElementByName("password").sendKeys("a");
		Thread.sleep(1000);
		browser.findElementByTagName("form").submit();
		
		//等待变成期望的title
		wait.until(ExpectedConditions.titleContains("Reading List"));
		
		assertEquals("You have no books in your book list", browser.findElementByTagName("div").getText());

		browser.findElementByName("title").sendKeys("BOOK TITLE");
		Thread.sleep(1000);
		browser.findElementByName("author").sendKeys("BOOK AUTHOR");
		Thread.sleep(1000);
		browser.findElementByName("isbn").sendKeys("1234567890");
		Thread.sleep(1000);
		browser.findElementByName("description").sendKeys("DESCRIPTION");
		Thread.sleep(1000);
		browser.findElementById("bookForm").submit();

		//等待期望的页面元素
		wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("dt.bookHeadline")));
		
		WebElement dl = browser.findElementByCssSelector("dt.bookHeadline");
		assertEquals("BOOK TITLE by BOOK AUTHOR (ISBN: 1234567890)", dl.getText());
		WebElement dt = browser.findElementByCssSelector("dd.bookDescription");
		assertEquals("DESCRIPTION", dt.getText());
	}
	
	@AfterClass
	public static void closeBrowser() {
		//不关闭是为了看最后的效果，但是服务器已经停止，所以也无法继续操作
		//browser.quit();
	}
}
