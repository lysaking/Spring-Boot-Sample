package readinglist;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

//Spring Boot2有很多变化
//Actuator的变化比较大，需要设置下面配置才可打开所有端点
//management.endpoints.web.exposure.include=*

@SpringBootApplication
public class ReadingListApplication  {

    public static void main(String[] args) {
        SpringApplication.run(ReadingListApplication.class, args);
    }
    
}
