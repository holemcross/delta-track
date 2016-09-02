package helpers;

/**
 * Created by amortega on 8/31/2016.
 */
public class SqlHelper {
    // SQL Text Helper
    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS ";
    public static final String DROP_TABLE = "DROP TABLE IF EXISTS ";
    public static final String PRIMARY_KEY = " INTEGER PRIMARY KEY AUTOINCREMENT, ";
    public static final String TEXT_TYPE = " TEXT";
    public static final String INT_TYPE = " INTEGER";
    public static final String REAL_TYPE = " REAL"; // floating point
    public static final String COMMA_SEP = ", ";
    public static final String ForeignKeyHelper(String columnName, String foreignTable){
        return "FOREIGN KEY("+columnName+") REFERENCES "+foreignTable+"(_id)";
    }
    public static final String TableDotProperty(String tableName, String propertyName){
        return tableName + "." + propertyName;
    }
}
