package com.shubham.zookeeper.implemenation.conf;

import lombok.Data;

@Data
public class ZookeeperConfig {

  private String zookeeperServers;

  private String rootNode;

  private String processNodePrefix;

  private int sessionTimeout;

}
