package org.jenkins.ci.plugins.jenkinslint.check;

import hudson.PluginWrapper;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Project;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import jenkins.model.Jenkins;
import org.jenkins.ci.plugins.jenkinslint.model.AbstractCheck;

import java.util.List;
import java.util.logging.Level;

/**
 * @author Victor Martinez
 */
public class GroovySystemExitChecker extends AbstractCheck {

    public GroovySystemExitChecker(boolean enabled) {
        super(enabled);
        this.setDescription(Messages.GroovySystemExitCheckerDesc());
        this.setSeverity(Messages.GroovySystemExitCheckerSeverity());
    }

    public boolean executeCheck(Item item) {
        boolean found = false;
        if (item.getClass().getSimpleName().equals("MavenModuleSet")) {
            try {
                Object getPrebuilders = item.getClass().getMethod("getPrebuilders", null).invoke(item);
                if (getPrebuilders instanceof List && isSystemExit((List) getPrebuilders)) {
                    found = true;
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
                if (getBuilders instanceof List && isSystemExit((List) getBuilders)) {
                    found = true;
                }
            }catch (Exception e) {
                LOG.log(Level.WARNING, "Exception " + e.getMessage(), e.getCause());
            }
        }

        if (item instanceof AbstractProject) {
            if (isSystemExitInPublisher(((AbstractProject) item).getPublishersList())) {
                found = true;
            }

            if (((AbstractProject) item).getProperty(ParametersDefinitionProperty.class) != null) {
                if (isSystemExitInParameters(((ParametersDefinitionProperty) ((AbstractProject) item).getProperty(ParametersDefinitionProperty.class)).getParameterDefinitions())) {
                    found = true;
                }
            }
        }

        if (item instanceof Job) {
            // Pipeline support
            if (item.getClass().getSimpleName().equals("WorkflowJob")) {
                try {
                    Object getDefinition = item.getClass().getMethod("getDefinition", null).invoke(item);
                    if (getDefinition.getClass().getSimpleName().equals("CpsFlowDefinition")) {
                        if (containsSystemExit(getDefinition.getClass().getMethod("getScript",null).invoke(getDefinition))) {
                            found = true;
                        }
                    }
                } catch (Exception e) {
                    LOG.log(Level.FINE, "Exception " + e.getMessage(), e.getCause());
                }
            }
        }
        return found;
    }

    private boolean isSystemExit(List<Builder> builders) {
        boolean status = false;
        if (builders != null && builders.size() > 0 ) {
            for (Builder builder : builders) {
                if (builder.getClass().getName().endsWith("SystemGroovy")) {
                    try {
                        // INFO: Groovy Version 2.0 support Secured Sandbox and changed its implementation
                        PluginWrapper plugin = Jenkins.getInstance().pluginManager.getPlugin("groovy");
                        if (plugin!=null && plugin.getVersionNumber().isOlderThan(new hudson.util.VersionNumber("2.0.0"))) {
                            Object scriptSource = builder.getClass().getMethod("getScriptSource", null).invoke(builder);
                            if (scriptSource.getClass().getName().endsWith("StringScriptSource")) {
                                if (containsSystemExit(scriptSource.getClass().getMethod("getCommand", null).invoke(scriptSource))) {
                                    status = true;
                                }
                            }
                        } else {
                            Object scriptSource = builder.getClass().getMethod("getSource", null).invoke(builder);
                            if (scriptSource.getClass().getName().endsWith("StringSystemScriptSource")) {
                                Object script = scriptSource.getClass().getMethod("getScript", null).invoke(scriptSource);
                                if (script != null && script.getClass().getName().endsWith("SecureGroovyScript")) {
                                    if (containsSystemExit(script.getClass().getMethod("getScript", null).invoke(script))) {
                                        status = true;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Exception " + e.getMessage(), e.getCause());
                    }
                }
            }
        }
        return status;
    }

    private boolean isSystemExitInPublisher (DescribableList<Publisher, Descriptor<Publisher>> publishersList) {
        boolean status = false;
        if (publishersList != null) {
            for (Publisher publisher : publishersList) {
                if (publisher.getClass().getName().endsWith("GroovyPostbuildRecorder")) {
                    LOG.log(Level.FINEST, "GroovyPostbuildRecorder " + publisher);
                    try {
                        Object scriptSource = publisher.getClass().getMethod("getScript", null).invoke(publisher);
                        if (scriptSource.getClass().getName().endsWith("SecureGroovyScript")) {
                            if (containsSystemExit(scriptSource.getClass().getMethod("getScript", null).invoke(scriptSource))) {
                                status = true;
                            }
                        }
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Exception " + e.getMessage(), e.getCause());
                    }
                }
            }
        }
        return status;
    }

    private boolean isSystemExitInParameters (List<hudson.model.ParameterDefinition> properties) {
        boolean status = false;
        if (properties != null) {
            for (ParameterDefinition property : properties) {
                if (property.getClass().getName().endsWith("ChoiceParameter") ||
                        property.getClass().getName().endsWith("CascadeChoiceParameter") ||
                        property.getClass().getName().endsWith("DynamicReferenceParameter")) {
                    LOG.log(Level.FINEST, "unochoice " + property);
                    try {
                        Object scriptSource = property.getClass().getMethod("getScript", null).invoke(property);
                        if (scriptSource.getClass().getName().endsWith("GroovyScript")) {
                            Object script = scriptSource.getClass().getMethod("getScript", null).invoke(scriptSource);
                            if (script != null && script.getClass().getName().endsWith("SecureGroovyScript")) {
                                if (containsSystemExit(script.getClass().getMethod("getScript", null).invoke(script))) {
                                    status = true;
                                }
                            }
                        }
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Exception " + e.getMessage(), e.getCause());
                    }
                }
            }
        }
        return status;
    }

    private boolean containsSystemExit (Object command) {
        boolean status = false;
        if (command !=null && command instanceof String) {
            // TODO: Parse to search for non comments, otherwise some false positives!
            status = (command != null && ((String) command).toLowerCase().contains("system.exit"));
            LOG.log(Level.FINE, "isSystemExit " + status);
        }
        return status;
    }
}
