package org.jenkins.ci.plugins.jenkinslint;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.xml.XmlPage;
import hudson.model.FreeStyleProject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

/**
 * @author victor.martinez.
 */
public class JenkinsLintActionTestCase {

    private static final String EMPTY_TABLE = "<tbody id=\"jenkinsLintTableBody\"></tbody></table>";
    private static final String URL = "jenkinslint";

    @Rule
    public JenkinsRule j = new JenkinsRule();
    @Test
    public void testEmptyJob() throws Exception {
        HtmlPage page = j.createWebClient().goTo(URL);
        WebAssert.assertTextPresent(page, "JenkinsLint");
        assertTrue(page.getWebResponse().getContentAsString().contains(EMPTY_TABLE));
        WebAssert.assertTextPresent(page, "JL-1");
    }
    @Test
    public void testJob() throws Exception {
        String jobName = "JOB";
        FreeStyleProject project = j.createFreeStyleProject(jobName);
        HtmlPage htmlPage = j.createWebClient().goTo(URL);
        WebAssert.assertTextPresent(htmlPage, "JenkinsLint");
        assertFalse(htmlPage.getWebResponse().getContentAsString().contains(EMPTY_TABLE));
        assertTrue(htmlPage.getWebResponse().getContentAsString().contains(j.getURL().toString() + project.getUrl()));
        WebAssert.assertTextPresent(htmlPage, "JL-1");
        Page page = j.createWebClient().goTo("jenkinslint/api/xml", "application/xml");
        assertThat(page.getWebResponse().getContentAsString(), containsString(jobName));
    }
    @Test
    public void testAPI() throws Exception {
        Page page = j.createWebClient().goTo("jenkinslint/api/xml", "application/xml");
        assertThat(page.getWebResponse().getContentAsString(), containsString("jenkinsLintAction"));

        XmlPage p = j.createWebClient().goToXml("jenkinslint/api/xml");
        for (DomNode element: p.getFirstChild().getChildNodes()){
            assertNotEquals(0, element.getChildNodes());
        }
        assertEquals("checkSet", p.getFirstChild().getChildNodes().get(0).getNodeName());
        assertEquals("jobSet", p.getFirstChild().getChildNodes().get(1).getNodeName());
        assertEquals("slaveCheckSet", p.getFirstChild().getChildNodes().get(2).getNodeName());
        assertEquals("slaveSet", p.getFirstChild().getChildNodes().get(3).getNodeName());
    }
    @Test
    public void testUITable() throws Exception {
        String jobName = "JOB";
        FreeStyleProject project = j.createFreeStyleProject(jobName);
        HtmlPage page = j.createWebClient().goTo(URL);
        assertFalse(page.getWebResponse().getContentAsString().contains(EMPTY_TABLE));
        String content = page.getWebResponse().getContentAsString();
        assertTrue(content.contains(j.getURL().toString() + project.getUrl()));
        assertTrue(content.contains(htmlLint("JobNameChecker", "JL-1")));
        assertTrue(content.contains(htmlLint("JobDescriptionChecker", "JL-2")));
        assertTrue(content.contains(htmlLint("JobAssignedLabelChecker", "JL-3")));
        assertTrue(content.contains(htmlLint("MasterLabelChecker", "JL-4")));
        assertTrue(content.contains(htmlLint("JobLogRotatorChecker", "JL-5")));
        assertTrue(content.contains(htmlLint("MavenJobTypeChecker", "JL-6")));
        assertTrue(content.contains(htmlLint("CleanupWorkspaceChecker", "JL-7")));
        assertTrue(content.contains(htmlLint("JavadocChecker", "JL-8")));
        assertTrue(content.contains(htmlLint("ArtifactChecker", "JL-9")));
        assertTrue(content.contains(htmlLint("NullSCMChecker", "JL-10")));
        assertTrue(content.contains(htmlLint("PollingSCMTriggerChecker", "JL-11")));
        assertTrue(content.contains(htmlLint("GitShallowChecker", "JL-12")));
        assertTrue(content.contains(htmlLint("MultibranchJobTypeChecker", "JL-13")));
        assertTrue(content.contains(htmlLint("HardcodedScriptChecker", "JL-14")));
        assertTrue(content.contains(htmlLint("GradleWrapperChecker", "JL-15")));
        assertTrue(content.contains(htmlLint("TimeoutChecker", "JL-16")));
        assertTrue(content.contains(htmlLint("GroovySystemExitChecker", "JL-17")));
        assertTrue(content.contains(htmlLint("GitRefChecker", "JL-18")));
        assertTrue(content.contains(htmlLint("TimerTriggerChecker", "JL-19")));
        assertTrue(content.contains(htmlLint("GitRefSubmoduleChecker", "JL-20")));
    }

    private String htmlLint (String name, String id) {
        return "<th tooltip=\"" +  name + "\" class=\"pane-header\">" + id + "</th>";
    }
}
