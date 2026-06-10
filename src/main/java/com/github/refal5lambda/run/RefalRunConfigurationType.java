package com.github.refal5lambda.run;

import com.github.refal5lambda.RefalIcons;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.openapi.util.NotNullLazyValue;

/** "Refal-5λ" run configuration type, shown in Run/Debug Configurations. */
public final class RefalRunConfigurationType extends ConfigurationTypeBase {
    public static final String ID = "RefalLambdaRunConfiguration";

    public RefalRunConfigurationType() {
        super(ID, "Refal-5\u03bb", "Compile and run a Refal-5 Lambda program",
                NotNullLazyValue.createValue(() -> RefalIcons.FILE));
        addFactory(new RefalRunConfigurationFactory(this));
    }
}
