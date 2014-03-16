DECLARE
 profile_name VARCHAR2(30);

BEGIN
 profile_name := DBMS_SQLTUNE.accept_sql_profile (
	task_name => ?,
    task_owner => 'VOSHOD',
	replace => TRUE
 );
 
 DBMS_SQLTUNE.ALTER_SQL_PROFILE (
	name =>  profile_name, 
	attribute_name  =>  'CATEGORY',  
	value =>  'OESO_TEST'
 );
 
 ? := profile_name;
END;	