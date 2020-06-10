package ua.dp.hammer.smarthome.config;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ua.dp.hammer.smarthome.entities.DeviceTypeEntity;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
//@EnableLoadTimeWeaving
@EnableJpaRepositories(basePackages = "ua.dp.hammer.smarthome.repositories")
public class DbConfig {
   @Bean
   @Profile("prod")
   public DataSource dataSourceProd() {
      String connectionUrl = "jdbc:postgresql://postgres-container:5432/super_home?user=postgres&password=Qwerty123";
      ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectionUrl,null);
      PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
      ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory);

      poolableConnectionFactory.setPool(connectionPool);
      return new PoolingDataSource<>(connectionPool);
   }

   @Bean
   @Profile("integration")
   public DataSource dataSourceIntegrationTests() {
      String connectionUrl = "jdbc:postgresql://192.168.0.2:5433/tests_db?user=postgres&password=tests";
      ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectionUrl,null);
      PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
      ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory);

      poolableConnectionFactory.setPool(connectionPool);
      return new PoolingDataSource<>(connectionPool);
   }

   @Autowired
   DataSource dataSource;

   @Bean
   public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
      LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
      entityManagerFactoryBean.setDataSource(dataSource);
      entityManagerFactoryBean.setPackagesToScan(DeviceTypeEntity.class.getPackage().getName());

      HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
      entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);
      entityManagerFactoryBean.setJpaProperties(hibernateProperties());
      return entityManagerFactoryBean;
   }

   @Bean
   public PlatformTransactionManager transactionManager(final EntityManagerFactory emf) {
      final JpaTransactionManager transactionManager = new JpaTransactionManager();
      transactionManager.setEntityManagerFactory(emf);
      return transactionManager;
   }

   private Properties hibernateProperties() {
      Properties hibernateProperties = new Properties();

      hibernateProperties.setProperty("hibernate.cache.use_query_cache", "false");
      hibernateProperties.setProperty("hibernate.cache.use_second_level_cache", "false");
      hibernateProperties.setProperty("hibernate.show_sql", "true");
      hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "none");
      hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL95Dialect");
      return hibernateProperties;
   }
}
