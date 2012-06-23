package com.traackr.scalastic.elasticsearch

import org.elasticsearch.action.support.broadcast._
import org.elasticsearch.common.settings.ImmutableSettings._
import org.elasticsearch.index.query._, QueryBuilders._
import org.elasticsearch.common.unit._, TimeValue._
import scala.collection._, JavaConversions._

trait IndexCrud
    extends Exists
    with IndexCreate with IndexDelete
    with Open with Close
    with Alias with Unalias
    with PutMapping with DeleteMapping
    with PutTemplate with DeleteTemplate
    with ClearCache
    with Flush
    with Refresh
    with Optimize
    with GatewaySnapshot
    with Segments
    with Status
    with Stats
    with ValidateQuery
    with UpdateSettings {
  self: Indexer =>
}

trait IndexCreate {
  self: Indexer =>

  def createIndex(index: String, settings: Map[String, String] = Map(), mappings: Map[String, String] = Map(), cause: Option[String] = None, timeout: Option[String] = None) =
    createIndex_send(index, settings, mappings, cause, timeout).actionGet

  def createIndex_send(index: String, settings: Map[String, String] = Map(), mappings: Map[String, String] = Map(), cause: Option[String] = None, timeout: Option[String] = None) =
    createIndex_prepare(index, settings, mappings, cause, timeout).execute

  def createIndex_prepare(index: String, settings: Map[String, String] = Map(), mappings: Map[String, String] = Map(), cause: Option[String] = None, timeout: Option[String] = None) = {
    val request = client.admin.indices.prepareCreate(index)
    if (!settings.isEmpty) request.setSettings(settingsBuilder.put(settings).build())
    mappings foreach { case (kind, mapping) => request.addMapping(kind, mapping) }
    cause foreach { request.cause(_) }
    timeout foreach { request.setTimeout(_) }
    request
  }
}

trait IndexDelete {
  self: Indexer =>
  def deleteIndex(indices: Iterable[String] = Nil, timeout: Option[String] = None) = deleteIndex_send(indices, timeout).actionGet
  def deleteIndex_send(indices: Iterable[String] = Nil, timeout: Option[String] = None) = deleteIndex_prepare(indices, timeout).execute
  def deleteIndex_prepare(indices: Iterable[String] = Nil, timeout: Option[String] = None) = {
    val request = client.admin.indices.prepareDelete(indices.toArray: _*)
    timeout foreach { request.setTimeout(_) }
    request
  }

  def deleteIndexIfExists(indices: Iterable[String] = Nil, timeout: Option[String] = None) = deleteIndexIfExists_send(indices, timeout).actionGet
  def deleteIndexIfExists_send(indices: Iterable[String] = Nil, timeout: Option[String] = None) = deleteIndexIfExists_prepare(indices, timeout).execute
  def deleteIndexIfExists_prepare(indices: Iterable[String] = Nil, timeout: Option[String] = None) = deleteIndex_prepare(indices filter (exists(_)), timeout)
}

trait UpdateSettings {
  self: Indexer =>
  def updateSettings(settings: String, indices: String*) = updateSettings_send(settings, indices.toArray: _*).actionGet
  def updateSettings_send(settings: String, indices: String*) = updateSettings_prepare(settings, indices.toArray: _*).execute
  def updateSettings_prepare(settings: String, indices: String*) = {
    val request = client.admin.indices.prepareUpdateSettings(indices.toArray: _*)
    request.setSettings(settingsBuilder.loadFromSource(settings).build())
    request
  }
}

trait Exists {
  self: Indexer =>
  def exists(indices: String*) = exists_send(indices.toArray: _*).actionGet.exists
  def exists_send(indices: String*) = exists_prepare(indices.toArray: _*).execute
  def exists_prepare(indices: String*) = client.admin.indices.prepareExists(indices.toArray: _*)
}

trait Alias {
  self: Indexer =>
  def alias(indices: Iterable[String], alias: String, filter: Map[String, Object] = Map(), timeout: Option[String] = None) = alias_send(indices, alias, filter, timeout).actionGet
  def alias_send(indices: Iterable[String], alias: String, filter: Map[String, Object] = Map(), timeout: Option[String] = None) = alias_prepare(indices, alias, filter, timeout).execute
  def alias_prepare(indices: Iterable[String], alias: String, filter: Map[String, Object] = Map(), timeout: Option[String] = None) = {
    val request = client.admin.indices.prepareAliases
    indices foreach { request.addAlias(_, alias, filter) }
    timeout foreach { each => request.setTimeout(parseTimeValue(each, null)) }
    request
  }
}

