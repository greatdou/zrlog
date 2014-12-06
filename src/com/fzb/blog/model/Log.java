 package com.fzb.blog.model;
 
 import com.fzb.common.util.ParseTools;
 import com.jfinal.plugin.activerecord.Db;
 import com.jfinal.plugin.activerecord.Model;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 public class Log extends Model<Log> implements Serializable
 {
   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final Log dao = new Log();
 
   public Map<String, Object> getLogByLogId(int id)
   {
     String sql = "select l.*,u.userName,(select count(commentId) from comment where logId=l.logId) commentSize ,t.alias as typeAlias,t.typeName as typeName  from log l inner join user u,type t where t.typeId=l.typeId and u.userId=l.userId and l.logId=?";
     return ((Log)findFirst(sql, new Object[] { Integer.valueOf(id) })).getAttrs();
   }
 
   public Log getLastLog(int id)
   {
     String lastLogSql = "select l.alias as alias,l.title as title from log l where l.logId<? order by logId desc";
     Log log = (Log)findFirst(lastLogSql, new Object[] { Integer.valueOf(id) });
     if (log == null) {
       log = (Log)((Log)new Log().set("alias", Integer.valueOf(id))).set("title", "没有下一篇了");
     }
     return log;
   }
 
   public Log getNextLog(int id)
   {
     String nextLogSql = "select l.alias as alias,l.title as title from log l where l.logId>?";
     Log log = (Log)findFirst(nextLogSql, new Object[] { Integer.valueOf(id) });
     if (log == null) {
       log = (Log)((Log)new Log().set("alias", Integer.valueOf(id))).set("title", "没有下一篇了");
     }
     return log;
   }
 
   public int getLogByLogIdAlias(String alias) {
     String sql = "select l.logId from log l  where l.alias=?";
     return ((Log)findFirst(sql, new Object[] { alias })).getInt("logId").intValue();
   }
 
   public int getMaxRecord() {
     return ((Log)findFirst("select max(logId) max from log")).getInt("max").intValue();
   }
 
   public Map<String, Object> getLogsByPage(int page, int pageSize)
   {
     Map<String, Object> data = new HashMap<String, Object>();
     String sql = "select l.*,t.typeName,t.alias as typeAlias,u.userName,(select count(commentId) from comment where logId=l.logId) commentSize from log l inner join user u inner join type t where u.userId=l.userId and t.typeid=l.typeid order by l.logId desc limit ?,?";
 
     data.put("rows", find(sql, new Object[] { Integer.valueOf(ParseTools.getFirstRecord(page, pageSize)), Integer.valueOf(pageSize) }));
     fillData(page, pageSize, "from log l inner join user u where u.userId=l.userId", data, new Object[0]);
     return data;
   }
   
   public Map<String, Object> getList(int page, int pageSize)
   {
     Map<String, Object> data = new HashMap<String, Object>();
     String sql = "select l.*,t.typeName,l.logId as id,t.alias as typeAlias,u.userName,(select count(commentId) from comment where logId=l.logId) commentSize from log l inner join user u inner join type t where u.userId=l.userId and t.typeid=l.typeid order by l.logId desc limit ?,?";
 
     data.put("rows", find(sql, new Object[] { Integer.valueOf(ParseTools.getFirstRecord(page, pageSize)), Integer.valueOf(pageSize) }));
     fillData(page, pageSize, "from log l inner join user u where u.userId=l.userId", data, new Object[0]);
     return data;
   }
 
   public Map<String, Object> getLogsBySort(int page, int pageSize, String typeAlias)
   {
     Map<String, Object> data = new HashMap<String, Object>();
     String sql = "select l.*,t.typeName,t.alias  as typeAlias,(select count(commentId) from comment where logId=l.logId) commentSize,u.userName from log l inner join user u,type t where u.userId=l.userId and t.typeId=l.typeId and t.alias=? order by l.logId desc limit ?,?";
     data.put("rows", find(sql, new Object[] { typeAlias, Integer.valueOf(ParseTools.getFirstRecord(page, pageSize)), Integer.valueOf(pageSize) }));
 
     fillData(page, pageSize, "from log l inner join user u,type t where u.userId=l.userId and t.typeId=l.typeId and t.alias=?", data, new Object[] { typeAlias });
     return data;
   }
 
   private void fillData(int page, int pageSize, String where, Map<String, Object> data, Object[] obj)
   {
     if (((List<Log>)data.get("rows")).size() > 0) {
       data.put("page", Integer.valueOf(page));
       long count = ((Log)findFirst("select count(l.logId) cnt " + where, obj)).getLong("cnt").longValue();
       data.put("total", Integer.valueOf(ParseTools.getTotalPate(count, pageSize)));
       data.put("records", Long.valueOf(count));
     }
     else {
       data.clear();
     }
   }
 
   public Map<String, Object> getArchives()
   {
     List<Object[]> lo = Db.query("select  DATE_FORMAT(releaseTime,'%Y_%m'),count(DATE_FORMAT(releaseTime,'%Y_%m')) from log group by DATE_FORMAT(releaseTime,'%Y_%m') order by logId desc");
     Map<String, Object> archives = new LinkedHashMap<String, Object>();
     for (Object[] objects : lo) {
       archives.put(objects[0].toString(), objects[1]);
     }
     return archives;
   }
   public Map<String, Object> getLogsByTag(int page, int pageSize, String tag) {
     Map<String, Object> data = new HashMap<String, Object>();
     String sql = "select l.*,t.typeName,t.alias  as typeAlias,(select count(commentId) from comment where logId=l.logId) commentSize,u.userName from log l inner join user u,type t where u.userId=l.userId and t.typeId=l.typeId and l.keywords like ? order by l.logId desc limit ?,?";
     data.put("rows", find(sql, new Object[] { "%" + tag + "%", Integer.valueOf(ParseTools.getFirstRecord(page, pageSize)), Integer.valueOf(pageSize) }));
     fillData(page, pageSize, "from log l inner join user u,type t where u.userId=l.userId and t.typeId=l.typeId and  l.keywords like ?", data, new Object[] { tag + "%" });
     return data;
   }
   public Map<String, Object> getLogsByData(int page, int pageSize, String date) {
     Map<String, Object> data = new HashMap<String, Object>();
     String sql = "select l.*,t.typeName,t.alias as typeAlias,(select count(commentId) from comment where logId=l.logId) commentSize,u.userName from log l inner join user u,type t where u.userId=l.userId and t.typeId=l.typeId and DATE_FORMAT(releaseTime,'%Y_%m')=? order by l.logId desc limit ?,?";
     data.put("rows", find(sql, new Object[] { date, Integer.valueOf(ParseTools.getFirstRecord(page, pageSize)), Integer.valueOf(pageSize) }));
     fillData(page, pageSize, "from log l inner join user u,type t where u.userId=l.userId and t.typeId=l.typeId and  DATE_FORMAT(releaseTime,'%Y_%m')=?", data, new Object[] { date });
     return data;
   }
   public Map<String, Object> getLogsByTitleOrContent(int page, int pageSize, String key) {
     Map<String, Object> data = new HashMap<String, Object>();
     String sql = "select l.*,t.typeName,t.alias as typeAlias,(select count(commentId) from comment where logId=l.logId) commentSize,u.userName from log l inner join user u,type t where u.userId=l.userId and t.typeId=l.typeId and (l.title like ? or l.content like ?) order by l.logId desc limit ?,?";
     data.put("rows", find(sql, new Object[] { "%" + key + "%", "%" + key + "%", Integer.valueOf(ParseTools.getFirstRecord(page, pageSize)), Integer.valueOf(pageSize) }));
     fillData(page, pageSize, "from log l inner join user u,type t where u.userId=l.userId and t.typeId=l.typeId and (l.title like ? or l.content like ?)", data, new Object[] { "%" + key + "%", "%" + key + "%" });
     return data;
   }
 
   public List<Object[]> getAllAlias() {
     return Db.query("select alias,releaseTime from log");
   }
 }
