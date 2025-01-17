package oss.fosslight.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ThymeleafConfig {
	private static final String TEMPLATE_ENCODING = "UTF-8";
	private final ApplicationContext applicationContext;
	
	@Bean
	TemplateEngine emailTemplateEngine() {
		final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.addTemplateResolver(htmlTemplateResolver());
		templateEngine.addTemplateResolver(textTemplateResolver());
		return templateEngine;
	}

	private ITemplateResolver htmlTemplateResolver() {
		final SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
		templateResolver.setApplicationContext(applicationContext);
		templateResolver.setOrder(Integer.valueOf(1));
//		templateResolver.setResolvablePatterns(Collections.singleton("html/*"));
		templateResolver.setPrefix("classpath:/template/");
		templateResolver.setSuffix(".html");
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setCharacterEncoding(TEMPLATE_ENCODING);
		templateResolver.setCacheable(false);
		return templateResolver;
	}
	
    private ITemplateResolver textTemplateResolver() {
        final SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(applicationContext);
        templateResolver.setOrder(Integer.valueOf(2));
//        templateResolver.setResolvablePatterns(Collections.singleton("text/*"));
        templateResolver.setPrefix("classpath:/template/");
        templateResolver.setSuffix(".txt");
        templateResolver.setTemplateMode(TemplateMode.TEXT);
        templateResolver.setCharacterEncoding(TEMPLATE_ENCODING);
        templateResolver.setCacheable(false);
        return templateResolver;
    }
}
