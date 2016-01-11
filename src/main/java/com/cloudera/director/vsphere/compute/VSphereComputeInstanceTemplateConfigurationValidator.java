package com.cloudera.director.vsphere.compute;

import com.cloudera.director.spi.v1.model.ConfigurationValidator;
import com.cloudera.director.spi.v1.model.Configured;
import com.cloudera.director.spi.v1.model.LocalizationContext;
import com.cloudera.director.spi.v1.model.exception.PluginExceptionConditionAccumulator;
import com.cloudera.director.spi.v1.util.Preconditions;

/**
  * @author chiq
  *
  */

/**
 * Validates vSphere compute instance template configuration.
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class VSphereComputeInstanceTemplateConfigurationValidator implements ConfigurationValidator {

  /**
   * The vSphere compute provider.
   */
  private final VSphereComputeProvider provider;

  /**
   * Creates an vSphere compute instance template configuration validator with the specified
   * parameters.
   *
   * @param provider the vSphere compute provider
   */
  public VSphereComputeInstanceTemplateConfigurationValidator(VSphereComputeProvider provider) {
    this.provider = Preconditions.checkNotNull(provider, "provider");
  }

  @Override
  public void validate(String name, Configured configuration, PluginExceptionConditionAccumulator accumulator,
      LocalizationContext localizationContext) {

    // TODO add validations
  }
}
