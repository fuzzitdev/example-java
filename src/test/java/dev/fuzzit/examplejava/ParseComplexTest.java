package dev.fuzzit.examplejava;

import org.junit.runner.RunWith;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;

@RunWith(JQF.class)
public class ParseComplexTest {

    @Fuzz
    public void fuzz(String data)
    {
        ParseComplex.parse(data);
    }
}
