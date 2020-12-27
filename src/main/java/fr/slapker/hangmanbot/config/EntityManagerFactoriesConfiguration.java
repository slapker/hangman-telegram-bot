package fr.slapker.hangmanbot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;

@Configuration
public class EntityManagerFactoriesConfiguration {
	@Autowired
	private DataSource dataSource;

    @Value("${bot.name}")
    String botName;

    @Value("${bot.token}")
    String botToken;

	@Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean emf() {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan(new String[] {"fr.slapker.hangmanbot.model"});
        emf.setJpaVendorAdapter(
        new HibernateJpaVendorAdapter());
        return emf;
   }

}