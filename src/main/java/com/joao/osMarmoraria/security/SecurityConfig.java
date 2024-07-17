import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CorsFilter corsFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .antMatchers(HttpMethod.POST, "/auth/register").permitAll()
                .antMatchers(HttpMethod.POST, "/product").hasRole("ADMIN")
                .antMatchers(HttpMethod.GET, "/funcionarios").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/funcionarios").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/funcionarios/{id}").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/funcionarios/{id}").hasRole("ADMIN")
                .antMatchers(HttpMethod.GET, "/clientes").permitAll()
                .antMatchers(HttpMethod.POST, "/clientes").permitAll()
                .antMatchers(HttpMethod.PUT, "/clientes/{id}").permitAll()
                .antMatchers(HttpMethod.DELETE, "/clientes/{id}").permitAll()
                .antMatchers(HttpMethod.POST, "/os").permitAll()
                .antMatchers(HttpMethod.POST, "/usuarios").permitAll()
                .antMatchers(HttpMethod.GET, "/api/estado").permitAll()
                .antMatchers(HttpMethod.POST, "/api/estado").permitAll()
                .antMatchers(HttpMethod.PUT, "/api/estado/{id}").permitAll()
                .antMatchers(HttpMethod.DELETE, "/api/estado/{id}").permitAll()
                .antMatchers(HttpMethod.GET, "/api/cidade").permitAll()
                .antMatchers(HttpMethod.GET, "/api/cidade/{id}").permitAll()
                .antMatchers(HttpMethod.POST, "/api/cidade").permitAll()
                .antMatchers(HttpMethod.PUT, "/api/cidade/{id}").permitAll()
                .antMatchers(HttpMethod.DELETE, "/api/cidade/{id}").permitAll()
                .antMatchers(HttpMethod.GET, "/pessoas").permitAll()
                .antMatchers(HttpMethod.POST, "/pessoas").permitAll()
                .antMatchers(HttpMethod.PUT, "/pessoas/{id}").permitAll()
                .antMatchers(HttpMethod.DELETE, "/pessoas/{id}").permitAll()
                .anyRequest().authenticated();
    }
}
