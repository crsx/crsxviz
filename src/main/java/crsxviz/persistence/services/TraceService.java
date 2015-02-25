package crsxviz.persistence.services;

import crsxviz.persistence.beans.ActiveRules;
import crsxviz.persistence.beans.Cookies;
import crsxviz.persistence.beans.Steps;
import java.util.List;
import javax.annotation.PostConstruct;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class TraceService {
	
    private EntityManager em;
    private EntityManagerFactory emf;
    private EntityTransaction et;

    public void testInstance() {
        this.emf = Persistence.createEntityManagerFactory("crsxviz");
        this.em = this.emf.createEntityManager();
        this.et = this.em.getTransaction();
    }
    
    @PostConstruct
    public void init() {
        this.emf = Persistence.createEntityManagerFactory("crsxviz");
        this.em = this.emf.createEntityManager();
        this.et = this.em.getTransaction();
    }

    @SuppressWarnings("unchecked")
    public List<ActiveRules> allRules() {
            return this.em.createNamedQuery(ActiveRules.getAll).getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Cookies> allCookies() {
            return this.em.createNamedQuery(Cookies.getAll).getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Steps> allSteps() {
            return this.em.createNamedQuery(Steps.getAll).getResultList();
    }
	
    public void close() {
        et.begin();
        et.commit();
        em.close();
    }
    
    public void save() {
        et.begin();
        em.flush();
        et.commit();
    }
}
