package dbhelper.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import dbhelper.RowHandler;
import dbhelper.db.SortFilterState.Sort;
import dbhelper.graph.Graph;

public class Database {
	private static final String SELECT_TABLES = "select table_name from user_tables order by table_name ASC";

	private static final String SELECT_PK =	"select x0.table_name, x1.COLUMN_NAME, x2.DATA_TYPE  from user_constraints x0 " + 
			"join user_cons_columns x1 on x0.CONSTRAINT_NAME = x1.CONSTRAINT_NAME " +
			"join user_TAB_COLUMNS x2 on x2.COLUMN_NAME = x1.COLUMN_NAME  " + 
			"join user_tables x3 on x0.table_name = x3.table_name "	+ 
			"where x0.CONSTRAINT_TYPE = 'P'";
	
	private static final String SELECT_COLUMNS = "select table_name, column_name, data_type from user_tab_columns";
	
	private static final String SELECT_CONS = 
			"select x0.table_name, x1.column_name, x2.table_name, x2.column_name from user_CONSTRAINTS x0 " +
			"join user_cons_columns x1 on (x0.constraint_name = x1.constraint_name) " +
			"join user_cons_columns x2 on (x0.r_constraint_name = x2.constraint_name) " +
			"where constraint_type='R'";
	
	private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	
	private Connection conn;
	
	Map<String, Table> tables 
		= new LinkedHashMap<String, Table>();
	
	public Database(String jdbcUrl, String login, String password) throws SQLException {
		Locale.setDefault(Locale.ENGLISH);
		conn = DriverManager.getConnection(jdbcUrl, login, password);
		
		getTables();
		fillTables();
		fillCons();
	}
	
