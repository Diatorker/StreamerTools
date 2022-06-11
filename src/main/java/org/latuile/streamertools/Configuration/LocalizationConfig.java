package org.latuile.streamertools.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.List;
import java.util.Locale;

@Configuration
public class LocalizationConfig implements WebMvcConfigurer
{
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver hlr = new AcceptHeaderLocaleResolver();
        hlr.setSupportedLocales(List.of(Locale.FRANCE, Locale.ENGLISH));
        hlr.setDefaultLocale(Locale.ENGLISH);
        return hlr;
    }
}
