package org.jenkins.ci.plugins.jenkinslint.check;

import hudson.model.FreeStyleProject;
import hudson.tasks.LogRotator;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * JobLogRotatorChecker Test Case.
 *
 * @author Victor Martinez
 */
public class JobLogRotatorCheckerTestCase extends AbstractCheckerTestCase {
    private JobLogRotatorChecker checker = new JobLogRotatorChecker();

    @Test public void testDefaultJob() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        assertTrue(checker.executeCheck(project));
    }
    @Test public void testEmptyLogRotatorJob() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject("");
        project.setBuildDiscarder(new LogRotator(null,null,null,null));
        assertTrue(checker.executeCheck(project));
    }
    @Test public void testLogRotatorJob() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject("");
        project.setBuildDiscarder(new LogRotator(-1,-1,-1,-1));
        assertTrue(checker.executeCheck(project));
    }
    @Test public void testControlComment() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        assertFalse(checker.isIgnored(project.getDescription()));
        project.setDescription("#lint:ignore:" + checker.getClass().getSimpleName());
        assertTrue(checker.isIgnored(project.getDescription()));
    }
}
