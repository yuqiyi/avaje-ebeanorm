package io.ebeaninternal.server.deploy.parse;

import io.ebean.annotation.Cache;
import io.ebean.annotation.DbComment;
import io.ebean.annotation.DbPartition;
import io.ebean.annotation.DocStore;
import io.ebean.annotation.Draftable;
import io.ebean.annotation.DraftableElement;
import io.ebean.annotation.History;
import io.ebean.annotation.Index;
import io.ebean.annotation.InvalidateQueryCache;
import io.ebean.annotation.ReadAudit;
import io.ebean.annotation.StorageEngine;
import io.ebean.annotation.UpdateMode;
import io.ebean.annotation.View;
import io.ebean.config.TableName;
import io.ebeaninternal.server.deploy.BeanDescriptor.EntityType;
import io.ebeaninternal.server.deploy.IndexDefinition;
import io.ebeaninternal.server.deploy.InheritInfo;
import io.ebeaninternal.server.deploy.PartitionMeta;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import static io.ebean.util.AnnotationUtil.findAnnotationRecursive;
import static io.ebean.util.AnnotationUtil.findAnnotationsRecursive;

/**
 * Read the class level deployment annotations.
 */
public class AnnotationClass extends AnnotationParser {

  private static final Logger logger = LoggerFactory.getLogger(AnnotationClass.class);

  private final String asOfViewSuffix;

  private final String versionsBetweenSuffix;

  private final boolean disableL2Cache;

  /**
   * Create to parse AttributeOverride annotations which is run last
   * after all the properties/fields have been parsed fully.
   */
  AnnotationClass(DeployBeanInfo<?> info, ReadAnnotationConfig readConfig) {
    super(info, readConfig);
    this.asOfViewSuffix = readConfig.getAsOfViewSuffix();
    this.versionsBetweenSuffix = readConfig.getVersionsBetweenSuffix();
    this.disableL2Cache = readConfig.isDisableL2Cache();
  }

  /**
   * Parse any AttributeOverride set on the class.
   */
  void parseAttributeOverride() {

    Class<?> cls = descriptor.getBeanType();
    AttributeOverride override = findAnnotationRecursive(cls, AttributeOverride.class);
    if (override != null) {
      String propertyName = override.name();
      Column column = override.column();
      DeployBeanProperty beanProperty = descriptor.getBeanProperty(propertyName);
      if (beanProperty == null) {
        logger.error("AttributeOverride property [" + propertyName + "] not found on " + descriptor.getFullName());
      } else {
        readColumn(column, beanProperty);
      }
    }
  }

  /**
   * Read the class level deployment annotations.
   */
  @Override
  public void parse() {
    read(descriptor.getBeanType());
    setTableName();
  }

  /**
   * Set the table name if it has not already been set.
   */
  private void setTableName() {

    if (descriptor.isBaseTableType()) {
      Class<?> beanType = descriptor.getBeanType();
      InheritInfo inheritInfo = descriptor.getInheritInfo();
      if (inheritInfo != null) {
        beanType = inheritInfo.getRoot().getType();
      }
      // default the TableName using NamingConvention.
      TableName tableName = namingConvention.getTableName(beanType);
      descriptor.setBaseTable(tableName, asOfViewSuffix, versionsBetweenSuffix);
    }
  }

  private void read(Class<?> cls) {

    // maybe doc store only so check for this before @Entity
    DocStore docStore = findAnnotationRecursive(cls, DocStore.class);
    if (docStore != null) {
      descriptor.readDocStore(docStore);
      descriptor.setEntityType(EntityType.DOC);
      descriptor.setName(cls.getSimpleName());
    }

    Entity entity = findAnnotationRecursive(cls, Entity.class);
    if (entity != null) {
      descriptor.setEntityType(EntityType.ORM);
      if (entity.name().isEmpty()) {
        descriptor.setName(cls.getSimpleName());
      } else {
        descriptor.setName(entity.name());
      }
    }

    IdClass idClass = findAnnotationRecursive(cls, IdClass.class);
    if (idClass != null) {
      descriptor.setIdClass(idClass.value());
    }

    Embeddable embeddable = findAnnotationRecursive(cls, Embeddable.class);
    if (embeddable != null) {
      descriptor.setEntityType(EntityType.EMBEDDED);
      descriptor.setName("Embeddable:" + cls.getSimpleName());
    }

    for (Index index : findAnnotationsRecursive(cls, Index.class)) {
      descriptor.addIndex(new IndexDefinition(index.columnNames(), index.name(), index.unique()));
    }

    UniqueConstraint uc = findAnnotationRecursive(cls, UniqueConstraint.class);
    if (uc != null) {
      descriptor.addIndex(new IndexDefinition(uc.columnNames()));
    }

    View view = findAnnotationRecursive(cls, View.class);
    if (view != null) {
      descriptor.setView(view.name(), view.dependentTables());
    }
    Table table = findAnnotationRecursive(cls, Table.class);
    if (table != null) {
      UniqueConstraint[] uniqueConstraints = table.uniqueConstraints();
      for (UniqueConstraint c : uniqueConstraints) {
        descriptor.addIndex(new IndexDefinition(c.columnNames()));
      }
    }

    StorageEngine storage = findAnnotationRecursive(cls, StorageEngine.class);
    if (storage != null) {
      descriptor.setStorageEngine(storage.value());
    }

    DbPartition partition = findAnnotationRecursive(cls, DbPartition.class);
    if (partition != null) {
      descriptor.setPartitionMeta(new PartitionMeta(partition.mode(), partition.property()));
    }

    Draftable draftable = findAnnotationRecursive(cls, Draftable.class);
    if (draftable != null) {
      descriptor.setDraftable();
    }

    DraftableElement draftableElement = findAnnotationRecursive(cls, DraftableElement.class);
    if (draftableElement != null) {
      descriptor.setDraftableElement();
    }

    ReadAudit readAudit = findAnnotationRecursive(cls, ReadAudit.class);
    if (readAudit != null) {
      descriptor.setReadAuditing();
    }

    History history = findAnnotationRecursive(cls, History.class);
    if (history != null) {
      descriptor.setHistorySupport();
    }

    DbComment comment = findAnnotationRecursive(cls, DbComment.class);
    if (comment != null) {
      descriptor.setDbComment(comment.value());
    }

    UpdateMode updateMode = findAnnotationRecursive(cls, UpdateMode.class);
    if (updateMode != null) {
      descriptor.setUpdateChangesOnly(updateMode.updateChangesOnly());
    }

    if (!disableL2Cache) {
      Cache cache = findAnnotationRecursive(cls, Cache.class);
      if (cache != null) {
        descriptor.setCache(cache);
      } else {
        InvalidateQueryCache invalidateQueryCache = findAnnotationRecursive(cls, InvalidateQueryCache.class);
        if (invalidateQueryCache != null) {
          descriptor.setInvalidateQueryCache(invalidateQueryCache.region());
        }
      }
    }

    for (NamedQuery namedQuery : findAnnotationsRecursive(cls, NamedQuery.class)) {
      descriptor.addNamedQuery(namedQuery.name(), namedQuery.query());
    }
  }

}
