package readinglist;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//WebMvcConfigurerAdapter�������ã�������Ҫʵ��WebMvcConfigurer
//���ܼ̳�WebMvcConfigurationSupport��������Զ�����ʧЧ
//ԭ����WebMvcAutoConfiguration�ϱ�ע��@ConditionalOnMissingBean(WebMvcConfigurationSupport.class)
@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Override
    public void addViewControllers(ViewControllerRegistry registry) {
      registry.addViewController("/login").setViewName("login");
      registry.addViewController("/register").setViewName("register");
    }
    
    @Override
    public void addArgumentResolvers(
        List<HandlerMethodArgumentResolver> argumentResolvers) {
      argumentResolvers.add(new ReaderHandlerMethodArgumentResolver());
    }
}
