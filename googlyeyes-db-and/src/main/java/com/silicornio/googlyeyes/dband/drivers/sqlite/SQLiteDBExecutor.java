package com.silicornio.googlyeyes.dband.drivers.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.silicornio.googlyeyes.dband.general.GEL;

public class SQLiteDBExecutor {
	
	/**
	 * Execute the statement given
	 * @param conn SQLiteDatabase where execute
	 * @param sStatement String to execute
	 * @return boolean TRUE if was executed correctly
	 */
	public static boolean executeStatementSimple(SQLiteDatabase conn, String sStatement) throws SQLiteDBException {
        try {

            GEL.d("STATEMENT: " + sStatement);
            conn.execSQL(sStatement);
            return true;

        } catch (Exception e) {
			GEL.e("Returning exception: " + e.toString());
            throw SQLiteDBException.create(e.toString());
        }
	}
	
	/**
	 * Execute the statement given
	 * @param conn SQLiteDatabase where execute
	 * @param sStatement String to execute
	 * @return int value of the ID generated, for no ID: 0 if OK, -1 if error
	 */
	public static int executeStatementInsert(SQLiteDatabase conn, String sStatement) throws SQLiteDBException {
        SQLiteStatement statement = null;
        try {

            GEL.d("STATEMENT: " + sStatement);
            statement = conn.compileStatement(sStatement);
            return (int)statement.executeInsert();

        } catch (Exception e) {
			GEL.e("Returning exception: " + e.toString());
			throw SQLiteDBException.create(e.toString());
        } finally {
            if(statement!=null){
                try {
                    statement.close();
                } catch (Exception e) {
					GEL.e("Exception closing statement, not expected: " + e.toString());
				}
            }
        }
	}
	
	
	/**
	 * Execute the statement given
	 * @param conn SQLiteDatabase where execute
	 * @param sStatement String to execute
	 * @return int value of the ID generated, for no ID: 0 if OK, -1 if error
	 */
	public static boolean executeStatementUpdateDelete(SQLiteDatabase conn, String sStatement) throws SQLiteDBException {
		
		SQLiteStatement statement = null;
		try {

			GEL.d("STATEMENT: " + sStatement);
            statement = conn.compileStatement(sStatement);
            return statement.executeUpdateDelete()>0;
						
		} catch (Exception e) {
			GEL.e("Returning exception: " + e.toString());
			throw SQLiteDBException.create(e.toString());
		} finally {
			if(statement!=null){
				try {
					statement.close();
				} catch (Exception e) {
					GEL.e("Exception closing statement, not expected: " + e.toString());
				}
			}
		}
		
	}
	
	/**
	 * Execute the statement given
	 * @param conn SQLiteDatabase where execute
	 * @param sStatement String to execute
	 * @return boolean TRUE if was executed correctly
	 */
	protected static boolean executeQueryStatement(SQLiteDatabase conn, String sStatement, StatementListener listener) throws SQLiteDBException {

		try {
			GEL.d("STATEMENT QUERY: " + sStatement);
			Cursor cursor = conn.rawQuery(sStatement, null);
			if(cursor!=null){
				
				return listener.onResultSet(cursor);
			}else{
				return false;
			}
						
		} catch (Exception e) {
			GEL.e("Returning exception: " + e.toString());
			throw SQLiteDBException.create(e.toString());
		}
		
	}
	
	protected interface StatementListener{
		boolean onResultSet(Cursor cursor);
	}
	
}
