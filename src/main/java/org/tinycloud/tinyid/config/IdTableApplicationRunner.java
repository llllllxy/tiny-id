package org.tinycloud.tinyid.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.tinycloud.tinyid.bean.entity.TIdTable;
import org.tinycloud.tinyid.dao.IdTableDao;
import org.tinycloud.tinyid.utils.IdTableUtils;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * <p>
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-04-2024/4/27 22:41
 */
@Component
@Order(99)
public class IdTableApplicationRunner implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(SnowflakeApplicationRunner.class);

    @Autowired
    private IdTableDao idTableDao;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<TIdTable> list = idTableDao.listAll();
        if (!CollectionUtils.isEmpty(list)) {
            for (TIdTable item : list) {
                IdTableUtils.cacheMap.put(item.getIdCode(), new ArrayBlockingQueue<String>(item.getIdStep()));
            }
        }
    }
}
