package org.tinycloud.tinyid.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.tinycloud.tinyid.bean.dto.IdTableAddDto;
import org.tinycloud.tinyid.bean.dto.IdTableEditDto;
import org.tinycloud.tinyid.bean.dto.IdTableQueryDto;
import org.tinycloud.tinyid.bean.entity.TIdTable;
import org.tinycloud.tinyid.utils.AuthUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-04-2024/4/27 20:20
 */
@Slf4j
@Repository
public class IdTableDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 刷新流水号步值
     *
     * @param idCode 编码
     * @return TIdTable
     */
    @Transactional(rollbackFor = Exception.class)
    public TIdTable refreshByIdCode(String idCode) {
        // 对同一行执行 UPDATE 时，数据库会加行级锁，保证并发更新的原子性
        String updateSql = "UPDATE t_idtable SET id_value = id_value + id_step WHERE id_code = ?";
        int rows = this.jdbcTemplate.update(updateSql, idCode);
        if (rows != 1) {
            throw new IllegalStateException("刷新流水号失败, idCode=" + idCode);
        }

        String selectSql = "SELECT * FROM t_idtable WHERE id_code = ?";
        return this.jdbcTemplate.queryForObject(selectSql, BeanPropertyRowMapper.newInstance(TIdTable.class), idCode);
    }

    /**
     * 获取所有流水号信息
     *
     * @return TIdTable列表
     */
    public List<TIdTable> listAll() {
        String sql = "SELECT * FROM t_idtable";
        List<TIdTable> list = this.jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(TIdTable.class));
        return list;
    }

    /**
     * 获取单条流水号信息
     *
     * @param idCode 编码
     * @return TIdTable
     */
    public TIdTable get(String idCode) {
        String sql = "SELECT * FROM t_idtable WHERE id_code = ?";
        List<TIdTable> list = this.jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(TIdTable.class), idCode);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        } else {
            return list.get(0);
        }
    }

    /**
     * 获取总数
     *
     * @return 流水号总数
     */
    public Long totalCount() {
        String sql = "SELECT count(*) FROM t_idtable";
        Long totalSize = jdbcTemplate.queryForObject(sql, Long.class);
        return totalSize;
    }

    /**
     * 获取id_value最大的前流水号信息
     *
     * @return TIdTable列表
     */
    public List<TIdTable> listTop25() {
        String sql = "SELECT * FROM t_idtable ORDER BY id_value DESC LIMIT 25";
        List<TIdTable> list = this.jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(TIdTable.class));
        return list;
    }


    /**
     * 分页查询list
     *
     * @param dto IdTableQueryDto
     * @return 流水号列表
     */
    public List<TIdTable> pageList(IdTableQueryDto dto) {
        List<Object> perimeters = new ArrayList<>();
        String sql = "SELECT * FROM t_idtable WHERE 1=1 ";
        if (StringUtils.hasText(dto.getIdCode())) {
            sql = sql + "AND id_code LIKE CONCAT('%', ? ,'%')";
            perimeters.add(dto.getIdCode());
        }
        if (StringUtils.hasText(dto.getIdName())) {
            sql = sql + "AND id_name LIKE CONCAT('%', ? ,'%')";
            perimeters.add(dto.getIdName());
        }
        int offset = (dto.getPageNo() - 1) * dto.getPageSize();
        int limit = dto.getPageSize();
        sql = sql + "LIMIT ?,?";
        perimeters.add(offset);
        perimeters.add(limit);

        List<TIdTable> nodeList = this.jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(TIdTable.class), perimeters.toArray());
        return nodeList;
    }

    /**
     * 分页查询count
     *
     * @param dto IdTableQueryDto
     * @return 流水号总条数
     */
    public Long pageCount(IdTableQueryDto dto) {
        List<Object> perimeters = new ArrayList<>();
        String sql = "SELECT count(*) FROM t_idtable WHERE 1=1 ";
        if (StringUtils.hasText(dto.getIdCode())) {
            sql = sql + "AND id_code LIKE CONCAT('%', ? ,'%')";
            perimeters.add(dto.getIdCode());
        }
        if (StringUtils.hasText(dto.getIdName())) {
            sql = sql + "AND id_name LIKE CONCAT('%', ? ,'%')";
            perimeters.add(dto.getIdName());
        }
        Long totalSize = jdbcTemplate.queryForObject(sql, Long.class, perimeters.toArray());
        return totalSize;
    }

    /**
     * 删除流水号
     *
     * @param id 流水号主键
     * @return 删除结果
     */
    public boolean delete(Long id) {
        String sql = "DELETE FROM t_idtable WHERE id = ?";
        int num = this.jdbcTemplate.update(sql, id);
        return num > 0;
    }

    /**
     * 新增流水号
     *
     * @param dto 数据
     * @return 新增结果
     */
    public boolean add(IdTableAddDto dto) {
        String sql = "INSERT INTO t_idtable (id_code, id_name, id_step, id_length, has_prefix, id_prefix, has_suffix, id_suffix, remark, created_by) VALUES (?,?,?,?,?,?,?,?,?,?)";
        int num = this.jdbcTemplate.update(sql, dto.getIdCode(), dto.getIdName(), dto.getIdStep(),
                dto.getIdLength(), dto.getHasPrefix(), dto.getIdPrefix(), dto.getHasSuffix(), dto.getIdSuffix(),
                dto.getRemark(), (String) AuthUtils.getLoginId());
        return num > 0;
    }

    /**
     * 修改流水号
     *
     * @param dto 数据
     * @return 修改结果
     */
    public boolean edit(IdTableEditDto dto) {
        String sql = "UPDATE t_idtable SET id_name = ?, id_step = ?, id_length = ?, has_prefix = ?, id_prefix = ?, has_suffix = ?, id_suffix = ?, remark = ?, created_by = ? WHERE id = ?";
        int num = this.jdbcTemplate.update(sql, dto.getIdName(), dto.getIdStep(), dto.getIdLength(),
                dto.getHasPrefix(), dto.getIdPrefix(), dto.getHasSuffix(), dto.getIdSuffix(),
                dto.getRemark(), (String) AuthUtils.getLoginId(), dto.getId());
        return num > 0;
    }
}
