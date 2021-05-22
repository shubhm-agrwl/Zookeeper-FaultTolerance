package com.shubham.zookeeper.implementation;

import com.shubham.zookeeper.implemenation.conf.ZookeeperServConf;
import com.shubham.zookeeper.implementation.core.ProcessNode;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

/**
 * @author Shubham Agrawal
 *
 */
public class ZookeeperServer extends Application<ZookeeperServConf> {
  @Override
  protected void bootstrapLogging() {}

  public static void main(String[] args) throws Exception {
    new ZookeeperServer().run(args);
  }

  @Override
  public void run(ZookeeperServConf config, Environment environment) throws Exception {

    // Initiating Zookeeper interaction
    new ProcessNode(config);

  }
}