trait Unalias {
  self: Indexer =>
  def unalias(indices: Iterable[String], alias: String, timeout: Option[String] = None) = unalias_send(indices, alias, timeout).actionGet
  def unalias_send(indices: Iterable[String], alias: String, timeout: Option[String] = None) = unalias_prepare(indices, alias, timeout).execute
  def unalias_prepare(indices: Iterable[String], alias: String, timeout: Option[String] = None) = {
    val request = client.admin.indices.prepareAliases
    indices foreach { request.removeAlias(_, alias) }
    timeout foreach { each => request.setTimeout(parseTimeValue(each, null)) }
    request
  }
}

trait Optimize {
  self: Indexer =>

  def optimize(
    indices: Iterable[String] = Nil,
    flush: Option[Boolean] = None,
    listenerThreaded: Option[Boolean] = None,
    maxNumSegments: Option[Int] = None,
    onlyExpungeDeletes: Option[Boolean] = None,
    operationThreading: Option[BroadcastOperationThreading] = None,
    refresh: Option[Boolean] = None, waitForMerge: Option[Boolean] = None) =
    optimize_send(indices, flush, listenerThreaded, maxNumSegments, onlyExpungeDeletes, operationThreading, refresh, waitForMerge).actionGet

  def optimize_send(
    indices: Iterable[String] = Nil,
    flush: Option[Boolean] = None,
    listenerThreaded: Option[Boolean] = None,
    maxNumSegments: Option[Int] = None,
    onlyExpungeDeletes: Option[Boolean] = None,
    operationThreading: Option[BroadcastOperationThreading] = None,
    refresh: Option[Boolean] = None, waitForMerge: Option[Boolean] = None) =
    optimize_prepare(indices, flush, listenerThreaded, maxNumSegments, onlyExpungeDeletes, operationThreading, refresh, waitForMerge).execute

  def optimize_prepare(
    indices: Iterable[String] = Nil,
    flush: Option[Boolean] = None,
    listenerThreaded: Option[Boolean] = None,
    maxNumSegments: Option[Int] = None,
    onlyExpungeDeletes: Option[Boolean] = None,
    operationThreading: Option[BroadcastOperationThreading] = None,
    refresh: Option[Boolean] = None, waitForMerge: Option[Boolean] = None) = {
		  /* method body */
    val request = client.admin.indices.prepareOptimize(indices.toArray: _*)
    flush foreach { request.setFlush(_) }
    listenerThreaded foreach { request.setListenerThreaded(_) }
    maxNumSegments foreach { request.setMaxNumSegments(_) }
    onlyExpungeDeletes foreach { request.setOnlyExpungeDeletes(_) }
    operationThreading foreach { request.setOperationThreading(_) }
    refresh foreach { request.setRefresh(_) }
    waitForMerge foreach { request.setWaitForMerge(_) }
    request
  }
}

trait Flush {
  self: Indexer =>
  def flush(indices: Iterable[String] = Nil, full: Option[Boolean] = None, refresh: Option[Boolean] = None) = flush_send(indices, full, refresh).actionGet
  def flush_send(indices: Iterable[String] = Nil, full: Option[Boolean] = None, refresh: Option[Boolean] = None) = flush_prepare(indices, full, refresh).execute
  def flush_prepare(indices: Iterable[String] = Nil, full: Option[Boolean] = None, refresh: Option[Boolean] = None) = {
    val request = client.admin.indices.prepareFlush(indices.toArray: _*)
    full foreach { request.setFull(_) }
    refresh foreach { request.setRefresh(_) }
    request
  }
}

trait Refresh {
  self: Indexer =>
  def refresh(indices: Iterable[String] = Nil, listenerThreaded: Option[Boolean] = None, operationThreading: Option[BroadcastOperationThreading] = None, waitForOperations: Option[Boolean] = None) =
    refresh_send(indices, listenerThreaded, operationThreading, waitForOperations).actionGet
  def refresh_send(indices: Iterable[String] = Nil, listenerThreaded: Option[Boolean] = None, operationThreading: Option[BroadcastOperationThreading] = None, waitForOperations: Option[Boolean] = None) =
    refresh_prepare(indices, listenerThreaded, operationThreading, waitForOperations).execute
  def refresh_prepare(indices: Iterable[String] = Nil, listenerThreaded: Option[Boolean] = None, operationThreading: Option[BroadcastOperationThreading] = None, waitForOperations: Option[Boolean] = None) = {
    val request = client.admin.indices.prepareRefresh(indices.toArray: _*)
    listenerThreaded foreach { request.setListenerThreaded(_) }
    operationThreading foreach { request.setOperationThreading(_) }
    waitForOperations foreach { request.setWaitForOperations(_) }
    request
  }
}

