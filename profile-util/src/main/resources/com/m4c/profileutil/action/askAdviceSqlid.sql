DECLARE
	task_name VARCHAR2(30); 
	res CLOB; 
BEGIN 
	task_name := DBMS_SQLTUNE.CREATE_TUNING_TASK ( sql_id => ? );
    DBMS_SQLTUNE.EXECUTE_TUNING_TASK             ( task_name );
    SELECT  DBMS_SQLTUNE.REPORT_TUNING_TASK      ( task_name ) INTO res FROM DUAL ;
     
	? := res;
    ? := task_name;
END;