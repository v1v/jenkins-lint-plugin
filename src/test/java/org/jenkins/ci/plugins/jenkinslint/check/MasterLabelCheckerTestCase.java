package org.jenkins.ci.plugins.jenkinslint.check;

import hudson.model.FreeStyleProject;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * MasterLabelCheckerTestCase Test Case.
 *
 * @author Victor Martinez
 */
public class MasterLabelCheckerTestCase extends AbstractCheckerTestCase {
    private MasterLabelChecker checker = new MasterLabelChecker();

    @Test public void testDefaultJob() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        assertFalse(checker.executeCheck(project));
    }
    @Test public void testNonDefaultJob() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        j.createSlave("abc", null);
        project.setAssignedLabel(j.jenkins.getLabel("abc"));
        project.save();
        assertFalse(checker.executeCheck(project));
    }
    @Test public void testWithAssignedLabel() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        project.setAssignedLabel(j.jenkins.getLabel("master"));
        project.save();
        assertTrue(checker.executeCheck(project));
    }
    @Test public void testControlComment() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        assertFalse(checker.isIgnored(project.getDescription()));
        project.setDescription("#lint:ignore:" + checker.getClass().getSimpleName());
        assertTrue(checker.isIgnored(project.getDescription()));
    }
}