trait Status {
  self: Indexer =>
  def status(indices: Iterable[String] = Nil, recovery: Option[Boolean] = None, snapshot: Option[Boolean] = None) = status_send(indices, recovery, snapshot).actionGet
  def status_send(indices: Iterable[String] = Nil, recovery: Option[Boolean] = None, snapshot: Option[Boolean] = None) = status_prepare(indices, recovery, snapshot).execute
  def status_prepare(indices: Iterable[String] = Nil, recovery: Option[Boolean] = None, snapshot: Option[Boolean] = None) = {
    val request = client.admin.indices.prepareStatus(indices.toArray: _*)
    recovery foreach { request.setRecovery(_) }
    snapshot foreach { request.setSnapshot(_) }
    request
  }
}

trait Stats {
  self: Indexer =>

  def stats(
    indices: Iterable[String] = Nil,
    docs: Option[Boolean] = None,
    flush: Option[Boolean] = None,
    get: Option[Boolean] = None,
    groups: Iterable[String] = Nil,
    indexing: Option[Boolean] = None,
    merge: Option[Boolean] = None,
    refresh: Option[Boolean] = None,
    search: Option[Boolean] = None,
    store: Option[Boolean] = None,
    types: Iterable[String] = Nil) = stats_send(indices, docs, flush, get, groups, indexing, merge, refresh, search, store, types).actionGet

  def stats_send(
    indices: Iterable[String] = Nil,
    docs: Option[Boolean] = None,
    flush: Option[Boolean] = None,
    get: Option[Boolean] = None,
    groups: Iterable[String] = Nil,
    indexing: Option[Boolean] = None,
    merge: Option[Boolean] = None,
    refresh: Option[Boolean] = None,
    search: Option[Boolean] = None,
    store: Option[Boolean] = None,
    types: Iterable[String] = Nil) = stats_prepare(indices, docs, flush, get, groups, indexing, merge, refresh, search, store, types).execute

  def stats_prepare(
    indices: Iterable[String] = Nil,
    docs: Option[Boolean] = None,
    flush: Option[Boolean] = None,
    get: Option[Boolean] = None,
    groups: Iterable[String] = Nil,
    indexing: Option[Boolean] = None,
    merge: Option[Boolean] = None,
    refresh: Option[Boolean] = None,
    search: Option[Boolean] = None,
    store: Option[Boolean] = None,
    types: Iterable[String] = Nil) = {
		  /* method body */
    val request = client.admin.indices.prepareStats(indices.toArray: _*)
    docs foreach { request.setDocs(_) }
    flush foreach { request.setFlush(_) }
    get foreach { request.setGet(_) }
    request.setGroups(groups.toArray: _*)
    indexing foreach { request.setIndexing(_) }
    merge foreach { request.setMerge(_) }
    refresh foreach { request.setRefresh(_) }
    search foreach { request.setSearch(_) }
    store foreach { request.setStore(_) }
    request.setTypes(types.toArray: _*)
    request
  }
}

trait PutMapping {
  self: Indexer =>
  def putMapping(index: String, `type`: String, json: String, ignoreConflicts: Option[Boolean] = None, timeout: Option[String] = None) = 
    putMappingForAll(Seq(index), `type`, json, ignoreConflicts, timeout)
  def putMapping_send(index: String, `type`: String, json: String, ignoreConflicts: Option[Boolean] = None, timeout: Option[String] = None) = 
    putMappingForAll_send(Seq(index), `type`, json, ignoreConflicts, timeout)
  def putMapping_prepare(index: String, `type`: String, json: String, ignoreConflicts: Option[Boolean] = None, timeout: Option[String] = None) = 
    putMappingForAll_prepare(Seq(index), `type`, json, ignoreConflicts, timeout)

