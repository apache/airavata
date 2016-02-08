package org.apache.airavata.workflow.core.parser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Created by syodage on 2/8/16.
 */
public class JsonWorkflowParserTest {

    private String workflowString;


    @Before
    public void setUp() throws Exception {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("TestWorkflow.json");
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testParse() throws Exception {

    }
}