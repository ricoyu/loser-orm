# 核心接口

## 1.com.loserico.orm.dao.EntityOperations

JPA的entity对象的一些简单操作API

* `public <T> void persist(T entity);`
* `public <T> void persist(List<T> entities);`
* `public <T> T save(T entity);`
* `public <T> List<T> save(List<T> entities);`
* ......

## 2.com.loserico.orm.dao.CriteriaOperations

当SQL逻辑不那么复杂, 堆EntityOperations来说又有点复杂;)的情况用CriteriaOperations

* 根据属性查找

  ```java
  public <T> List<T> findByProperty(Class<T> entityClass, String propertyName, Object value, boolean includeDeleted);
  ```

* 支持排序的版本

  ```java
  public <T> List<T> findByProperty(Class<T> entityClass, String propertyName, Object value, OrderBean... orders)
  ```

* 分页

  ```java
  public <T> List<T> findByProperty(Class<T> entityClass, Predicate predicate, boolean includeDeleted, Page page);
  ```

* 根据属性查找，返回一个对象，如果找到多个，取第一个,可以指定是否包含软删除的记录,找不到则返回null

  ```java
  public <T> T findUniqueByProperty(Class<T> entityClass, String propertyName, Object value, OrderBean... orders);
  ```

* 根据日期区间查找

  ```java
  public <T> List<T> findBetween(Class<T> entityClass, String propertyName, LocalDateTime begin, LocalDateTime end);
  ```

* 根据属性在给定值列表中来获取，可以指定是否包含软删除的对象(IN 操作)

  ```java
  public <T> List<T> findIn(Class<T> entityClass, String propertyName, Collection<?> value);
  ```

* 找到某个属性是null的对象

  ```java
  public <T> List<T> findIsNull(Class<T> entityClass, String propertyName);
  ```

* 检查有对应entity是否存在

  ```java
  public <T> boolean ifExists(Class<T> entityClass, String propertyName, Object value);
  ```

* 根据属性查找，返回唯一一个对象，如果找到多个，取第一个，如果找不到则抛出 EntityNotFoundException

  ```java
  public <T> T ensureEntityExists(Class<T> entityClass, String propertyName, Object value) throws EntityNotFoundException;
  ```

* 根据属性删除对象

  ```java
  public <T> int deleteByProperty(Class<T> entityClass, String propertyName, Object propertyValue);
  ```

* 骚操作太多, 不一一列举
  ......

## 3.com.loserico.orm.dao.JPQLOperations

你要愿意写JPQL, 那么这个类就是为你准备的

* 命名JPQL/HQL查询, 不带参数

  ```
  public <T> List<T> namedQuery(String queryName, Class<T> clazz);
  ```

* ......

## 4.com.loserico.orm.dao.SQLOperations

重点在这里

* 支持分页的命名SQL查询，同时会自动调用queryName_count来获取总记录数 支持Velocity风格的SQL模版

  ```java
  public <T> List<T> namedSqlQuery(String queryName, Map<String, Object> params, Class<T> clazz, Page page);
  ```

* 跟namedSqlQuery的差别就是结果集不封装到Bean里面

  ```java
  public List<?> namedRawSqlQuery(String queryName);
  ```

* 返回单个值的查询 比如type是BigDecimal.class，那么这个查询返回的是BigDecimal

  ```java
  public <T> T namedScalarQuery(String queryName, Map<String, Object> params, Class<T> type);
  ```

* 根据给定的查询条件判断是否有符合条件的记录存在

  ```java
  public boolean ifExists(String queryName, Map<String, Object> params);
  ```

* 执行更新

  ```java
  public int executeUpdate(String queryName, Map<String, Object> params);
  ```

* 骚操作太多, 不一一列举

  ......

# 配置

只要配一个Bean就哦了

```java
@Bean
public JpaDao jpaDao() {
	JpaDao jpaDao = new JpaDao();
	jpaDao.getEnumLookupProperties().add("code");
	jpaDao.setUseDefaultOrder(true);
    jpaDao.getContextClasses().put("UserContext", "cn.mulberrylearning.scs.utils.UserContextHolder");
	return jpaDao;
}
```

**解释一下:**

* 通过`@PersistenceContext`自动将系统中配置好的EntityManager注入到了JpaDao里面

  ```java
  @PersistenceContext
  protected EntityManager entityManager;
  ```

* 对enum类型的支持

  假设有这样一个Entity

  ```java
  @Entity
  @Table(name = "SALE_ORDER", schema = "SCS")
  @Access(AccessType.FIELD)
  public class SaleOrder extends BaseEntity {
  		......
  
      @Column(name = "CUSTOMER_ID")
      private Long customerId;//客户ID
  
      @Column(name = "GENERATION_STRATEGY", nullable = false)
      private GenerationStrategyStatus generationStrategyStatus;
      ......
  }
  ```

  ```java
  public enum GenerationStrategyStatus {
      SINGLE_NEW(0, "独立新建"),
      RELATION_NEW(1, "关联新建");
  
      private int code;
      private String desc;
  
      private GenerationStrategyStatus(int code, String desc) {
          this.code = code;
          this.desc = desc;
      }
  	......
  }
  ```

  如果generationStrategyStatus在数据库里面对应表字段存的是code, 那么`jpaDao.getEnumLookupProperties().add("code");`的作用就是在int型的code和GenerationStrategyStatus之间互转. 默认支持按照enum的name, ordinal转换, 做了上述配置的话优先按code转换