  def putMappingForAll(indices: Iterable[String], `type`: String, json: String, ignoreConflicts: Option[Boolean] = None, timeout: Option[String] = None) = 
    putMappingForAll_send(indices, `type`, json, ignoreConflicts, timeout).actionGet
  def putMappingForAll_send(indices: Iterable[String], `type`: String, json: String, ignoreConflicts: Option[Boolean] = None, timeout: Option[String] = None) = 
    putMappingForAll_prepare(indices, `type`, json, ignoreConflicts, timeout).execute
  def putMappingForAll_prepare(indices: Iterable[String], `type`: String, json: String, ignoreConflicts: Option[Boolean] = None, timeout: Option[String] = None) = {
    val request = client.admin.indices.preparePutMapping(indices.toArray: _*)
    request.setType(`type`)
    request.setSource(json)
    ignoreConflicts foreach { request.setIgnoreConflicts(_) }
    timeout foreach { request.setTimeout(_) }
    request
  }
}

trait DeleteMapping {
  self: Indexer =>
  def deleteMapping(indices: Iterable[String], `type`: Option[String] = None, timeout: Option[String] = None) = 
    deleteMapping_send(indices, `type`, timeout).actionGet
  def deleteMapping_send(indices: Iterable[String], `type`: Option[String] = None, timeout: Option[String] = None) = 
    deleteMapping_prepare(indices, `type`, timeout).execute
  def deleteMapping_prepare(indices: Iterable[String], `type`: Option[String] = None, timeout: Option[String] = None) = {
    val request = client.admin.indices.prepareDeleteMapping(indices.toArray: _*)
    `type` foreach { request.setType(_) }
    timeout foreach { each => request.setMasterNodeTimeout(parseTimeValue(each, null)) }
    request
  }
}

trait PutTemplate {
  self: Indexer =>

  def putTemplate(
    name: String,
    settings: Map[String, String] = Map(),
    mappings: Map[String, String] = Map(),
    cause: Option[String] = None,
    create: Option[Boolean] = None,
    order: Option[Int] = None,
    timeout: Option[String] = None) = putTemplate_send(name, settings, mappings, cause, create, order, timeout).actionGet

  def putTemplate_send(
    name: String,
    settings: Map[String, String] = Map(),
    mappings: Map[String, String] = Map(),
    cause: Option[String] = None,
    create: Option[Boolean] = None,
    order: Option[Int] = None,
    timeout: Option[String] = None) = putTemplate_prepare(name, settings, mappings, cause, create, order, timeout).execute

  def putTemplate_prepare(
    name: String,
    settings: Map[String, String] = Map(),
    mappings: Map[String, String] = Map(),
    cause: Option[String] = None,
    create: Option[Boolean] = None,
    order: Option[Int] = None,
    timeout: Option[String] = None) = {
		  /* method body */
    val request = client.admin.indices.preparePutTemplate(name)
    if (!settings.isEmpty) request.setSettings(settingsBuilder.put(settings).build())
    mappings foreach { case (kind, mapping) => request.addMapping(kind, mapping) }
    cause foreach { request.cause(_) }
    create foreach { request.setCreate(_) }
    order foreach { request.setOrder(_) }
    timeout foreach { request.setTimeout(_) }
    request
  }
}

trait DeleteTemplate {
  self: Indexer =>
  def deleteTemplate(name: String, timeout: Option[String] = None) = deleteTemplate_send(name, timeout).actionGet
  def deleteTemplate_send(name: String, timeout: Option[String] = None) = deleteTemplate_prepare(name, timeout).execute
  def deleteTemplate_prepare(name: String, timeout: Option[String] = None) = {
    val request = client.admin.indices.prepareDeleteTemplate(name)
    timeout foreach { request.setTimeout(_) }
    request
  }
}

trait Open {
  self: Indexer =>
  def openIndex(index: String, timeout: Option[String] = None) = openIndex_send(index, timeout).actionGet
  def openIndex_send(index: String, timeout: Option[String] = None) = openIndex_prepare(index, timeout).execute
  def openIndex_prepare(index: String, timeout: Option[String] = None) = {
    val request = client.admin.indices.prepareOpen(index)
    timeout foreach { request.setTimeout(_) }
    request
  }
}

trait Close {
  self: Indexer =>
  def closeIndex(index: String, timeout: Option[String] = None) = closeIndex_send(index, timeout).actionGet
  def closeIndex_send(index: String, timeout: Option[String] = None) = closeIndex_prepare(index, timeout).execute
  def closeIndex_prepare(index: String, timeout: Option[String] = None) = {
    val request = client.admin.indices.prepareClose(index)
    timeout foreach { request.setTimeout(_) }
    request
  }
}

