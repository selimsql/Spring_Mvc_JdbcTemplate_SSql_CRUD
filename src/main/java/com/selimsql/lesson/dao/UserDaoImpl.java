package com.selimsql.lesson.dao;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.selimsql.lesson.domain.User;
import com.selimsql.lesson.dto.UserDTO;
import com.selimsql.lesson.util.Util;

@Repository("userDao")
public class UserDaoImpl implements UserDao {

	private String ENTITY_TABLE_NAME;

	private JdbcTemplate jdbcTemplate;


	@Autowired
	public UserDaoImpl(DataSource dataSource) {
		this.ENTITY_TABLE_NAME = "User";
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public List<User> queryUserList(UserDTO userDTO) {
		String sql = "Select * from " + ENTITY_TABLE_NAME;
		String whereStr = "";
		String code = userDTO.getCode();
		List<Object> listPrm = new ArrayList<Object>();
		if (StringUtils.hasLength(code)) {
			whereStr = " and Code like ?";
			listPrm.add(code + "%");
		}

		String name = userDTO.getName();
		if (StringUtils.hasLength(name)) {
			whereStr += " and Name like ?";
			listPrm.add(name + "%");
		}

		String surname = userDTO.getSurname();
		if (StringUtils.hasLength(surname)) {
			whereStr += " and Surname like ?";
			listPrm.add(surname + "%");
		}

		if (StringUtils.hasLength(whereStr)) {
			sql += " Where " + whereStr.substring(5);
		}

		Object[] params = listPrm.toArray();

		sql += " Order by Id";

		RowMapper<User> rowMapper = BeanPropertyRowMapper.newInstance(User.class);
		RowMapperResultSetExtractor<User> rowMapperResultSetExtractor = new RowMapperResultSetExtractor<User>(rowMapper);
		List<User> list = jdbcTemplate.query(sql, params, rowMapperResultSetExtractor);
		return list;
	}


	@Override
	public User findById(Integer id) {
		String sql = "Select * from " + ENTITY_TABLE_NAME + " Where id = ?";
        RowMapper<User> rowMapper = BeanPropertyRowMapper.newInstance(User.class);

		List<User> list = jdbcTemplate.query(sql, new Object[]{id}, new RowMapperResultSetExtractor<User>(rowMapper, 1));
		int count = (list==null ? 0 : list.size());
		if (count==0)
			return null;

		return list.get(0);
	}

	@Override
	public User findByCode(String code) {
		String sql = "Select * from " + ENTITY_TABLE_NAME + " Where Code = ?";
        RowMapper<User> rowMapper = BeanPropertyRowMapper.newInstance(User.class);
		List<User> list = jdbcTemplate.query(sql, new Object[]{code}, new RowMapperResultSetExtractor<User>(rowMapper, 1));
		int count = (list==null ? 0 : list.size());
		if (count==0)
			return null;

		return list.get(0);
	}

	@Override
	public User findByEmail(String email) {
		String sql = "Select * from " + ENTITY_TABLE_NAME + " Where Email = ?";
        RowMapper<User> rowMapper = BeanPropertyRowMapper.newInstance(User.class);
		List<User> list = jdbcTemplate.query(sql, new Object[]{email}, new RowMapperResultSetExtractor<User>(rowMapper, 1));
		int count = (list==null ? 0 : list.size());
		if (count==0)
			return null;

		return list.get(0);
	}

	@Override
	public int fetchMaxId() {
		String sql = "Select Max(id) From " + ENTITY_TABLE_NAME;

		Integer max = jdbcTemplate.queryForObject(sql, Integer.class);
		return Util.getInt(max);
	}


	@Override
	public int insertRow(User row) {
		String sql = "Insert into " + ENTITY_TABLE_NAME + "(Id, Code, Name, Surname, Password, Email, Phone, Status)"
					+ " Values(?, ?, ?, ?, ?, ?, ?, ?)";

		Object[] values = new Object[]{row.getId(), row.getCode(), row.getName(), row.getSurname(), row.getPassword(),
							row.getEmail(), row.getPhone(), row.getStatus()};

		int rowCount = jdbcTemplate.update(sql, values);
		return rowCount;
	}

	@Override
	public int updateRow(User row) {
		String sql = "Update " + ENTITY_TABLE_NAME
					+ " Set Code = ?, Name = ?, Surname = ?, Password = ?,"
					+ " Email = ?, Phone = ?, Status = ?"
					+ " Where Id = ?";

		Object[] values = new Object[]{row.getCode(), row.getName(), row.getSurname(), row.getPassword(),
							row.getEmail(), row.getPhone(), row.getStatus(),
							row.getId()};
		int rowCount = jdbcTemplate.update(sql, values);
		return rowCount;
	}

	@Override
	public int deleteRow(User row) {
		String sql = "Delete from " + ENTITY_TABLE_NAME + " Where Id = ?";
		int rowCount = jdbcTemplate.update(sql, row.getId());
		return rowCount;
	}
}
