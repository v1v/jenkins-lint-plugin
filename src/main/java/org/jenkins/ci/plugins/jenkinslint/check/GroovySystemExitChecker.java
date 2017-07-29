package org.jenkins.ci.plugins.jenkinslint.check;

import hudson.model.Item;
import hudson.model.Project;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;
import org.jenkins.ci.plugins.jenkinslint.model.AbstractCheck;

import java.util.List;
import java.util.logging.Level;

/**
 * @author Victor Martinez
 */
public class GroovySystemExitChecker extends AbstractCheck {

    public GroovySystemExitChecker() {
        super();
        this.setDescription(Messages.GroovySystemExitCheckerDesc());
        this.setSeverity(Messages.GroovySystemExitCheckerSeverity());
    }

    public boolean executeCheck(Item item) {
        boolean found = false;
        if (Jenkins.getInstance().pluginManager.getPlugin("groovy") != null) {

            if (item.getClass().getSimpleName().equals("MavenModuleSet")) {
                try {
                    Object getPrebuilders = item.getClass().getMethod("getPrebuilders", null).invoke(item);
                    if (getPrebuilders instanceof List) {
                        found = isSystemExit((List) getPrebuilders);
                    }
                }catch (Exception e) {
                    LOG.log(Level.WARNING, "Exception " + e.getMessage(), e.getCause());
                }
            }
            if (item instanceof Project) {
                found = isSystemExit(((Project) item).getBuilders());
            }
            if (item.getClass().getSimpleName().equals("MatrixProject")) {
                try {
                    Object getBuilders = item.getClass().getMethod("getBuilders", null).invoke(item);
                    if (getBuilders instanceof List) {
                        found = isSystemExit((List) getBuilders);
                    }
                }catch (Exception e) {
                    LOG.log(Level.WARNING, "Exception " + e.getMessage(), e.getCause());
                }
            }
        } else {
            LOG.log(Level.INFO, "Groovy is not installed");
        }
        return found;
    }

    private boolean isSystemExit (List<Builder> builders) {
        boolean status = false;
        if (builders != null && builders.size() > 0 ) {
            for (Builder builder : builders) {
                if (builder.getClass().getName().endsWith("SystemGroovy")) {
                    try {
                        Object scriptSource = builder.getClass().getMethod("getScriptSource",null).invoke(builder);
                        if (scriptSource.getClass().getName().endsWith("StringScriptSource")) {
                            Object command = scriptSource.getClass().getMethod("getCommand",null).invoke(scriptSource);
                            if (command instanceof String) {
                                // TODO: Parse to search for non comments, otherwise some false positives!
                                status = (command != null && ((String) command).toLowerCase().contains("system.exit"));
                                LOG.log(Level.FINE, "isSystemExit " + status);
                            }
                        }
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Exception " + e.getMessage(), e.getCause());
                        status = false;
                    }
                }
            }
        }
        return status;
    }

}
