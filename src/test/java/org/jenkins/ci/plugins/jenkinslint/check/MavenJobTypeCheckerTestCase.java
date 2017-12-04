package org.jenkins.ci.plugins.jenkinslint.check;

import hudson.maven.MavenModuleSet;
import hudson.model.FreeStyleProject;
import org.jenkins.ci.plugins.jenkinslint.AbstractTestCase;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * MavenJobTypeChecker Test Case.
 *
 * @author Victor Martinez
 */
public class MavenJobTypeCheckerTestCase extends AbstractTestCase {
    private MavenJobTypeChecker checker = new MavenJobTypeChecker(true);

    @Test public void testEmptyJob() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        assertFalse(checker.executeCheck(project));
    }
    @Test public void testEmptyMavenModuleJob() throws Exception {
        MavenModuleSet project = j.createMavenProject();
        assertTrue(checker.executeCheck(project));
    }
    @Test public void testMavenModuleJob() throws Exception {
        MavenModuleSet project = j.createMavenProject("test");
        assertTrue(checker.executeCheck(project));
    }
    @Test public void testControlComment() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        assertFalse(checker.isIgnored(project.getDescription()));
        project.setDescription("#lint:ignore:" + checker.getClass().getSimpleName());
        assertTrue(checker.isIgnored(project.getDescription()));
    }
    @Test public void testWorkflowJob() throws Exception {
        assertFalse(checker.executeCheck(createWorkflow(null, true)));
    }
}
