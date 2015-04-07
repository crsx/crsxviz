package crsxviz.persistence.services;

import crsxviz.persistence.beans.ActiveRules;
import crsxviz.persistence.beans.CompiledSteps;
import crsxviz.persistence.beans.Cookies;
import crsxviz.persistence.beans.DispatchedRules;
import crsxviz.persistence.beans.Steps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.eclipse.persistence.config.PersistenceUnitProperties;

public class JpaService {

    private static final String DEFAULT_DATABASE = "out.db";

    private ObservableList<String> breakpoints;

    private EntityManager em;
    private EntityManagerFactory emf;
    private EntityTransaction et;
    
    private String dbName;
    private String url;

    public void init() {
        this.init(DEFAULT_DATABASE);
    }

    public void init(String dbpath) {
        dbName = dbpath;
        breakpoints = FXCollections.observableArrayList();
        url = "jdbc:sqlite:" + dbpath;
        if (!url.endsWith(".db")) {
            url += ".db";
        }
        Map overriddenProps = new HashMap();
        overriddenProps.put(PersistenceUnitProperties.JDBC_URL, url);
        this.emf = Persistence.createEntityManagerFactory("crsxviz", overriddenProps);
        this.em = this.emf.createEntityManager();
        this.et = this.em.getTransaction();
    }

    public String getDbName() {
        return (dbName != null) ? dbName : "";
    }

    public List<ActiveRules> allRules() {
        return this.em.createNamedQuery(ActiveRules.getAll).getResultList();
    }

    public List<Cookies> allCookies() {
        return this.em.createNamedQuery(Cookies.getAll).getResultList();
    }

    public List<Steps> allSteps() {
        return this.em.createNamedQuery(Steps.getAll).getResultList();
    }

    public List<CompiledSteps> allCompiledSteps() {
        return CompiledSteps.loadAll(url);
    }

    public CompiledSteps getCompiledStep(Long num) {
        return CompiledSteps.loadStep(url, num);
    }

    public List<DispatchedRules> allDispatchedRules() {
        return DispatchedRules.loadAllDispatchtedRules(url);
    }
    
    public ObservableList<String> allObservableBreakpoints() {
        return breakpoints;
    }

    public ObservableList<String> allObservableRules() {
        ObservableList list = FXCollections.observableArrayList();
        allRules().stream().forEach(
                (rule) -> list.add(rule.getValue())
        );
        return list;
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
