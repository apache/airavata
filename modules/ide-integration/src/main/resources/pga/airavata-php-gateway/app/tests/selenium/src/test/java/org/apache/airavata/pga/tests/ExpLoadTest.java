package org.apache.airavata.pga.tests;

import org.apache.airavata.pga.tests.utils.UserLogin;
import org.junit.Before;
import org.junit.Test;

/*
 **********Executing Load Tests on Airavata Applications**********
 * Created by Eroma on 4/26/15.
 * The script to be used in load testing using PGA gateway
 * Updated to work with Latest PGA by Eroma 08/05/2015
*/
public class ExpLoadTest extends UserLogin {

    @Before
    public void setUp() throws Exception {}

    @Test
    public void runAll() throws Exception {
        System.out.println("============== Running all tests ==================");

        long startTime = System.nanoTime();
        int iterations1 = 1;
        for (int i=0; i < iterations1; ++i) {
            System.out.println("Starting AmberSanderBR2 iteration ...");
            AmberSanderBR2 t1 = new AmberSanderBR2();
            t1.setUp();
            t1.testAmberSanderBR2();
            t1.tearDown();
            System.out.println("AmberSanderBR2 iteration - Done");

            System.out.println("Starting AmberSanderStampede ...");
            AmberSanderStampede t2 = new AmberSanderStampede();
            t2.setUp();
            t2.testAmberSanderStampede();
            t2.tearDown();
            System.out.println("AmberSanderStampede - Done");

            System.out.println("Starting AmberSanderComet ...");
            AmberSanderComet t3 = new AmberSanderComet();
            t3.setUp();
            t3.testAmberSanderComet();
            t3.tearDown();
            System.out.println("AmberSanderComet - Done");

            System.out.println("Starting AutoDockBR2 ...");
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
            System.out.println("EchoBR2 - Done");

            System.out.println("Starting EchoStampede ...");
            EchoStampede t6 = new EchoStampede();
            t6.setUp();
            t6.testEchoStampede();
            t6.tearDown();
            System.out.println("EchoStampede - Done");

            System.out.println("Starting EchoComet ...");
            EchoComet t7 = new EchoComet();
            t7.setUp();
            t7.testEchoComet();
            t7.tearDown();
            System.out.println("EchoComet - Done");

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

            System.out.println("Starting GaussianComet ...");
            GaussianComet t10 = new GaussianComet();
            t10.setUp();
            t10.testGaussianComet();
            t10.tearDown();
            System.out.println("GaussianComet - Done");

            System.out.println("Starting GaussianGordon ...");
            GaussianGordon t11 = new GaussianGordon();
            t11.setUp();
            t11.testGaussianGordon();
            t11.tearDown();
            System.out.println("GaussianGordon - Done");

            System.out.println("Starting GromacsStampede ...");
            GromacsStampede t12 = new GromacsStampede();
            t12.setUp();
            t12.testGromacsStampede();
            t12.tearDown();
            System.out.println("GromacsStampede - Done");

            System.out.println("Starting LammpsStampede ...");
            LammpsStampede t13 = new LammpsStampede();
            t13.setUp();
            t13.testLammpsStampede();
            t13.tearDown();
            System.out.println("LammpsStampede - Done");

            System.out.println("Starting LammpsComet ...");
            LammpsComet t14 = new LammpsComet();
            t14.setUp();
            t14.testLammpsComet();
            t14.tearDown();
            System.out.println("LammpsComet - Done");

            System.out.println("Starting NwChemStampede ...");
            NwChemStampede t15 = new NwChemStampede();
            t15.setUp();
            t15.testNwChemStampede();
            t15.tearDown();
            System.out.println("NwChemStampede - Done");

            System.out.println("Starting NwChemComet ...");
            NwChemComet t16 = new NwChemComet();
            t16.setUp();
            t16.testNwChemComet();
            t16.tearDown();
            System.out.println("NwChemComet - Done");

            System.out.println("Starting PhastaPStampede ...");
            PhastaPStampede t17 = new PhastaPStampede();
            t17.setUp();
            t17.testPhastaPStampede();
            t17.tearDown();
            System.out.println("PhastaPStampede - Done");

            System.out.println("Starting TinkerMonteStampede ...");
            TinkerMonteStampede t18 = new TinkerMonteStampede();
            t18.setUp();
            t18.testTinkerMonteStampede();
            t18.tearDown();
            System.out.println("TinkerMonteStampede - Done");

            /*System.out.println("Starting TrinityStampede ...");
            TrinityStampede t19 = new TrinityStampede();
            t19.setUp();
            t19.testTrinityStampede();
            t19.tearDown();
            System.out.println("TrinityStampede - Done");*/

            System.out.println("Starting WRFStampede ...");
            WRFStampede t20 = new WRFStampede();
            t20.setUp();
            t20.testWRFStampede();
            t20.tearDown();
            System.out.println("WRFStampede - Done");
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        System.out.println("Time to execute All Experiments "
                + " : " +  (duration / 1000000000)/60 + " minutes.");


        System.out.println("==================== Done =========================");

    }
}
