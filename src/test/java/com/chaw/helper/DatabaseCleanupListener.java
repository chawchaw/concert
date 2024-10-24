package com.chaw.helper;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DatabaseCleanupListener implements TestExecutionListener {


    @Override
    public void afterTestExecution(TestContext testContext) throws Exception {
        EntityManager entityManager = testContext.getApplicationContext().getBean(EntityManager.class);
        truncateAllTables(entityManager);
    }

    @Transactional
    public void truncateAllTables(EntityManager entityManager) {
        EntityManager em = entityManager.getEntityManagerFactory().createEntityManager();

        em.getTransaction().begin();

        List<String> tableNames = em.createNativeQuery(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'"
        ).getResultList();

        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

        for (String tableName : tableNames) {
            em.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
        }

        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();

        em.getTransaction().commit();
        em.close();
    }
}