	public void close() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void fillCons() {
		try {
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(SELECT_CONS);
			
			while (rs.next()) {
				String tableName = rs.getString(1);
				String colName = rs.getString(2);
				String rTableName = rs.getString(3);
				String rColName = rs.getString(4);
				
				Table rTable = tables.get(rTableName);
				Table table = tables.get(tableName);
				
				Column c1 = table.getColumn(colName);
				Column c2 = rTable.getColumn(rColName);
				
				c1.setReferenced(c2);
				c2.addChild(c1);
			}
			
			rs.close();
			st.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}

	private void getTables() {
		try {
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(SELECT_TABLES);
			
			while (rs.next()) {
				String name = rs.getString(1);
				tables.put(name, new Table(name));
			}
			
			rs.close();
			st.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void fillTables() {
		try {
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(SELECT_PK);
			
			while (rs.next()) {
				tables.get(rs.getString(1)).addColumn(rs.getString(2), rs.getString(3));
				tables.get(rs.getString(1)).setPk(rs.getString(2));
			}
			
			rs.close();

			rs = st.executeQuery(SELECT_COLUMNS);
			while (rs.next()) {
				tables.get(rs.getString(1)).addColumn(rs.getString(2), rs.getString(3));
			}
			
			rs.close();
			st.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Map<String, String> fetchRow(String name, String pkValue) {
		Map<String, String> result = new LinkedHashMap<String, String>();
		
		try {
			Statement st = conn.createStatement();
			String pkName = tables.get(name).getPk();
			String query = String.format("SELECT * FROM %s WHERE %s = %s", name, pkName, pkValue);
			ResultSet rs = st.executeQuery(query);
			
			while (rs.next()) {
				final int columnCount = rs.getMetaData().getColumnCount();
				
				for (int i = 1; i < columnCount; ++i) {
					String val = rs.getString(i);
					
					if (rs.wasNull()) {
						continue;
					}
					
					if (rs.getMetaData().getColumnType(i) == Types.VARCHAR || rs.getMetaData().getColumnType(i) == Types.CHAR) {
						result.put(rs.getMetaData().getColumnName(i), val.replaceAll("'", "''"));
					} 
					else 
					if (rs.getMetaData().getColumnType(i) == Types.TIMESTAMP) {
						result.put(rs.getMetaData().getColumnName(i),sdf.format(rs.getDate(i)));
					}
					else
					if (rs.getMetaData().getColumnType(i) == Types.NUMERIC) {
						result.put(rs.getMetaData().getColumnName(i), val);
					}
					else {
						throw new IllegalStateException();
					}
				}
			}
			
			
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public void exportRow(String name, String pkvalue) {
		try {
			Statement st = conn.createStatement();
			String pkName = tables.get(name).getPk();
			String query = String.format("SELECT * FROM %s WHERE %s = %s", name, pkName, pkvalue);
			ResultSet rs = st.executeQuery(query);
			
			while (rs.next()) {
				StringBuilder sb = new StringBuilder();
				sb.append("INSERT INTO ");
				sb.append(name);
				sb.append("(");
				final int columnCount = rs.getMetaData().getColumnCount();
				
				for (int i = 1; i < columnCount; ++i) {
					String columnName = rs.getMetaData().getColumnName(i);
					sb.append(columnName);
					
					if (i < columnCount - 1) {
						sb.append(',');
					}
				}
				sb.append(") VALUES (");
				
				for (int i = 1; i < columnCount; ++i) {
					String val = rs.getString(i);
					
					if (i > 1) {
						sb.append(',');
					}
					
					if (rs.wasNull()) {
						sb.append("null");
						continue;
					}
					
					if (rs.getMetaData().getColumnType(i) == Types.VARCHAR || rs.getMetaData().getColumnType(i) == Types.CHAR) {
						sb.append('\'');
						sb.append(val.replaceAll("'", "''"));
						sb.append('\'');
					} 
					else 
					if (rs.getMetaData().getColumnType(i) == Types.TIMESTAMP) {
						sb.append("to_date('");
						sb.append(sdf.format(rs.getDate(i)));
						sb.append("','DD.MM.YYYY HH24:MI:SS')");					
					}
					else
					if (rs.getMetaData().getColumnType(i) == Types.NUMERIC) {
						sb.append(val);
					}
					else {
						throw new IllegalStateException();
					}
				}
				sb.append(");");
				System.out.println(sb);
			}
			
			
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Graph<String> getGraph() {
		Graph<String> g = new Graph<String>();
		
		for (Table table : tables.values()) {
			g.addNode(table.getName());
			for (Column c : table.getColumns()) {
				Column refColumn = c.getReferenced();
				if (refColumn != null) {
					g.addEdge(table.getName(), refColumn.getTable().getName(), c);
				}
			}
		}
		
		return g;
	}

	/**
	 * Выбрать поле в строке
	 * @param c колонка, соответствующая полю
	 * @param key PK строки
	 * @return
	 */
	public Long selectFk(Column c, long key) {
		Long result = null;
		Statement st = null;
		try {
			st = conn.createStatement();
			String query = String.format("SELECT %s FROM %s WHERE %s = %s",
				c.getName(), c.getTable().getName(), c.getTable().getPk(), key);
			
			ResultSet rs = st.executeQuery(query);
			
			if (rs.next() == false) {
				throw new RuntimeException("No data for key: " + key);
			}
			assert rs.last();
			String value = rs.getString(1);
			
			result = rs.wasNull() ? null : Long.valueOf(value);
			
			st.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				st.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} 
		
		return result;
	}

	public Table getTable(String tableName) {
		return tables.get(tableName);
	}

	public Map<String, Table> allTables() {
		return tables;
	}

	public int getCount(String tableName) {
		try {
			Statement st = conn.createStatement();
		
			ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tableName);
			rs.next();
			
			int result = rs.getInt(1);
			st.close();
			
			return result;
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public void selectAllFk(String string, RowHandler rowHandler) {
		try {
			Statement st = conn.createStatement();
			
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ");
			
			Iterator<String> it = rowHandler.getColumns().iterator();
			sb.append(it.next());
			while (it.hasNext()) {
				sb.append(",");
				sb.append(it.next());
			}
			sb.append(" FROM ");
			sb.append(string);
			
			ResultSet rs = st.executeQuery(sb.toString());
			
			long[] array = new long[rowHandler.getColumns().size()];
			
			while (rs.next()) {
				for (int i = 1; i <= rowHandler.getColumns().size(); ++i) {
					array[i-1] = rs.getLong(i);
				}
				
				rowHandler.processRow(array);
			}
			
			st.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<Long> executeLongResultQuery(String query) {
		List<Long> result = new LinkedList<Long>();
		try {
			Statement st = conn.createStatement();

			ResultSet rs = st.executeQuery(query);
			while (rs.next()) {
				result.add(rs.getLong(1));
			}
			st.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result;
	}

	public String[][] getRows(String name, String[] columnNames, SortFilterState sortFilter, int start, int pageSize) {
		List<String[]> result = new LinkedList<String[]>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");

		for (int i = 0; i < columnNames.length; ++i) {
			sb.append(columnNames[i]);
			if (i < columnNames.length - 1) {
				sb.append(",");
			}
		}
	
		sb.append(" FROM ");
		sb.append(name);
		sb.append(" WHERE 1=1 ");
		
		for (Entry<String, String> filter : sortFilter.getFilter()) {
			sb.append("AND ");
			sb.append(filter.getKey());
			sb.append(" LIKE '%");
			sb.append(filter.getValue());
			sb.append("%' ");
		}
		
		Entry<String, Sort> sort = sortFilter.getSort();
		if (sort != null) {
			sb.append("ORDER BY " + sort.getKey());
			sb.append(sort.getValue() == Sort.ASC ? " ASC" : " DESC");
		}
		
		
		String query = sb.toString();
		query = String.format(
			"SELECT * FROM (SELECT x0.*, ROWNUM as rnum FROM  (%s) x0) WHERE rnum >=  %s AND rnum < %s", sb, start, start + pageSize);
		
		Statement st = null;
		try {
			st = conn.createStatement();
		
			ResultSet rs = st.executeQuery(query);
			while (rs.next()) {
				String[] row = new String[columnNames.length];
				for (int i = 1; i < columnNames.length + 1; ++i) {
					row[i - 1] = rs.getString(i);
				}
				
				result.add(row);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return result.toArray(new String[][]{{}});
	}

	public long getLinkedRowsCount(String table, String column, String value) {
		return executeLongResultQuery(String.format("SELECT COUNT (*) FROM %s WHERE %s = %s",
			table, column, value)).get(0);
	}
}
