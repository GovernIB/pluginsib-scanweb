package org.fundaciobit.pluginsib.scanweb.springboottester.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.web.SecurityFilterChain;

/**
 * 
 * @author anadal
 *
 */
@Configuration
public class SecurityConfig {


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {


        http.authorizeRequests().antMatchers("/**").permitAll().antMatchers("/").permitAll();
                //.anyRequest().authenticated();
                //.antMatchers("/secure/**").authenticated();

        //http.httpBasic(); //withDefaults()

        http.csrf().disable();

        return http.build();

    }


}