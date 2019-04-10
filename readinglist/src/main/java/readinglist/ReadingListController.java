package readinglist;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/")
@ConfigurationProperties("amazon")
public class ReadingListController {

	private ReadingListRepository readingListRepository;
	private AmazonProperties amazonConfig;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ReaderRepository readerRepository;

	@Autowired
	public ReadingListController(ReadingListRepository readingListRepository, AmazonProperties amazonConfig) {
		this.readingListRepository = readingListRepository;
		this.amazonConfig = amazonConfig;
	}

//	@RequestMapping(method = RequestMethod.GET, value = "/fail")
//	public void fail() {
//		throw new RuntimeException();
//	}

	@ExceptionHandler(value = RuntimeException.class)
	@ResponseStatus(value = HttpStatus.BANDWIDTH_LIMIT_EXCEEDED)
	public String error() {
		return "error";
	}

	@RequestMapping(method = RequestMethod.GET)
	public String readersBooks(Reader reader, Model model) {
		List<Book> readingList = readingListRepository.findByReader(reader);
		if (readingList != null) {
			model.addAttribute("books", readingList);
			model.addAttribute("reader", reader);
			model.addAttribute("amazonID", amazonConfig.getAssociateId());
		}
		return "readingList";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String addToReadingList(Reader reader, Book book) {
		book.setReader(reader);
		readingListRepository.save(book);
		return "redirect:/";
	}

	//实现注册用户，需要注意密码需要encode，在securityconfig里配置
	@RequestMapping(value = "/registry", method = RequestMethod.POST)
	public String register(ReaderReg user) {
		Reader reader = new Reader();
		reader.setUsername(user.getUsername());
		reader.setPassword(passwordEncoder.encode(user.getPassword()));
		reader.setFullname(user.getFullname());
		readerRepository.save(reader);
		return "redirect:/";
	}

}
