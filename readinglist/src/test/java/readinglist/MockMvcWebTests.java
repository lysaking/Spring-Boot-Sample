package readinglist;

import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ReadingListApplication.class)
public class MockMvcWebTests {

	@Autowired
	WebApplicationContext webContext;
	private MockMvc mockMvc;
	HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository;

	@Before
	public void setupMockMvc() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webContext).apply(springSecurity()).build();
		httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
	}

	@Test
	public void homePage_unauthenticatedUser() throws Exception {
		mockMvc.perform(get("/")).andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", "http://localhost/login"));
	}

	@Test
	@WithUserDetails("a")
	//也可以使用下面的方法，，但这样无法测试从数据库读取用户信息
	//@WithMockUser(username="", password="", roles="READER")
	public void postBook() throws Exception {
		//还是csrf的问题，需要生成一个token
		CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

		//在session中设置token，注意attribute的名字是从HttpSessionCsrfTokenRepository里复制过来的
		mockMvc.perform(post("/")
				.sessionAttr(HttpSessionCsrfTokenRepository.class.getName().concat(".CSRF_TOKEN"), csrfToken)
				.contentType(APPLICATION_FORM_URLENCODED)
				.param("title", "BOOK TITLE")
				.param("author", "BOOK AUTHOR")
				.param("isbn", "1234567890")
				.param("description", "DESCRIPTION")
				//在提交的form里填充token
				.param("_csrf", csrfToken.getToken()))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", "/"));

		Reader expectedReader = new Reader();
		expectedReader.setUsername("a");
		expectedReader.setPassword("$2a$10$aCFzErCFCaHX0GNBibJv7OGU4mjZoUjGi1fI2NA/jexf3Au8DlzPe");
		expectedReader.setFullname("a");

		Book expectedBook = new Book();
		expectedBook.setId(1L);
		expectedBook.setReader(expectedReader);
		expectedBook.setTitle("BOOK TITLE");
		expectedBook.setAuthor("BOOK AUTHOR");
		expectedBook.setIsbn("1234567890");
		expectedBook.setDescription("DESCRIPTION");

		mockMvc.perform(get("/")).andExpect(status().isOk())
				.andExpect(view().name("readingList"))
				.andExpect(model().attribute("reader", samePropertyValuesAs(expectedReader)))
				.andExpect(model().attributeExists("books"))
				.andExpect(model().attribute("books", hasSize(1)))
				//这里需要重写reader类的equals方法，否则测试失败
				.andExpect(model().attribute("books", contains(samePropertyValuesAs(expectedBook))));
	}
}
