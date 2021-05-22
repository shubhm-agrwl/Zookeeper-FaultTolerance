package com.shubham.zookeeper.implemenation.conf;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import lombok.Data;

public @Data class ZookeeperServConf extends Configuration {

  @NotNull
  String zookeeperServerId;

  @NotNull
  @Valid
  @JsonProperty
  private ZookeeperConfig zookeeperConfig;

}
