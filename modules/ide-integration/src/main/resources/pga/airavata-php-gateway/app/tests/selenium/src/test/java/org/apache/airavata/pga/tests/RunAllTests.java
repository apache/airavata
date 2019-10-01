package org.apache.airavata.pga.tests;

import org.apache.airavata.pga.tests.utils.UserLogin;
import org.junit.Before;
import org.junit.Test;

/*
 **********Executing All Tests on Airavata Applications**********
 * Created by Eroma on 4/26/15.
 * The script is to execute all aApplication experiments as a bundle.
 * Updated to work with Latest PGA by Eroma 08/05/2015.
*/
public class RunAllTests extends UserLogin {
    @Before
    public void setUp() throws Exception {}

    @Test
    public void runAll() throws Exception {
        System.out.println("============== Running all tests ==================");

        long startTime = System.nanoTime();

        System.out.println("Starting CreateModifySearchProject ...");
        CreateModifySearchProject t0 = new CreateModifySearchProject();
        t0.setUp();
        t0.testCreateModifySearchProject();
        t0.tearDown();
        System.out.println("CreateModifySearchProject - Done");

        /*System.out.println("Starting AmberSanderBR2 ...");
        AmberSanderBR2 t1 = new AmberSanderBR2();
        t1.setUp();
        t1.testAmberSanderBR2();
        t1.tearDown();
        System.out.println("AmberSanderBR2 - Done");*/

        System.out.println("Starting AmberSanderComet ...");
        AmberSanderComet t2 = new AmberSanderComet();
        t2.setUp();
        t2.testAmberSanderComet();
        t2.tearDown();
        System.out.println("AmberSanderComet - Done");

        System.out.println("Starting AmberSanderStampede ...");
        AmberSanderStampede t3 = new AmberSanderStampede();
        t3.setUp();
        t3.testAmberSanderStampede();
        t3.tearDown();
        System.out.println("AmberSanderStampede - Done");

        /*System.out.println("Starting AutoDockBR2 ...");
        AutoDockBR2 t4 = new AutoDockBR2();
        t4.setUp();
        t4.testAutoDockBR2();
        t4.tearDown();
        System.out.println("AutoDockBR2 - Done");

        System.out.println("Starting EchoBR2 ...");
        EchoBR2 t5 = new EchoBR2();
        t5.setUp();
        t5.testEchoBR2();
        t5.tearDown();
        System.out.println("EchoBR2 - Done");*/

        System.out.println("Starting EchoComet...");
        EchoComet t6 = new EchoComet();
        t6.setUp();
        t6.testEchoComet();
        t6.tearDown();
        System.out.println("EchoTrestles - Done");

        System.out.println("Starting EchoStampede ...");
        EchoStampede t7 = new EchoStampede();
        t7.setUp();
        t7.testEchoStampede();
        t7.tearDown();
        System.out.println("EchoStampede - Done");

        System.out.println("Starting EspressoStampede ...");
        EspressoStampede t8 = new EspressoStampede();
        t8.setUp();
        t8.testEspressoStampede();
        t8.tearDown();
        System.out.println("EspressoStampede - Done");

        System.out.println("Starting GamessGordon ...");
        GamessGordon t9 = new GamessGordon();
        t9.setUp();
        t9.testGamessGordon();
        t9.tearDown();
        System.out.println("GamessGordon - Done");

        System.out.println("Starting GaussianGordon ...");
        GaussianGordon t10 = new GaussianGordon();
        t10.setUp();
        t10.testGaussianGordon();
        t10.tearDown();
        System.out.println("GaussianGordon - Done");

        System.out.println("Starting GromacsStampede ...");
        GromacsStampede t11 = new GromacsStampede();
        t11.setUp();
        t11.testGromacsStampede();
        t11.tearDown();
        System.out.println("GromacsStampede - Done");

        System.out.println("Starting LammpsComet ...");
        LammpsComet t12 = new LammpsComet();
        t12.setUp();
        t12.testLammpsComet();
        t12.tearDown();
        System.out.println("LammpsComet - Done");

        System.out.println("Starting LammpsStampede ...");
        LammpsStampede t13 = new LammpsStampede();
        t13.setUp();
        t13.testLammpsStampede();
        t13.tearDown();
        System.out.println("LammpsStampede - Done");

        System.out.println("Starting NwChemComet ...");
        NwChemComet t14 = new NwChemComet();
        t14.setUp();
        t14.testNwChemComet();
        t14.tearDown();
        System.out.println("NwChemComet - Done");

        System.out.println("Starting NwChemStampede ...");
        NwChemStampede t15 = new NwChemStampede();
        t15.setUp();
        t15.testNwChemStampede();
        t15.tearDown();
        System.out.println("NwChemStampede - Done");

        System.out.println("Starting PhastaPStampede ...");
        PhastaPStampede t16 = new PhastaPStampede();
        t16.setUp();
        t16.testPhastaPStampede();
        t16.tearDown();
        System.out.println("PhastaPStampede - Done");

        System.out.println("Starting TinkerMonteStampede ...");
        TinkerMonteStampede t17 = new TinkerMonteStampede();
        t17.setUp();
        t17.testTinkerMonteStampede();
        t17.tearDown();
        System.out.println("TinkerMonteStampede - Done");

        /*System.out.println("Starting TrinityStampede ...");
        TrinityStampede t18 = new TrinityStampede();
        t18.setUp();
        t18.testTrinityStampede();
        t18.tearDown();
        System.out.println("TrinityStampede - Done");*/

        System.out.println("Starting WRFStampede ...");
        WRFStampede t19 = new WRFStampede();
        t19.setUp();
        t19.testWRFStampede();
        t19.tearDown();
        System.out.println("WRFStampede - Done");

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        System.out.println("Time to execute Experiment Tests "
                + " : " +  (duration / 1000000000)/60 + " minutes.");

        System.out.println("==================== Done =========================");

    }
}
