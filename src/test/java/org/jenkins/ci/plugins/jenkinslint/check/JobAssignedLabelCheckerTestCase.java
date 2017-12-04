package org.jenkins.ci.plugins.jenkinslint.check;

import hudson.matrix.MatrixProject;
import hudson.maven.MavenModuleSet;
import hudson.model.FreeStyleProject;
import org.jenkins.ci.plugins.jenkinslint.AbstractTestCase;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * JobAssignedLabelChecker Test Case.
 *
 * @author Victor Martinez
 */
public class JobAssignedLabelCheckerTestCase extends AbstractTestCase {
    private JobAssignedLabelChecker checker = new JobAssignedLabelChecker(true);

    @Test public void testDefaultJob() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        assertTrue(checker.executeCheck(project));
    }
    @Test public void testEmptyAssignedLabel() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject("");
        assertTrue(checker.executeCheck(project));
    }
    @Test public void testWithAssignedLabel() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        j.createSlave("test",null);
        project.setAssignedLabel(j.jenkins.getLabel("test"));
        project.save();
        assertFalse(checker.executeCheck(project));
    }
    @Issue("JENKINS-42310")
    @Test public void testMavenModuleJob() throws Exception {
        MavenModuleSet project = j.createMavenProject();
        assertTrue(checker.executeCheck(project));
    }
    @Issue("JENKINS-42310")
    @Test public void testMavenWithAssignedLabel() throws Exception {
        MavenModuleSet project = j.createMavenProject();
        j.createSlave("test",null);
        project.setAssignedLabel(j.jenkins.getLabel("test"));
        project.save();
        assertFalse(checker.executeCheck(project));
    }
    @Issue("JENKINS-42310")
    @Test public void testMatrixProject() throws Exception {
        MatrixProject project = j.createMatrixProject();
        assertTrue(checker.executeCheck(project));
    }
    @Issue("JENKINS-42310")
    @Test public void testMatrixProjectWithAssignedLabel() throws Exception {
        MatrixProject project = j.createMatrixProject();
        j.createSlave("test",null);
        project.setAssignedLabel(j.jenkins.getLabel("test"));
        project.save();
        assertFalse(checker.executeCheck(project));
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
