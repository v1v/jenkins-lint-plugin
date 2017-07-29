package org.jenkins.ci.plugins.jenkinslint.check;

import hudson.model.Item;
import hudson.model.Project;
import hudson.tasks.Builder;
import hudson.tasks.CommandInterpreter;
import org.jenkins.ci.plugins.jenkinslint.model.AbstractCheck;

import java.util.List;
import java.util.logging.Level;

/**
 * @author Victor Martinez
 */
public class HardcodedScriptChecker extends AbstractCheck {

    public final static int THRESHOLD = 2;

    public HardcodedScriptChecker() {
        super();

        this.setDescription(Messages.HardcodedScriptCheckerDesc());
        this.setSeverity(Messages.HardcodedScriptCheckerSeverity());
    }

    public boolean executeCheck(Item item) {
        LOG.log(Level.FINE, "executeCheck " + item);
        boolean found = false;
        if (item.getClass().getSimpleName().equals("MavenModuleSet")) {
            try {
                Object getPrebuilders = item.getClass().getMethod("getPrebuilders", null).invoke(item);
                if (getPrebuilders instanceof List) {
                    found = isBuilderHarcoded((List) getPrebuilders);
                }
            }catch (Exception e) {
                LOG.log(Level.WARNING, "Exception " + e.getMessage(), e.getCause());
            }
        }
        if (item instanceof Project) {
            found = isBuilderHarcoded (((Project)item).getBuilders());
        }
        if (item.getClass().getSimpleName().equals("MatrixProject")) {
            try {
                Object getBuilders = item.getClass().getMethod("getBuilders", null).invoke(item);
                if (getBuilders instanceof List) {
                    found = isBuilderHarcoded((List) getBuilders);
                }
            }catch (Exception e) {
                LOG.log(Level.WARNING, "Exception " + e.getMessage(), e.getCause());
            }
        }
        return found;
    }

    private boolean isBuilderHarcoded (List<Builder> builders) {
        boolean found = false;
        if (builders != null && builders.size() > 0 ) {
            for (Builder builder : builders) {
                if (builder instanceof hudson.tasks.Shell || builder instanceof hudson.tasks.BatchFile) {
                    if (isHarcoded (((CommandInterpreter)builder).getCommand(), THRESHOLD)) {
                        found = true;
                    }
                }
            }
        } else {
            found = false;
        }
        return found;
    }

    private boolean isHarcoded (String content, int threshold) {
        if (content != null) {
            return content.split("\r\n|\r|\n").length > threshold;
        } else {
            return false;
        }
    }
}
