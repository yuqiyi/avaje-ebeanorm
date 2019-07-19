package io.ebeaninternal.server.deploy.parse;

import io.ebean.Model;
import io.ebean.annotation.DbArray;
import io.ebean.annotation.DbHstore;
import io.ebean.annotation.DbJson;
import io.ebean.annotation.DbJsonB;
import io.ebean.annotation.UnmappedJson;
import io.ebean.util.AnnotationUtil;
import io.ebeaninternal.server.deploy.DetermineManyType;
import io.ebeaninternal.server.deploy.ManyType;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertySimpleCollection;
import io.ebeaninternal.server.type.ScalarType;
import io.ebeaninternal.server.type.TypeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.ManyToOne;
import javax.persistence.PersistenceException;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * Create the properties for a bean.
 * <p>
 * This also needs to determine if the property is a associated many, associated
 * one or normal scalar property.
 * </p>
 */
public class DeployCreateProperties {

  private static final Logger logger = LoggerFactory.getLogger(DeployCreateProperties.class);

  private final DetermineManyType determineManyType;

  private final TypeManager typeManager;

  public DeployCreateProperties(TypeManager typeManager) {
    this.typeManager = typeManager;
    this.determineManyType = new DetermineManyType();
  }

  /**
   * Create the appropriate properties for a bean.
   */
  public void createProperties(DeployBeanDescriptor<?> desc) {

    createProperties(desc, desc.getBeanType(), 0);
    desc.sortProperties();
  }

  /**
   * Return true if we should ignore this field.
   * <p>
   * We want to ignore ebean internal fields and some others as well.
   * </p>
   */
  private boolean ignoreFieldByName(String fieldName) {
    if (fieldName.startsWith("_ebean_")) {
      // ignore Ebean internal fields
      return true;
    }
    // ignore AspectJ internal fields
    return fieldName.startsWith("ajc$instance$");
  }

  private boolean ignoreField(Field field) {
    return Modifier.isStatic(field.getModifiers())
      || Modifier.isTransient(field.getModifiers())
      || ignoreFieldByName(field.getName());
  }

  /**
   * properties the bean properties from Class. Some of these properties may not map to database
   * columns.
   */
  private void createProperties(DeployBeanDescriptor<?> desc, Class<?> beanType, int level) {

    if (beanType.equals(Model.class)) {
      // ignore all fields on model (_$dbName)
      return;
    }
    boolean scalaObject = desc.isScalaObject();

    try {
      Method[] declaredMethods = beanType.getDeclaredMethods();
      Field[] fields = beanType.getDeclaredFields();

      for (int i = 0; i < fields.length; i++) {

        Field field = fields[i];
        if (!ignoreField(field)) {
          String fieldName = getFieldName(field, beanType);
          String initFieldName = initCap(fieldName);

          Method getter = findGetter(field, initFieldName, declaredMethods, scalaObject);

          DeployBeanProperty prop = createProp(desc, field, beanType, getter);
          if (prop != null) {
            // set a order that gives priority to inherited properties
            // push Id/EmbeddedId up and CreatedTimestamp/UpdatedTimestamp down
            int sortOverride = prop.getSortOverride();
            prop.setSortOrder((level * 10000 + 100 - i + sortOverride));

            DeployBeanProperty replaced = desc.addBeanProperty(prop);
            if (replaced != null && !replaced.isTransient()) {
              String msg = "Huh??? property " + prop.getFullBeanName() + " being defined twice";
              msg += " but replaced property was not transient? This is not expected?";
              logger.warn(msg);
            }
          }
        }
      }

      Class<?> superClass = beanType.getSuperclass();

      if (!superClass.equals(Object.class)) {
        // recursively add any properties in the inheritance hierarchy
        // up to the Object.class level...
        createProperties(desc, superClass, level + 1);
      }

    } catch (PersistenceException ex) {
      throw ex;

    } catch (Exception ex) {
      throw new PersistenceException(ex);
    }
  }

  /**
   * Make the first letter of the string upper case.
   */
  private String initCap(String str) {
    if (str.length() > 1) {
      return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    } else {
      // only a single char
      return str.toUpperCase();
    }
  }

  /**
   * Return the bean spec field name (trim of "is" from boolean types)
   */
  private String getFieldName(Field field, Class<?> beanType) {

    String name = field.getName();

    if ((Boolean.class.equals(field.getType()) || boolean.class.equals(field.getType()))
      && name.startsWith("is") && name.length() > 2) {

      // it is a boolean type field starting with "is"
      char c = name.charAt(2);
      if (Character.isUpperCase(c)) {
        String msg = "trimming off 'is' from boolean field name " + name + " in class " + beanType.getName();
        logger.info(msg);

        return name.substring(2);
      }
    }
    return name;
  }

