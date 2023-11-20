package org.apache.apisix.plugin.runner.db;

import org.apache.apisix.plugin.runner.db.model.*;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserDao {
    User selectByWolfUserId(int wolfUserId);
}