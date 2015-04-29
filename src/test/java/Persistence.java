import crsxviz.persistence.beans.ActiveRules;
import crsxviz.persistence.beans.Cookies;
import crsxviz.persistence.beans.DispatchedRules;
import crsxviz.persistence.beans.RuleDetails;
import crsxviz.persistence.beans.Steps;
import crsxviz.persistence.services.DataService;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class Persistence {
    
    private final DataService ts = new DataService();
    
    private List<Cookies> cookies;
    private List<Steps> steps;
    private List<ActiveRules> rules;
    private List<DispatchedRules> dispatchedRules;
    private List<RuleDetails> ruleDetails;
    
    @Before
    public void setUp() {
        ts.setDataName("out.db");
        cookies = ts.allCookies();
        steps = ts.allSteps();
        rules = ts.allRules();
        dispatchedRules = ts.allDispatchedRules();
        ruleDetails = ts.getRuleDetails(rules.get(0).getValue());
    }

    @Test
    public void test() {
        assertEquals(18, cookies.size());
        assertEquals(1, cookies.get(1).getCookieId());
        
        assertEquals(23, steps.size());
        assertEquals(2, steps.get(1).getStepNum());
        
        assertEquals(14, rules.size());
        assertEquals(1, rules.get(1).getActiveRuleId());
        
        assertEquals(16, dispatchedRules.size());
        assertEquals( 7, dispatchedRules.get(0).getActiveRuleId());
        
        assertTrue(RuleDetails.toString(ruleDetails).contains("Compile-Finish[Compile-Helper[#1, y2, x13_0"));
    }
}
