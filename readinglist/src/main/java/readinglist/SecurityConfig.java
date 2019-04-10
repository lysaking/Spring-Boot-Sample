package readinglist;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Example;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private ReaderRepository readerRepository;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
		.authorizeRequests()
		.antMatchers("/").access("hasRole('READER')")
		.antMatchers("/**").permitAll()
		.and()
				.formLogin().loginPage("/login").failureUrl("/login?error=true");
		
		//从spring boot 2开始默认csrf是启用的，这会导致post请求失败
		//H2的管理页面会因此导致403拒绝访问，所以一种方法是忽略H2管理页面
		http.csrf().ignoringAntMatchers("/h2-console/**");
		//上面只能解决login页面可以登录，但登录进去页面无法正常显示
		//因为不允许iframe，这里需要设置允许同源frame
		http.headers().frameOptions().sameOrigin();
		//另一种暴力方法就是禁用csrf，但会造成安全隐患
		//http.csrf().disable();
		//还有一种彻底绕开这些限制的方式是启用新的端口，在H2config里设置
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService());
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return new UserDetailsService() {
			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				//Spring Data改变了repository的findOne方法，不再用ID作为参数，并且返回Optional类型
				Reader userDetails = new Reader();
				userDetails.setUsername(username);
				Example<Reader> example = Example.of(userDetails);
				Optional<Reader> optional = readerRepository.findOne(example);
				if (optional.isPresent()) {
					System.out.println(optional.get().getPassword());
					return optional.get();
				}
				throw new UsernameNotFoundException("User '" + username + "' not found.");
			}
		};
	}

	//Spring Boot2默认禁止NoOpPasswordEncoder，可以看出在各方面加强安全
	//造成的问题就是密码不能是明文
	@Bean
	public PasswordEncoder passwordEncoder() {
		//可以明确指定NoOpPasswordEncoder，但这不安全而且也将被废弃
//		return NoOpPasswordEncoder.getInstance();
		//所以还是指定一个encoder，添加用户时也要做encode
		//另外sql脚本添加测试数据时也要用encode后的密码，可以尝试登陆在运行log里看到期望的密码
		return new BCryptPasswordEncoder();
	}

}
