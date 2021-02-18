```java

		if(interceptors != null && !interceptors.isEmpty()) { // 查询出修改的所有列
			jdbcTemplate.execute("SET @uids := NULL");
			List<Field> keyFields = DOInfoReader.getKeyColumns(clazz);
			StringBuilder selectUids = new StringBuilder("(SELECT @uids := CONCAT_WS(',',");
			for(Field key : keyFields) {
				selectUids.append("`" + key.getAnnotation(Column.class).value() + "`").append(",");
			}
			selectUids.append("@uids))");
			sql = SQLUtils.getUpdateAllSQL(clazz, setSql, whereSql, selectUids.toString());
			rows = namedJdbcExecuteUpdate(sql, values.toArray());
			String ids = jdbcTemplate.queryForObject("SELECT @uids", String.class);
			if(ids != null && !ids.trim().isEmpty()) {
				String[] strs = ids.split(",");
				int size = strs.length / keyFields.size();
				List<Object> result = new ArrayList<Object>();
				List<Object> keys = new ArrayList<Object>();
				boolean isOneKey = keyFields.size() == 1;
				for(int i = 0; i < size; i++) {
					T t = null;
					try {
						t = clazz.newInstance();
					} catch (Exception e) {
						LOGGER.error("newInstance class {} fail", clazz, e);
					}
					if(t == null) {continue;}
					for(int j = 0; j < keyFields.size(); j++) {
						DOInfoReader.setValue(keyFields.get(j), t, strs[i * keyFields.size() + j]);
					}
					if(isOneKey) {
						 // 这里之所以要从对象里拿值，是为了得到对的类型，才能走到主键索引
						keys.add(DOInfoReader.getValue(keyFields.get(0), t));
					} else {
						boolean succ = getByKey(t);
						if(!succ) {
							LOGGER.error("getByKey fail for t:{}", NimbleOrmJSON.toJson(t));
						}
						result.add(t);
					}
				}
				if(isOneKey && !keys.isEmpty()) {
					result = (List<Object>) getAll(clazz, "where `" + 
				       keyFields.get(0).getAnnotation(Column.class).value() + "` in (?)", keys);
				}
				doInterceptAfterUpdate(result, rows);
			}
		}

```