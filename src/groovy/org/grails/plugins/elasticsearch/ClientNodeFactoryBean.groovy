package org.grails.plugins.elasticsearch

import org.springframework.beans.factory.FactoryBean
import static org.elasticsearch.node.NodeBuilder.*
import org.apache.commons.lang.NotImplementedException
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.apache.log4j.Logger

class ClientNodeFactoryBean implements FactoryBean {
  def elasticSearchContextHolder
  static SUPPORTED_MODES = ['local', 'transport', 'node']

  Logger log = Logger.getLogger(this.class)

  Object getObject() {
    // Retrieve client mode, default is "node"
    def clientMode = elasticSearchContextHolder.config.client.mode ?: 'node'
    if(!(clientMode in SUPPORTED_MODES)) {
      throw new IllegalArgumentException("Invalid client mode, expected values were ${SUPPORTED_MODES}.")
    }

    def nb = nodeBuilder()
    def transportClient = null
    switch(clientMode) {
      case 'local':
        def storeType = elasticSearchContextHolder.config.index.store.type
        if (storeType) {
          nb.settings().put('index.store.type', storeType as String)
          log.debug "Local ElasticSearch client with store type of ${storeType} configured."
        } else {
          log.debug "Local ElasticSearch client with default store type configured."
        }
        nb.local(true)
        break
      case 'transport':
        transportClient = new TransportClient()
        if(!elasticSearchContextHolder.config.client.hosts){
          transportClient.addTransportAddress(new InetSocketTransportAddress('localhost', 9300))
          log.debug "Transport based ElasticSearch client with default address [localhost:9300] configured"
        } else {
          String bindAddresses = ""
          elasticSearchContextHolder.config.client.hosts.each { address ->
            bindAddresses += "${address.host}:${address.port},"
            transportClient.addTransportAddress(new InetSocketTransportAddress(address.host, address.port))
          }
          log.debug "Transport based ElasticSearch client, connected to [${bindAddresses}] configured"
        }
        break
      case 'node':
      default:
        nb.client(true)
        log.debug "ElasticSearch node configured."
    }

    return transportClient ?: nb.node().client()
  }

  Class getObjectType() {
    return org.elasticsearch.client.Client
  }

  boolean isSingleton() {
    return true
  }
}