* 默认按照`CREATE_TIME`字段排序

  `jpaDao.setUseDefaultOrder(true);`是一个开关属性, 表里没有`CREATE_TIME`字段或者不同的字段名就把这个开关关掉好了

# Demo

在Service层根据需要注入JpaDao的实例到这几个核心接口类型上

```java
@Autowired
private EntityOperations entityOperations;

@Autowired
private CriteriaOperations criteriaOperations;

@Autowired
private SQLOperations sqlOperations;
```

业务方法里面执行查询

```java
public List<PurchaseOrderListsVO> searchPurchaseOrder(PurchaseOrderSearchVO purchaseOrderSearchVO) {
    Map<String, Object> params = new HashMap<>();
    params.put("beginDate", purchaseOrderSearchVO.getBeginDate());
    params.put("endDate", purchaseOrderSearchVO.getEndDate());
    params.put("supplierId", purchaseOrderSearchVO.getSupplierId());
    params.put("status", QueryUtils.innerMatch(purchaseOrderSearchVO.getStatus()));
    params.put("auditStatus", purchaseOrderSearchVO.getAuditStatus());
    params.put("skus", QueryUtils.innerMatch(purchaseOrderSearchVO.getSkus()));
    params.put("purchaseContractNo", QueryUtils.innerMatch(purchaseOrderSearchVO.getPurchaseContractNo()));
    List<PurchaseOrderListsVO> purchaseOrderListsVOS = sqlOperations.namedSqlQuery("searchPurchaseOrder", params, PurchaseOrderListsVO.class, purchaseOrderSearchVO.getPage());
	......
}
```

PurchaseOrderListsVO是普通的POJO或者Entity都可以, 不强求;) 查询结果的字段名(如PURCHASE_CONTRACT_NO)自动转成POJO的属性名purchaseContractNo

有数据类型不一致的也自动转换

```java
@ApiModel("采购订单查询列表结果模型")
@Data
public class PurchaseOrderListsVO {

    private Long id;

    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("采购单合同号")
    private String purchaseContractNo;//采购单合同号

    @ApiModelProperty("供应商ID")
    private Long supplierId;//供应商ID
	......
}
```

其中`searchPurchaseOrder`是定义在XML中的SQL的名字, 看下面

## src/main/resources/named-sql

这个目录下放SQL语句, 如PurchaseOrder.hbm.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-mapping PUBLIC "-//Hibernate/Hibernate Mapping DTD 3.0//EN"
        "http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd" >
<hibernate-mapping>
    <sql-query name="searchPurchaseOrder">
        <![CDATA[
            SELECT
			-- SQL分页支持, 会生成一条SQL查总记录数
             #count()
                so.*
             #end
             from (
                   SELECT
                      po.ID,
                      po.ORDER_NO,
                      ......
                      po.status
                    FROM purchase_order po
                    JOIN USER u
                        ON po.CREATOR = u.USERNAME AND u.DELETED = 0
                      JOIN USER_ORGANIZATION uo
                        ON u.ID = uo.USER_ID
                        and uo.DELETED=0
						-- 这个表示Java代码里面传了blocParentId参数则生成的SQL里面会包含AND uo.BLOC_PARENT_ID= xxx这一段
                           #if($blocParentId)
                           AND uo.BLOC_PARENT_ID= :blocParentId
                           #end
                           #if($companyGroupIds)
                            AND uo.COMPANY_GROUP_ID IN (:companyGroupIds)
                           #end
                    .......................
                #if($supplierId)
                AND po.SUPPLIER_ID = :supplierId
                 #end
                #if($skus)
                AND s.SKU_CODE LIKE :skus
                 #end
                #if($status)
                AND po.STATUS LIKE :status
                 #end
                   #if($auditStatus)
                    AND po.LAST_AUDIT_STATUS = :auditStatus
                     #end
                 GROUP BY po.ID
                 ORDER BY po.PURCHASE_DATE desc,po.MODIFY_TIME desc) so
		]]>
    </sql-query>

    <sql-query name="getReceiveticketInfo">
        <![CDATA[
            SELECT
              rt.PURCHASE_ORDER_ID    id,
              GROUP_CONCAT(rt.RECEIVE_TICKET_DATE)    receiveTicketDates,
               GROUP_CONCAT(rt.RECEIVE_TICKET_NO) receiveTicketNo
            FROM receive_ticket rt
            WHERE deleted = FALSE
                AND rt.PURCHASE_ORDER_ID IN(:purchaseOrderId)
            GROUP BY rt.PURCHASE_ORDER_ID
		]]>
    </sql-query>

</hibernate-mapping>
```

