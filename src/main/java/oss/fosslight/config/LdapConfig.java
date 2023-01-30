package oss.fosslight.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.web.util.UriComponentsBuilder;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.repository.CodeMapper;
import oss.fosslight.util.StringUtil;

@Configuration
public class LdapConfig {

    @Autowired
    CodeMapper mapper;

    @Bean
    public LdapContextSource ldapContextSource() {

        LdapContextSource contextSource = new LdapContextSource();

        String cdNo = CoConstDef.CD_LOGIN_SETTING;

        String protocol = mapper.getCodeDetail(cdNo, CoConstDef.CD_LDAP_PROTOCOL).getCdDtlExp();
        String url = mapper.getCodeDetail(cdNo, CoConstDef.CD_LDAP_URL).getCdDtlExp();
        String port = mapper.getCodeDetail(cdNo, CoConstDef.CD_LDAP_PORT).getCdDtlExp();
        String userDn = mapper.getCodeDetail(cdNo, CoConstDef.CD_LDAP_SEARCH_ID).getCdDtlExp();
        String password = mapper.getCodeDetail(cdNo, CoConstDef.CD_LDAP_SEARCH_PW).getCdDtlExp();
        String baseDn = mapper.getCodeDetail(cdNo, CoConstDef.CD_LDAP_BASE_DN).getCdDtlExp();
//        String filter = mapper.getCodeDetail(cdNo, CoConstDef.CD_LDAP_FILTER).getCdDtlExp();
//        String searchScope = mapper.getCodeDetail(cdNo, CoConstDef.CD_SEARCH_SCOPE).getCdDtlExp();

        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                .scheme(protocol)
                .host(url);

        if (!StringUtil.isEmptyTrimmed(port)) {
            builder.port(port);
        }

        String uri = builder.build().toUriString();

        contextSource.setUrl(uri);
        contextSource.setBase(baseDn);
        contextSource.setUserDn(userDn);
        contextSource.setPassword(password);

        contextSource.afterPropertiesSet();

        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        return new LdapTemplate(ldapContextSource());
    }
}
