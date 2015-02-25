import crsxviz.persistence.beans.ActiveRules;
import crsxviz.persistence.services.TraceService;
import crsxviz.persistence.beans.Cookies;
import crsxviz.persistence.beans.Steps;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author nathanielward
 */
public class Persistence {
    
    private TraceService ts;
    
    private List<Cookies> cookies;
    private List<Steps> steps;
    private List<ActiveRules> rules;
    
    @Before
    public void setUp() {
        ts = new TraceService();
        ts.testInstance();
        
        cookies = ts.allCookies();
        steps = ts.allSteps();
        rules = ts.allRules();
    }

    @Test
    public void test() {
        assertEquals(11, cookies.size());
        assertEquals(1, cookies.get(1).getCookieId());
        
        assertEquals(20, steps.size());
        assertEquals(2, steps.get(1).getStepNum());
        
        assertEquals(6, rules.size());
        assertEquals(1, rules.get(1).getActiveRuleId());
        
        ts.close();
    }
}