  /**
   * Find a public non-static getter method that matches this field (according to bean-spec rules).
   */
  private Method findGetter(Field field, String initFieldName, Method[] declaredMethods, boolean scalaObject) {

    String methGetName = "get" + initFieldName;
    String methIsName = "is" + initFieldName;
    String scalaGet = field.getName();

    for (Method m : declaredMethods) {
      if ((scalaObject && m.getName().equals(scalaGet)) || m.getName().equals(methGetName)
        || m.getName().equals(methIsName)) {

        Class<?>[] params = m.getParameterTypes();
        if (params.length == 0) {
          if (field.getType().equals(m.getReturnType())) {
            int modifiers = m.getModifiers();
            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)) {
              // we find it...
              return m;
            }
          }
        }
      }
    }
    return null;
  }

  @SuppressWarnings({"unchecked"})
  private DeployBeanProperty createManyType(DeployBeanDescriptor<?> desc, Class<?> targetType, ManyType manyType) {

    try {
      ScalarType<?> scalarType = typeManager.getScalarType(targetType);
      if (scalarType != null) {
        return new DeployBeanPropertySimpleCollection(desc, targetType, manyType);
      }
    } catch (NullPointerException e) {
      logger.debug("expected non-scalar type {}", e.getMessage());
    }
    return new DeployBeanPropertyAssocMany(desc, targetType, manyType);
  }

  @SuppressWarnings({"unchecked"})
  private DeployBeanProperty createProp(DeployBeanDescriptor<?> desc, Field field) {

    Class<?> propertyType = field.getType();

    ManyToOne manyToOne = AnnotationUtil.findAnnotation(field, ManyToOne.class);
    if (manyToOne != null) {
      Class<?> tt = manyToOne.targetEntity();
      if (!tt.equals(void.class)) {
        propertyType = tt;
      }
    }
    if (isSpecialScalarType(field)) {
      return new DeployBeanProperty(desc, propertyType, field.getGenericType());
    }

    // check for Collection type (list, set or map)
    ManyType manyType = determineManyType.getManyType(propertyType);
    if (manyType != null) {
      // List, Set or Map based object
      Class<?> targetType = determineTargetType(field);
      if (targetType == null) {
        Transient transAnnotation = AnnotationUtil.findAnnotation(field, Transient.class);
        if (transAnnotation != null) {
          // not supporting this field (generic type used)
          return null;
        }
        logger.warn("Could not find parameter type (via reflection) on " + desc.getFullName() + " " + field.getName());
      }
      return createManyType(desc, targetType, manyType);
    }

    if (propertyType.isEnum() || propertyType.isPrimitive()) {
      return new DeployBeanProperty(desc, propertyType, null, null);
    }

    ScalarType<?> scalarType = typeManager.getScalarType(propertyType);
    if (scalarType != null) {
      return new DeployBeanProperty(desc, propertyType, scalarType, null);
    }

    if (isTransientField(field)) {
      // return with no ScalarType (still support JSON features)
      return new DeployBeanProperty(desc, propertyType, null, null);
    }
    try {
      return new DeployBeanPropertyAssocOne(desc, propertyType);

    } catch (Exception e) {
      logger.error("Error with " + desc + " field:" + field.getName(), e);
      return null;
    }
  }

  /**
   * Return true if the field has one of the special mappings.
   */
  private boolean isSpecialScalarType(Field field) {
    return (AnnotationUtil.findAnnotation(field, DbJson.class) != null)
      || (AnnotationUtil.findAnnotation(field, DbJsonB.class) != null)
      || (AnnotationUtil.findAnnotation(field, DbArray.class) != null)
      || (AnnotationUtil.findAnnotation(field, DbHstore.class) != null)
      || (AnnotationUtil.findAnnotation(field, UnmappedJson.class) != null);
  }

  private boolean isTransientField(Field field) {

    Transient t = AnnotationUtil.findAnnotation(field, Transient.class);
    return (t != null);
  }

  private DeployBeanProperty createProp(DeployBeanDescriptor<?> desc, Field field, Class<?> beanType, Method getter) {

    DeployBeanProperty prop = createProp(desc, field);
    if (prop == null) {
      // transient annotation on unsupported type
      return null;
    } else {
      prop.setOwningType(beanType);
      prop.setName(field.getName());

      // interested in the getter for reading annotations
      prop.setReadMethod(getter);
      prop.setField(field);
      return prop;
    }
  }

  /**
   * Determine the type of the List,Set or Map. Not been set explicitly so determine this from
   * ParameterizedType.
   */
  private Class<?> determineTargetType(Field field) {

    Type genType = field.getGenericType();
    if (genType instanceof ParameterizedType) {
      ParameterizedType ptype = (ParameterizedType) genType;

      Type[] typeArgs = ptype.getActualTypeArguments();
      if (typeArgs.length == 1) {
        // probably a Set or List
        if (typeArgs[0] instanceof Class<?>) {
          return (Class<?>) typeArgs[0];
        }
        // throw new RuntimeException("Unexpected Parameterised Type? "+typeArgs[0]);
        return null;
      }
      if (typeArgs.length == 2) {
        // this is probably a Map
        if (typeArgs[1] instanceof ParameterizedType) {
          // not supporting ParameterizedType on Map.
          return null;
        }
        if (typeArgs[1] instanceof WildcardType) {
          return Object.class;
        }
        return (Class<?>) typeArgs[1];
      }
    }
    // if targetType is null, then must be set in annotations
    return null;
  }
}
