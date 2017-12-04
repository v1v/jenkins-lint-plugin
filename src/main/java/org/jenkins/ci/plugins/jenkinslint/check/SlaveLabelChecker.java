package org.jenkins.ci.plugins.jenkinslint.check;

import hudson.model.Node;
import org.jenkins.ci.plugins.jenkinslint.model.AbstractSlaveCheck;

import java.util.logging.Level;

/**
 * @author Victor Martinez
 */
public class    SlaveLabelChecker extends AbstractSlaveCheck {

    public SlaveLabelChecker(boolean enabled) {
        super(enabled);
        this.setDescription(Messages.SlaveLabelCheckerDesc());
        this.setSeverity(Messages.SlaveLabelCheckerSeverity());
    }

    public boolean executeCheck(Node item) {
        LOG.log(Level.FINER, "slave " + item.getDisplayName() + " labels " + item.getLabelString());
        return ( item.getLabelString() == null || item.getLabelString().equals("") );
    }
}
