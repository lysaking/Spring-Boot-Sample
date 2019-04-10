package readinglist;

import java.sql.SQLException;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

//配置H2数据库的参数，主要可以配置web管理页面的端口，避免csrf问题
@Configuration
public class H2Config {
	// TCP port for remote connections, default 9092
	@Value("${h2.tcp.port:9092}")
	private String h2TcpPort;

	// Web port, default 8082
	@Value("${h2.web.port:8082}")
	private String h2WebPort;

	@Bean
	@ConditionalOnExpression("${h2.tcp.enabled:false}")
	public Server h2TcpServer() throws SQLException {
		return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", h2TcpPort).start();
	}

	@Bean
	@ConditionalOnExpression("${h2.web.enabled:true}")
	public Server h2WebServer() throws SQLException {
		return Server.createWebServer("-web", "-webAllowOthers", "-webPort", h2WebPort).start();
	}

}
