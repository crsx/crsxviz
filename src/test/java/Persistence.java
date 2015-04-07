import crsxviz.persistence.beans.ActiveRules;
import crsxviz.persistence.beans.Cookies;
import crsxviz.persistence.beans.Steps;
import crsxviz.persistence.services.DatabaseService;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class Persistence {
    
    private final DatabaseService ts = DatabaseService.getInstance("out.db");
    
    private List<Cookies> cookies;
    private List<Steps> steps;
    private List<ActiveRules> rules;
    
    @Before
    public void setUp() {
        cookies = ts.allCookies();
        steps = ts.allSteps();
        rules = ts.allRules();
    }

    @Test
    public void test() {
        assertEquals(18, cookies.size());
        assertEquals(1, cookies.get(1).getCookieId());
        
        assertEquals(22, steps.size());
        assertEquals(2, steps.get(1).getStepNum());
        
        assertEquals(14, rules.size());
        assertEquals(1, rules.get(1).getActiveRuleId());
    }
}
