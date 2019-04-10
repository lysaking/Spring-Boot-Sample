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
		
		//��spring boot 2��ʼĬ��csrf�����õģ���ᵼ��post����ʧ��
		//H2�Ĺ���ҳ�����˵���403�ܾ����ʣ�����һ�ַ����Ǻ���H2����ҳ��
		http.csrf().ignoringAntMatchers("/h2-console/**");
		//����ֻ�ܽ��loginҳ����Ե�¼������¼��ȥҳ���޷�������ʾ
		//��Ϊ������iframe��������Ҫ��������ͬԴframe
		http.headers().frameOptions().sameOrigin();
		//��һ�ֱ����������ǽ���csrf��������ɰ�ȫ����
		//http.csrf().disable();
		//����һ�ֳ����ƿ���Щ���Ƶķ�ʽ�������µĶ˿ڣ���H2config������
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
				//Spring Data�ı���repository��findOne������������ID��Ϊ���������ҷ���Optional����
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

	//Spring Boot2Ĭ�Ͻ�ֹNoOpPasswordEncoder�����Կ����ڸ������ǿ��ȫ
	//��ɵ�����������벻��������
	@Bean
	public PasswordEncoder passwordEncoder() {
		//������ȷָ��NoOpPasswordEncoder�����ⲻ��ȫ����Ҳ��������
//		return NoOpPasswordEncoder.getInstance();
		//���Ի���ָ��һ��encoder������û�ʱҲҪ��encode
		//����sql�ű���Ӳ�������ʱҲҪ��encode������룬���Գ��Ե�½������log�￴������������
		return new BCryptPasswordEncoder();
	}

}
