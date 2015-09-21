package org.restler.integration.springdata

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.jdbc.datasource.init.DataSourceInitializer
import org.springframework.jdbc.datasource.init.DatabasePopulator
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.Database
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import javax.sql.DataSource

@Configuration
open class DbConfig {

    private fun databasePopulator(): DatabasePopulator {
        val populator = ResourceDatabasePopulator()

        populator.addScript(ClassPathResource("import.sql"))
        return populator
    }

    @Bean open fun dataSourceInitializer(dataSource: DataSource): DataSourceInitializer {
        val initializer = DataSourceInitializer()
        initializer.setDataSource(dataSource)
        initializer.setDatabasePopulator(databasePopulator())
        return initializer
    }

    @Bean open fun dataSource(): DataSource =
            EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build()

    @Bean open fun entityManagerFactory(): LocalContainerEntityManagerFactoryBean {
        val vendorAdapter = HibernateJpaVendorAdapter()
        vendorAdapter.setDatabase(Database.H2)

        val factory = LocalContainerEntityManagerFactoryBean()
        factory.setJpaVendorAdapter(vendorAdapter)
        factory.setPackagesToScan(this.javaClass.getPackage().getName())
        factory.setDataSource(dataSource())

        return factory
    }

    @Bean open fun transactionManager() = JpaTransactionManager()
}

