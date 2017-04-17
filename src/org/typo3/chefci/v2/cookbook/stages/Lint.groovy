package org.typo3.chefci.v2.cookbook.stages

import org.typo3.chefci.helpers.JenkinsHelper
import org.typo3.chefci.helpers.Slack
import org.typo3.chefci.v2.shared.stages.AbstractStage

class Lint extends AbstractStage {

    Lint(Object script, JenkinsHelper jenkinsHelper, Slack slack) {
        super(script, 'Lint', jenkinsHelper, slack)
    }

    @Override
    void execute() {
        script.stage(stageName) {
            script.node {
                foodcritic()
                cookstyle()
                publishWarnings()
            }
        }
    }

    private foodcritic() {
        def exitCode = script.sh('foodcritic .', returnStatus: true)
        if exitCode > 0 {
            script.echo "test-kitchen returned non-zero exit status. Ignoring."
        }
    }

    private cookstyle() {
        script.sh('cookstyle --fail-level E')
    }

    private publishWarnings() {
        // see also http://atomic-penguin.github.io/blog/2014/04/29/stupid-jenkins-and-chef-tricks-part-1-rubocop/
        script.step([$class         : 'WarningsPublisher', canComputeNew: false, canResolveRelativePaths: false,
                     defaultEncoding: '', excludePattern: '', healthy: '', includePattern: '', unHealthy: '',
                     consoleParsers : [[parserName: 'Foodcritic'], [parserName: 'Rubocop']]])
        script.step([$class: 'AnalysisPublisher'])
    }
}