trait GatewaySnapshot {
  self: Indexer =>
  def gatewaySnapshot(indices: String*) = gatewaySnapshot_send(indices.toArray: _*).actionGet
  def gatewaySnapshot_send(indices: String*) = gatewaySnapshot_prepare(indices.toArray: _*).execute
  def gatewaySnapshot_prepare(indices: String*) = client.admin.indices.prepareGatewaySnapshot(indices.toArray: _*)
}

trait Segments {
  self: Indexer =>
  def segments(indices: String*) = segments_send(indices.toArray: _*).actionGet
  def segments_send(indices: String*) = segments_prepare(indices.toArray: _*).execute
  def segments_prepare(indices: String*) = client.admin.indices.prepareSegments(indices.toArray: _*)
}

trait ClearCache {
  self: Indexer =>
    
  def clearCache(
    indices: Iterable[String] = Nil,
    fields: Iterable[String] = Nil,
    bloomCache: Option[Boolean] = None,
    fieldDataCache: Option[Boolean] = None,
    filterCache: Option[Boolean] = None,
    idCache: Option[Boolean] = None,
    listenerThreaded: Option[Boolean] = None,
    operationThreading: Option[BroadcastOperationThreading] = None) =
    clearCache_send(indices, fields, bloomCache, fieldDataCache, filterCache, idCache, listenerThreaded, operationThreading).actionGet
    
  def clearCache_send(
    indices: Iterable[String] = Nil,
    fields: Iterable[String] = Nil,
    bloomCache: Option[Boolean] = None,
    fieldDataCache: Option[Boolean] = None,
    filterCache: Option[Boolean] = None,
    idCache: Option[Boolean] = None,
    listenerThreaded: Option[Boolean] = None,
    operationThreading: Option[BroadcastOperationThreading] = None) =
    clearCache_prepare(indices, fields, bloomCache, fieldDataCache, filterCache, idCache, listenerThreaded, operationThreading).execute
    
  def clearCache_prepare(
    indices: Iterable[String] = Nil,
    fields: Iterable[String] = Nil,
    bloomCache: Option[Boolean] = None,
    fieldDataCache: Option[Boolean] = None,
    filterCache: Option[Boolean] = None,
    idCache: Option[Boolean] = None,
    listenerThreaded: Option[Boolean] = None,
    operationThreading: Option[BroadcastOperationThreading] = None) = {
		  /* method body */
    val request = client.admin.indices.prepareClearCache(indices.toArray: _*)
    request.setFields(fields.toArray: _*)
    bloomCache foreach { request.setBloomCache(_) }
    fieldDataCache foreach { request.setFieldDataCache(_) }
    filterCache foreach { request.setFilterCache(_) }
    idCache foreach { request.setIdCache(_) }
    listenerThreaded foreach { request.setListenerThreaded(_) }
    operationThreading foreach { request.setOperationThreading(_) }
    request
  }
}

trait ValidateQuery {
  self: Indexer =>

  def validateQuery(
    indices: Iterable[String] = Nil,
    types: Iterable[String] = Nil,
    query: QueryBuilder = matchAllQuery,
    explain: Option[Boolean] = None,
    listenerThreaded: Option[Boolean] = None,
    operationThreading: Option[BroadcastOperationThreading] = None) =
    validateQuery_send(indices, types, query, explain, listenerThreaded, operationThreading).actionGet

  def validateQuery_send(
    indices: Iterable[String] = Nil,
    types: Iterable[String] = Nil,
    query: QueryBuilder = matchAllQuery,
    explain: Option[Boolean] = None,
    listenerThreaded: Option[Boolean] = None,
    operationThreading: Option[BroadcastOperationThreading] = None) =
    validateQuery_prepare(indices, types, query, explain, listenerThreaded, operationThreading).execute

  def validateQuery_prepare(
    indices: Iterable[String] = Nil,
    types: Iterable[String] = Nil,
    query: QueryBuilder = matchAllQuery,
    explain: Option[Boolean] = None,
    listenerThreaded: Option[Boolean] = None,
    operationThreading: Option[BroadcastOperationThreading] = None) = {
		  /* method body */
    val request = client.admin.indices.prepareValidateQuery(indices.toArray: _*)
    request.setTypes(types.toArray: _*)
    request.setQuery(query)
    explain foreach { request.setExplain(_) }
    listenerThreaded foreach { request.setListenerThreaded(_) }
    operationThreading foreach { request.setOperationThreading(_) }
    request
  }
}
